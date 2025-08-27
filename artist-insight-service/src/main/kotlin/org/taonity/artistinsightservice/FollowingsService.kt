package org.taonity.artistinsightservice

import jakarta.validation.Validator
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver.principal
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import org.taonity.artistinsightservice.mvc.FollowingsResponse
import org.taonity.artistinsightservice.mvc.SpotifyResponse
import org.taonity.spotify.model.ArtistObject
import org.taonity.spotify.model.PagingArtistObject
import java.util.ArrayList

@Service
class FollowingsService(
    private val spotifyRestClient: RestClient,
    private val validator: Validator,
    private val newArtistEnrichmentService: NewArtistEnrichmentService,
    private val userArtistEnrichmentService: UserArtistEnrichmentService,
    @Value("\${spotify.api-base-url}")
    private val spotifyApiBaseUrl: String,
    private val responseAttachments: ResponseAttachments
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    fun fetchRawFollowings(spotifyId: String): FollowingsResponse {
        val safeFollowings = safeFetchFollowing()
        val userFollowings = userArtistEnrichmentService.enrichUserArtists(spotifyId, safeFollowings)
        return FollowingsResponse(artists = userFollowings)
    }

    fun fetchGenreEnrichedFollowings(spotifyId: String): FollowingsResponse {
        val safeFollowings: List<SafeArtistObject> = safeFetchFollowing()
        val enrichedFollowings = newArtistEnrichmentService.enrichNewArtists(spotifyId, safeFollowings)
        return FollowingsResponse(enrichedFollowings)
    }


    private fun fetchFollowings() = fetchAllPages { uri ->
        spotifyRestClient.get()
            .uri(uri)
            .attributes(clientRegistrationId("spotify-artist-insight-service"))
            .attributes(principal("display_name"))
            .retrieve()
            .body<SpotifyResponse<PagingArtistObject>>()!!
            .artists
    }

    private fun safeFetchFollowing() = fetchFollowings()
        .map(ValidatedArtistObject::of)
        .filter { validatedArtistObject ->
            val violations = validator.validate(validatedArtistObject)
            if (violations.isEmpty()) {
                return@filter true
            }
            val errorMessage = violations.joinToString("; ") { "${it.propertyPath}: ${it.message}" }
            LOGGER.warn { "Validation failed for artist $validatedArtistObject with error: $errorMessage" }
            return@filter false
        }
        .map(ValidatedArtistObject::toSafe)

    private fun fetchAllPages(fetchPage: (String) -> PagingArtistObject): List<ArtistObject> {
        val allItems: MutableList<ArtistObject> = ArrayList()
        val initialUrl = UriComponentsBuilder
            .fromUriString("$spotifyApiBaseUrl/me/following")
            .queryParam("type", "artist")
            .queryParam("limit", 50)
            .build()
            .toUriString()
        var url: String? = initialUrl

        while (url != null) {
            if (allItems.size >= 1000) {
                responseAttachments.advisories.add(Advisory.TOO_MANY_FOLLOWERS)
                break
            }
            val page: PagingArtistObject = fetchPage(url)
            allItems.addAll(page.items)
            url = page.next
        }

        return allItems
    }
}
