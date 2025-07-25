package org.taonity.artistinsightservice

import mu.KotlinLogging
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId
import org.springframework.security.oauth2.client.web.client.RequestAttributePrincipalResolver.principal
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.taonity.artistinsightservice.mvc.FollowingsResponse
import org.taonity.artistinsightservice.mvc.SpotifyResponse
import org.taonity.artistinsightservice.openai.OpenAIService
import org.taonity.artistinsightservice.persistence.user.SpotifyUserService
import org.taonity.artistinsightservice.persistence.genre.ArtistGenreService
import org.taonity.spotify.model.ArtistObject
import org.taonity.spotify.model.PagingArtistObject
import java.util.ArrayList

@Service
class FollowingsService(
    private val artistGenreService: ArtistGenreService,
    private val spotifyUserService: SpotifyUserService,
    private val spotifyRestClient: RestClient,
    private val openAIService: OpenAIService
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    fun fetchRawFollowings(): FollowingsResponse {
        return FollowingsResponse(fetchFollowings(), false)
    }

    fun fetchGenreEnrichedFollowings(spotifyId: String): FollowingsResponse {
        val followings: List<ArtistObject> = fetchFollowings()

        val hasGptUsage = spotifyUserService.decrementGptUsagesIfLeft(spotifyId)
        if (!hasGptUsage) {
            LOGGER.warn { "Spotify user with id $spotifyId have no GPT usages left" }
            return FollowingsResponse(fetchFollowings(), false)
        }

        followings.forEach(this::enrichWithGenresIfPossible)

        return FollowingsResponse(fetchFollowings(), true)
    }

    private fun enrichWithGenresIfPossible(artist: ArtistObject) {
        if (artist.genres!!.isEmpty()) {
            val artistName = artist.name
            if (artistName.isNullOrBlank()) {
                LOGGER.warn { "Artist have no name $artistName" }
                return
            }
            val cachedGenres = artistGenreService.getGenres(artistName)
            if (cachedGenres.isEmpty()) {
                artist.genres = provideGenresWithOpenAIAndCache(artistName)
                LOGGER.info { "Artis $artistName was provided with genres ${artist.genres} by OpenAI call" }
                return
            }
            artist.genres = cachedGenres
            LOGGER.info { "Artis $artistName was provided with genres ${artist.genres} by DB call" }

        }
    }

    private fun provideGenresWithOpenAIAndCache(artistName: String): List<String> {
        val openAIProvidedGenres = openAIService.provideGenres(artistName)
        artistGenreService.saveGenres(artistName, openAIProvidedGenres)
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

    private fun fetchAllPages(fetchPage: (String) -> PagingArtistObject): List<ArtistObject> {
        val allItems: MutableList<ArtistObject> = ArrayList()
        var url: String? = "https://api.spotify.com/v1/me/following?type=artist"

        while (url != null) {
            val page: PagingArtistObject = fetchPage(url)
            allItems.addAll(page.items)
            url = page.next
        }

        return allItems
    }
}
