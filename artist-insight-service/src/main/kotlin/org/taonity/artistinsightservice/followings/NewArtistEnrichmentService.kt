package org.taonity.artistinsightservice.followings

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import org.taonity.artistinsightservice.attachments.Advisory
import org.taonity.artistinsightservice.GptUsageService
import org.taonity.artistinsightservice.attachments.ResponseAttachments
import org.taonity.artistinsightservice.followings.dto.SafeArtistObject
import org.taonity.artistinsightservice.followings.dto.EnrichableArtists
import org.taonity.artistinsightservice.openai.OpenAIClientException
import org.taonity.artistinsightservice.openai.OpenAIService
import org.taonity.artistinsightservice.persistence.genre.ArtistGenreService
import org.taonity.artistinsightservice.persistence.spotify_user_enriched_artists.SpotifyUserEnrichedArtistsService

@Service
class NewArtistEnrichmentService(
    private val artistGenreService: ArtistGenreService,
    private val gptUsageService: GptUsageService,
    private val openAIService: OpenAIService,
    private val spotifyUserEnrichedArtistsService: SpotifyUserEnrichedArtistsService,
    private val responseAttachments: ResponseAttachments,
    transactionManager: PlatformTransactionManager
) {
    private val transactionTemplate = TransactionTemplate(transactionManager)

    fun enrichNewArtists(spotifyId: String, rawArtists: List<SafeArtistObject>) : List<EnrichableArtists> {
        return rawArtists.map { rawArtist ->
            transactionTemplate.execute { transactionStatus ->
                val newArtistEnricher = NewArtistEnricher(artistGenreService, gptUsageService, openAIService, spotifyUserEnrichedArtistsService,
                    responseAttachments, transactionStatus,
                    spotifyId, rawArtist
                )

                try {
                    newArtistEnricher.enrichWithGenresIfPossible()
                } catch (e: Exception) {
                    transactionStatus.setRollbackOnly()
                    throw e
                }
            }
        }
    }
}

class NewArtistEnricher(
    private val artistGenreService: ArtistGenreService,
    private val gptUsageService: GptUsageService,
    private val openAIService: OpenAIService,
    private val spotifyUserEnrichedArtistsService: SpotifyUserEnrichedArtistsService,
    private val responseAttachments: ResponseAttachments,
    private val transactionStatus: TransactionStatus,
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
        
        val enrichmentInfo = artistGenreService.getArtistEnrichmentInfo(artistId, spotifyId)
        val dbArtistGenres = enrichmentInfo.genres
        val isLinkedToUser = enrichmentInfo.isLinkedToUser

        if (dbArtistGenres.isEmpty()) {
            return enrichUsingNewGenresFromGptIfPossible()
        }

        if (isLinkedToUser) {
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
            transactionStatus.setRollbackOnly()
            LOGGER.info { "Cannot enrich artist $artistName with id $artistId due to GPT usage limits: userUsagesConsumed=$userUsagesConsumed, globalUsageConsumed=$globalUsageConsumed" }
            if (!globalUsageConsumed) {
                responseAttachments.advisories.add(Advisory.GLOBAL_GPT_USAGES_DEPLETED)
            }
            if (!userUsagesConsumed) {
                responseAttachments.advisories.add(Advisory.USER_GPT_USAGES_DEPLETED)
            }
            return EnrichableArtists(
                rawArtist,
                genreEnriched = false,
                notEnoughUserGptUsages = userUsagesConsumed,
                notEnoughGlobalGptUsages = globalUsageConsumed
            )
        }
        val openAIProvidedGenres = try {
            openAIService.provideGenres(rawArtist.name)
        } catch (e: OpenAIClientException) {
            transactionStatus.setRollbackOnly()
            LOGGER.error(e) { }
            responseAttachments.advisories.add(Advisory.OPENAI_PROBLEM)
            return EnrichableArtists(rawArtist.copy(), false)
        }
        val enrichedArtist = rawArtist.copy(genres = openAIProvidedGenres)
        spotifyUserEnrichedArtistsService.saveEnrichedArtistsForUser(spotifyId, listOf(Pair(enrichedArtist, openAIProvidedGenres)))
        LOGGER.info { "Artis $artistName with id $artistId was provided with genres ${enrichedArtist.genres} by OpenAI call" }
        return EnrichableArtists(enrichedArtist)
    }
}