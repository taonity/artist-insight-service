package org.taonity.artistinsightservice.integration.spotify.service

import jakarta.validation.Validator
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
import org.taonity.artistinsightservice.artist.dto.SafeArtistObject
import org.taonity.artistinsightservice.artist.dto.SpotifyResponse
import org.taonity.artistinsightservice.artist.dto.ValidatedArtistObject
import org.taonity.artistinsightservice.infrastructure.utils.hasCause
import org.taonity.artistinsightservice.integration.spotify.exception.SpotifyClientException
import org.taonity.artistinsightservice.integration.spotify.exception.SpotifyTimeoutException
import org.taonity.spotify.model.ArtistObject
import org.taonity.spotify.model.GetMultipleArtists200Response
import org.taonity.spotify.model.PagingArtistObject
import java.io.InterruptedIOException

@Service
class SpotifyService(
    private val spotifyAuthorisationCodeRestClient: RestClient,
    private val spotifyClientCredentialsRestClient: RestClient,
    private val validator: Validator,
    @Value("\${spotify.api-base-url}")
    private val spotifyApiBaseUrl: String,
    @Value("\${spotify.healthcheck-user-id}")
    private val heathCheckUserId: String,
    private val responseAttachments: ResponseAttachments
) {

    fun fetchFollowings(): List<SafeArtistObject> {
        return safeFetchFollowing()
    }

    private fun safeFetchFollowing() = fetchAllPages()
        .map(ValidatedArtistObject.Companion::of)
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
        val responseSpec: RestClient.ResponseSpec = spotifyAuthorisationCodeRestClient.get()
            .uri(uri)
            .attributes(RequestAttributeClientRegistrationIdResolver.clientRegistrationId("spotify-artist-insight-service"))
            .attributes(RequestAttributePrincipalResolver.principal("display_name"))
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

    fun getHealthCheckUserUrl() : String {
        return "$spotifyApiBaseUrl/users/$heathCheckUserId"
    }

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

        val response = try {
            responseSpec.body<GetMultipleArtists200Response>()!!
        } catch (e: Exception) {
            if (e.hasCause(InterruptedIOException::class.java)) {
                throw SpotifyTimeoutException("Timeout while retrieving artists by IDs", e)
            }
            throw SpotifyClientException("Failed to retrieve artists by IDs", e)
        }

        return response.artists
            .filterNotNull()
            .map(ValidatedArtistObject.Companion::of)
            .map { validatedArtistObject ->
                val violations = validator.validate(validatedArtistObject)
                if (violations.isEmpty()) {
                    return@map validatedArtistObject.toSafe()
                }
                val errorMessage = violations.joinToString("; ") { "${it.propertyPath}: ${it.message}" }
                throw SpotifyClientException("Validation failed for artist $validatedArtistObject with error: $errorMessage")
            }
    }
}