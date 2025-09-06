package org.taonity.artistinsightservice.spotify

import jakarta.validation.Validator
import org.springframework.beans.factory.annotation.Value
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
import org.taonity.artistinsightservice.utils.hasCause
import org.taonity.spotify.model.ArtistObject
import org.taonity.spotify.model.PagingArtistObject
import java.io.InterruptedIOException

@Service
class SpotifyService(
    private val spotifyRestClient: RestClient,
    private val validator: Validator,
    @Value("\${spotify.api-base-url}")
    private val spotifyApiBaseUrl: String,
    private val responseAttachments: ResponseAttachments
) {

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
}