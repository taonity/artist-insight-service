package org.taonity.artistinsightservice.followings.service

import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.interceptor.TransactionAspectSupport
import org.taonity.artistinsightservice.advisory.Advisory
import org.taonity.artistinsightservice.settings.service.GptUsageService
import org.taonity.artistinsightservice.advisory.ResponseAttachments
import org.taonity.artistinsightservice.artist.dto.SafeArtistObject
import org.taonity.artistinsightservice.artist.dto.EnrichableArtists
import org.taonity.artistinsightservice.integration.openai.exception.OpenAIClientException
import org.taonity.artistinsightservice.integration.openai.service.OpenAIService
import org.taonity.artistinsightservice.artist.service.ArtistEnrichmentService
import org.taonity.artistinsightservice.user.service.UserArtistLinkService

@Component
class NewArtistEnricherFactory(
    private val artistEnrichmentService: ArtistEnrichmentService,
    private val gptUsageService: GptUsageService,
    private val openAIService: OpenAIService,
    private val userArtistLinkService: UserArtistLinkService,
    private val responseAttachments: ResponseAttachments
) {
    @Transactional
    fun createAndEnrich(spotifyId: String, rawArtist: SafeArtistObject): EnrichableArtists {
        return NewArtistEnricher(
            spotifyId, rawArtist,
            artistEnrichmentService, gptUsageService, openAIService,
            userArtistLinkService, responseAttachments
        ).enrichWithGenresIfPossible()
    }
}

class NewArtistEnricher(
    private val spotifyId: String,
    private val rawArtist: SafeArtistObject,
    private val artistEnrichmentService: ArtistEnrichmentService,
    private val gptUsageService: GptUsageService,
    private val openAIService: OpenAIService,
    private val userArtistLinkService: UserArtistLinkService,
    private val responseAttachments: ResponseAttachments
) {
    private val artistId: String = rawArtist.id
    private val artistName: String = rawArtist.name

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    fun enrichWithGenresIfPossible(): EnrichableArtists {
        if (rawArtist.genres.isNotEmpty()) {
            return EnrichableArtists(rawArtist.copy(), false)
        }
        
        val enrichmentInfo = artistEnrichmentService.getArtistEnrichmentInfo(artistId, spotifyId)
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
        userArtistLinkService.saveEnrichedArtistsForUser(spotifyId, listOf(Pair(enrichedArtist, dbArtistGenres)))
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
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
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
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
            LOGGER.error(e) { }
            responseAttachments.advisories.add(Advisory.OPENAI_PROBLEM)
            return EnrichableArtists(rawArtist.copy(), false)
        }
        val enrichedArtist = rawArtist.copy(genres = openAIProvidedGenres)
        userArtistLinkService.saveEnrichedArtistsForUser(spotifyId, listOf(Pair(enrichedArtist, openAIProvidedGenres)))
        LOGGER.info { "Artis $artistName with id $artistId was provided with genres ${enrichedArtist.genres} by OpenAI call" }
        return EnrichableArtists(enrichedArtist)
    }
}