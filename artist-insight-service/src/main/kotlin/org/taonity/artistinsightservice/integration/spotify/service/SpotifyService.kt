package org.taonity.artistinsightservice.integration.spotify.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import org.taonity.artistinsightservice.advisory.Advisory
import org.taonity.artistinsightservice.advisory.ResponseAttachments
import org.taonity.artistinsightservice.integration.spotify.dto.SafeArtistObject
import org.taonity.artistinsightservice.integration.spotify.dto.SpotifyResponse
import org.taonity.artistinsightservice.common.util.hasCause
import org.taonity.artistinsightservice.integration.spotify.exception.SpotifyClientException
import org.taonity.artistinsightservice.integration.spotify.exception.SpotifyTimeoutException
import org.taonity.spotify.model.ArtistObject
import org.taonity.spotify.model.GetMultipleArtists200Response
import org.taonity.spotify.model.PagingArtistObject
import org.taonity.spotify.model.PublicUserObject
import java.io.InterruptedIOException
import java.time.Duration

@Service
class SpotifyService(
    private val spotifyAuthorisationCodeRestClient: RestClient,
    private val spotifyClientCredentialsRestClient: RestClient,
    @Value("\${spotify.api-base-url}")
    private val spotifyApiBaseUrl: String,
    @Value("\${spotify.healthcheck-user-id}")
    private val healthCheckUserId: String,
    private val responseAttachments: ResponseAttachments
) {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private val FETCH_ALL_PAGES_BUDGET: Duration = Duration.ofSeconds(30)
    }


    fun fetchFollowings(): List<SafeArtistObject> = safeFetchFollowing()

    private fun safeFetchFollowing() = fetchAllPages()
        .map(::validateAndMapArtist)

    private fun fetchAllPages(): List<ArtistObject> {
        val allItems = mutableListOf<ArtistObject>()
        var url: String? = UriComponentsBuilder
            .fromUriString("$spotifyApiBaseUrl/me/following")
            .queryParam("type", "artist")
            .queryParam("limit", 50)
            .build()
            .toUriString()

        val deadlineNanos = System.nanoTime() + FETCH_ALL_PAGES_BUDGET.toNanos()
        while (url != null) {
            if (allItems.size >= 1000) {
                responseAttachments.advisories.add(Advisory.TOO_MANY_FOLLOWINGS)
                break
            }
            if (System.nanoTime() >= deadlineNanos) {
                LOGGER.warn { "Spotify followings pagination exceeded ${FETCH_ALL_PAGES_BUDGET.seconds}s budget after ${allItems.size} items" }
                responseAttachments.advisories.add(Advisory.SPOTIFY_TIMEOUT)
                break
            }
            val page: PagingArtistObject = fetchPageWithAuthentication(url)
            allItems.addAll(page.items)
            url = page.next
        }

        return allItems
    }

    private fun fetchPageWithAuthentication(uri: String): PagingArtistObject {
        val responseSpec: RestClient.ResponseSpec = spotifyAuthorisationCodeRestClient.get()
            .uri(uri)
            .attributes(RequestAttributeClientRegistrationIdResolver.clientRegistrationId("spotify-artist-insight-service"))
            .attributes(RequestAttributePrincipalResolver.principal("display_name"))
            .retrieve()

        return responseSpec.bodyOrThrow<SpotifyResponse<PagingArtistObject>>(
            timeoutMessage = "Timeout while retrieving user followings",
            failureMessage = "Failed to retrieve user followings"
        ).artists
    }

    fun getHealthCheckUserUrl(): String = "$spotifyApiBaseUrl/users/$healthCheckUserId"

    fun getHealthCheckUser(): ResponseEntity<String> {
        val url = getHealthCheckUserUrl()
        val responseSpec: RestClient.ResponseSpec = spotifyClientCredentialsRestClient.get()
            .uri(url)
            .attributes(RequestAttributeClientRegistrationIdResolver.clientRegistrationId("spotify-client-credentials"))
            .attributes(RequestAttributePrincipalResolver.principal("display_name"))
            .retrieve()
        return responseSpec.toEntity(String::class.java)
    }

    fun fetchArtistsByIds(artistIds: List<String>): List<SafeArtistObject> {
        if (artistIds.isEmpty()) {
            return emptyList()
        }

        // Spotify API allows max 50 artist IDs per request
        return artistIds.chunked(50).flatMap { chunk ->
            fetchArtistsBatchWithClientCredentials(chunk)
        }
    }

    private fun fetchArtistsBatchWithClientCredentials(artistIds: List<String>): List<SafeArtistObject> {
        val url = UriComponentsBuilder
            .fromUriString("$spotifyApiBaseUrl/artists")
            .queryParam("ids", artistIds.joinToString(","))
            .build()
            .toUriString()

        val responseSpec: RestClient.ResponseSpec = spotifyClientCredentialsRestClient.get()
            .uri(url)
            .attributes(RequestAttributeClientRegistrationIdResolver.clientRegistrationId("spotify-client-credentials"))
            .attributes(RequestAttributePrincipalResolver.principal("display_name"))
            .retrieve()

        val response = responseSpec.bodyOrThrow<GetMultipleArtists200Response>(
            timeoutMessage = "Timeout while retrieving artists by IDs",
            failureMessage = "Failed to retrieve artists by IDs"
        )

        return response.artists
            .filterNotNull()
            .map(::validateAndMapArtist)
    }

    fun fetchUserPublicProfile(userId: String): PublicUserObject {
        val url = "$spotifyApiBaseUrl/users/$userId"

        val responseSpec: RestClient.ResponseSpec = spotifyClientCredentialsRestClient.get()
            .uri(url)
            .attributes(RequestAttributeClientRegistrationIdResolver.clientRegistrationId("spotify-client-credentials"))
            .attributes(RequestAttributePrincipalResolver.principal("display_name"))
            .retrieve()

        return responseSpec.bodyOrThrow(
            timeoutMessage = "Timeout while retrieving user profile",
            failureMessage = "Failed to retrieve user profile"
        )
    }

    private fun validateAndMapArtist(artistObject: ArtistObject): SafeArtistObject = try {
        SafeArtistObject.fromApi(artistObject)
    } catch (e: IllegalArgumentException) {
        throw SpotifyClientException("Validation failed for artist $artistObject: ${e.message}", e)
    }

    private inline fun <reified T : Any> RestClient.ResponseSpec.bodyOrThrow(
        timeoutMessage: String,
        failureMessage: String
    ): T =
        try {
            body<T>() ?: throw SpotifyClientException("$failureMessage: empty response body")
        } catch (e: SpotifyClientException) {
            throw e
        } catch (e: Exception) {
            if (e.hasCause(InterruptedIOException::class.java)) {
                throw SpotifyTimeoutException(timeoutMessage, e)
            }
            throw SpotifyClientException(failureMessage, e)
        }
}