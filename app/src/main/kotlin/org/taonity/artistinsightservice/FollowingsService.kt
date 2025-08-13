package org.taonity.artistinsightservice

import jakarta.validation.Validator
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver.principal
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.taonity.artistinsightservice.mvc.EnrichableArtistObject
import org.taonity.artistinsightservice.mvc.FollowingsResponse
import org.taonity.artistinsightservice.mvc.SpotifyResponse
import org.taonity.artistinsightservice.openai.OpenAIService
import org.taonity.artistinsightservice.persistence.genre.ArtistGenreService
import org.taonity.artistinsightservice.persistence.spotify_user_enriched_artists.SpotifyUserEnrichedArtistsService
import org.taonity.artistinsightservice.persistence.user.SpotifyUserEntity
import org.taonity.artistinsightservice.persistence.user.SpotifyUserService
import org.taonity.spotify.model.ArtistObject
import org.taonity.spotify.model.PagingArtistObject
import java.util.ArrayList

@Service
class FollowingsService(
    private val artistGenreService: ArtistGenreService,
    private val spotifyUserService: SpotifyUserService,
    private val spotifyRestClient: RestClient,
    private val openAIService: OpenAIService,
    private val spotifyUserEnrichedArtistsService: SpotifyUserEnrichedArtistsService,
    private val validator: Validator,
    @Value("\${spotify.api-base-url}")
    private val spotifyApiBaseUrl: String,
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    fun fetchRawFollowings(): FollowingsResponse {
        val notEnrichedFollowings = safeFetchFollowing()
            .map { EnrichableArtistObject(it, false) }
        return FollowingsResponse(notEnrichedFollowings)
    }

    fun fetchGenreEnrichedFollowings(spotifyId: String): FollowingsResponse {
        val safeFollowings: List<SafeArtistObject> = safeFetchFollowing()

        val spotifyUser = spotifyUserService.findBySpotifyId(spotifyId)
        val gptUsagesLeft = spotifyUser.gptUsagesLeft
        if (gptUsagesLeft == 0) {
            LOGGER.info { "Spotify user with id $spotifyId have no GPT usages left" }
            val notEnrichedFollowings = safeFollowings
                .map { EnrichableArtistObject(it, false) }
            return FollowingsResponse(notEnrichedFollowings)
        }

        val enrichedFollowings = safeFollowings.map { enrichWithGenresIfPossible(it, spotifyUser)}

        return FollowingsResponse(enrichedFollowings)
    }

    private fun enrichWithGenresIfPossible(artistObject: SafeArtistObject, spotifyUser: SpotifyUserEntity): EnrichableArtistObject {
        if (artistObject.genres.isNotEmpty()) {
            return EnrichableArtistObject(artistObject, false)
        }
        val artistId = artistObject.id
        val artistName = artistObject.name
        val artistGenresAndUserLinkDto = artistGenreService.getGenresAndUserStatus(artistId, spotifyUser.spotifyId)

        if (artistGenresAndUserLinkDto.genres.isEmpty()) {
            artistObject.genres = provideGenresWithOpenAIAndCache(artistObject, spotifyUser.spotifyId)
            LOGGER.info { "Artis $artistName with id $artistId was provided with genres ${artistObject.genres} by OpenAI call" }
            spotifyUser.gptUsagesLeft--
            return EnrichableArtistObject(artistObject, true)
        }
        artistObject.genres = artistGenresAndUserLinkDto.genres
        if (artistGenresAndUserLinkDto.userHasArtist.booleanValue()) {
            LOGGER.info { "Artis $artistName with id $artistId was provided with genres ${artistObject.genres} by DB call, GPT usages not changed - ${spotifyUser.gptUsagesLeft}" }
        } else {
            spotifyUser.gptUsagesLeft--
            LOGGER.info { "Artis $artistName with id $artistId was provided with genres ${artistObject.genres} by DB call, GPT usages decremented - ${spotifyUser.gptUsagesLeft}" }
        }
        return EnrichableArtistObject(artistObject, true)
    }

    private fun provideGenresWithOpenAIAndCache(artistObject: SafeArtistObject, spotifyId: String): List<String> {
        val openAIProvidedGenres = openAIService.provideGenres(artistObject.name)
        spotifyUserEnrichedArtistsService.saveEnrichedArtistsForUser(spotifyId, listOf(Pair(artistObject, openAIProvidedGenres)))
        return openAIProvidedGenres
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
        var url: String? = "$spotifyApiBaseUrl/v1/me/following?type=artist"

        while (url != null) {
            val page: PagingArtistObject = fetchPage(url)
            allItems.addAll(page.items)
            url = page.next
        }

        return allItems
    }
}
