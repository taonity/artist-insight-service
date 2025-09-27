package org.taonity.artistinsightservice.spotify

import jakarta.validation.Validator
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Status
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver.principal
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import org.taonity.artistinsightservice.attachments.Advisory
import org.taonity.artistinsightservice.attachments.ResponseAttachments
import org.taonity.artistinsightservice.followings.dto.SafeArtistObject
import org.taonity.artistinsightservice.followings.dto.ValidatedArtistObject
import org.taonity.artistinsightservice.followings.dto.SpotifyResponse
import org.taonity.artistinsightservice.health.HealthCheckResult
import org.taonity.artistinsightservice.utils.hasCause
import org.taonity.spotify.model.ArtistObject
import org.taonity.spotify.model.PagingArtistObject
import java.io.InterruptedIOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.util.LinkedHashMap

@Service
class SpotifyService(
    private val spotifyRestClient: RestClient,
    private val validator: Validator,
    @Value("\${spotify.api-base-url}")
    private val spotifyApiBaseUrl: String,
    private val responseAttachments: ResponseAttachments
) {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private const val MAX_BODY_PREVIEW_CHARS = 160
    }

    fun fetchFollowings(): List<SafeArtistObject> {
        return safeFetchFollowing()
    }

    private fun safeFetchFollowing() = fetchAllPages()
        .map(ValidatedArtistObject::of)
        .map { validatedArtistObject ->
            val violations = validator.validate(validatedArtistObject)
            if (violations.isEmpty()) {
                return@map validatedArtistObject.toSafe()
            }
            val errorMessage = violations.joinToString("; ") { "${it.propertyPath}: ${it.message}" }
            throw SpotifyClientException("Validation failed for artist $validatedArtistObject with error: $errorMessage")
        }

    private fun fetchAllPages(): List<ArtistObject> {
        val allItems: MutableList<ArtistObject> = ArrayList()
        val initialUrl = UriComponentsBuilder
            .fromUriString("$spotifyApiBaseUrl/me/following")
            .queryParam("type", "artist")
            .queryParam("limit", 50)
            .build()
            .toUriString()
        var url: String? = initialUrl

        // TODO: implement timeout fo whole page series retrieval
        while (url != null) {
            if (allItems.size >= 1000) {
                responseAttachments.advisories.add(Advisory.TOO_MANY_FOLLOWINGS)
                break
            }
            val page: PagingArtistObject = fetchPageWithAuthentication(url)
            allItems.addAll(page.items)
            url = page.next
        }

        return allItems
    }

    private fun fetchPageWithAuthentication(uri: String): PagingArtistObject {
        val responseSpec: RestClient.ResponseSpec = spotifyRestClient.get()
            .uri(uri)
            .attributes(clientRegistrationId("spotify-artist-insight-service"))
            .attributes(principal("display_name"))
            .retrieve()

        val spotifyResponse = try {
            responseSpec.body<SpotifyResponse<PagingArtistObject>>()!!
        } catch (e: Exception) {
            if (e.hasCause(InterruptedIOException::class.java)) {
                throw SpotifyTimeoutException("Timeout while retrieving user followings", e)
            }
            throw SpotifyClientException("Failed to retrieve user followings", e)
        }
        return spotifyResponse.artists
    }

    fun checkAvailability(url: String, requestTimeout: Duration): HealthCheckResult {
        val httpClient = HttpClient.newBuilder()
            .connectTimeout(requestTimeout)
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(requestTimeout)
            .GET()
            .build()
        val start = Instant.now()

        return try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            val statusCode = response.statusCode()
            val details = LinkedHashMap<String, Any?>()
            details["url"] = url
            details["statusCode"] = statusCode
            details["responseTimeMs"] = elapsedMs

            val healthy = statusCode in 200..299 || statusCode == 401 || statusCode == 403

            if (statusCode == 401 || statusCode == 403) {
                details["note"] = "Received $statusCode indicating OAuth token is required for full access."
            }

            if (!healthy) {
                details["responsePreview"] = response.body().take(MAX_BODY_PREVIEW_CHARS)
            }

            HealthCheckResult(
                status = if (healthy) Status.UP else Status.DOWN,
                details = details
            )
        } catch (exception: Exception) {
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            LOGGER.warn(exception) { "Spotify availability check failed for $url" }
            val details = mapOf(
                "url" to url,
                "responseTimeMs" to elapsedMs,
                "error" to (exception.message ?: exception::class.simpleName)
            )
            HealthCheckResult(Status.DOWN, details)
        }
    }
}