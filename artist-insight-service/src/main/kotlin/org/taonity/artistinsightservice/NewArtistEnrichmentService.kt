package org.taonity.artistinsightservice

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.mvc.EnrichableArtists
import org.taonity.artistinsightservice.openai.OpenAIService
import org.taonity.artistinsightservice.persistence.genre.ArtistGenreService
import org.taonity.artistinsightservice.persistence.spotify_user_enriched_artists.SpotifyUserEnrichedArtistsService

@Service
class NewArtistEnrichmentService(
    private val artistGenreService: ArtistGenreService,
    private val gptUsageService: GptUsageService,
    private val openAIService: OpenAIService,
    private val spotifyUserEnrichedArtistsService: SpotifyUserEnrichedArtistsService,
) {
    fun enrichNewArtists(spotifyId: String, rawArtists: List<SafeArtistObject>) : List<EnrichableArtists> {
        return rawArtists.map {
            NewArtistEnricher(artistGenreService, gptUsageService, openAIService, spotifyUserEnrichedArtistsService,
                spotifyId, it
            ).enrichWithGenresIfPossible()
        }
    }
}

class NewArtistEnricher(
    private val artistGenreService: ArtistGenreService,
    private val gptUsageService: GptUsageService,
    private val openAIService: OpenAIService,
    private val spotifyUserEnrichedArtistsService: SpotifyUserEnrichedArtistsService,
    private val spotifyId: String,
    private val rawArtist: SafeArtistObject,
    private val artistId: String = rawArtist.id,
    private val artistName: String = rawArtist.name
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    fun enrichWithGenresIfPossible(): EnrichableArtists {
        if (rawArtist.genres.isNotEmpty()) {
            return EnrichableArtists(rawArtist.copy(), false)
        }
        
        val artistGenresAndUserLinkDto = artistGenreService.getGenresAndUserStatus(artistId, spotifyId)
        val dbArtistGenres = artistGenresAndUserLinkDto.genres
        val userHasArtists = artistGenresAndUserLinkDto.userHasArtist

        if (dbArtistGenres.isEmpty()) {
            return enrichUsingNewGenresFromGptIfPossible()
        }

        if (userHasArtists) {
            return enrichUsingOwnedGenresFromDb(dbArtistGenres)
        }

        val userUsageConsumed = gptUsageService.consumeUserUsage(spotifyId)
        if (!userUsageConsumed) {
            return buildWithNoGenresWithGptUserDepletionFlag()
        }

        return enrichUsingNewGenresFromDb(dbArtistGenres)
    }

    private fun enrichUsingNewGenresFromDb(dbArtistGenres: List<String>): EnrichableArtists {
        val enrichedArtist = rawArtist.copy(genres = dbArtistGenres)
        spotifyUserEnrichedArtistsService.saveEnrichedArtistsForUser(spotifyId, listOf(Pair(enrichedArtist, dbArtistGenres)))
        LOGGER.info { "Artis $artistName with id $artistId was provided with genres ${enrichedArtist.genres} by DB call, GPT usages decremented" }
        return EnrichableArtists(enrichedArtist, true)
    }

    private fun buildWithNoGenresWithGptUserDepletionFlag(): EnrichableArtists {
        LOGGER.info { "Spotify user with id $spotifyId has no GPT usages left for artist $artistName" }
        return EnrichableArtists(rawArtist.copy(), false, notEnoughUserGptUsages = true)
    }

    private fun enrichUsingOwnedGenresFromDb(dbArtistGenres: List<String>): EnrichableArtists {
        val enrichedArtist = rawArtist.copy(genres = dbArtistGenres)
        LOGGER.info { "Artis $artistName with id $artistId was provided with genres ${enrichedArtist.genres} by DB call, GPT usages not changed" }
        return EnrichableArtists(enrichedArtist)
    }

    private fun enrichUsingNewGenresFromGptIfPossible(): EnrichableArtists {
        val userUsagesConsumed = gptUsageService.consumeUserUsage(spotifyId)
        val globalUsageConsumed = gptUsageService.consumeGlobalUsage()
        if (!globalUsageConsumed || !userUsagesConsumed) {
            LOGGER.info { "Cannot enrich artist $artistName with id $artistId due to GPT usage limits: userUsagesConsumed=$userUsagesConsumed, globalUsageConsumed=$globalUsageConsumed" }
            return EnrichableArtists(
                rawArtist,
                genreEnriched = false,
                notEnoughUserGptUsages = userUsagesConsumed,
                notEnoughGlobalGptUsages = globalUsageConsumed
            )
        }
        val openAIProvidedGenres = openAIService.provideGenres(rawArtist.name)
        val enrichedArtist = rawArtist.copy(genres = openAIProvidedGenres)
        spotifyUserEnrichedArtistsService.saveEnrichedArtistsForUser(spotifyId, listOf(Pair(enrichedArtist, openAIProvidedGenres)))
        LOGGER.info { "Artis $artistName with id $artistId was provided with genres ${enrichedArtist.genres} by OpenAI call" }
        return EnrichableArtists(enrichedArtist)
    }
}