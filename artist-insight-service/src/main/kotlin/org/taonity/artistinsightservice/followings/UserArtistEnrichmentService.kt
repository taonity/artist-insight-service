package org.taonity.artistinsightservice.followings

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.attachments.Advisory
import org.taonity.artistinsightservice.attachments.ResponseAttachments
import org.taonity.artistinsightservice.followings.dto.SafeArtistObject
import org.taonity.artistinsightservice.followings.dto.EnrichableArtists
import org.taonity.artistinsightservice.persistence.artist.ArtistRepository
import org.taonity.artistinsightservice.persistence.user.SpotifyUserService
import kotlin.math.min

@Service
class UserArtistEnrichmentService(
    private val artistRepository: ArtistRepository,
    private val spotifyUserService: SpotifyUserService,
    private val responseAttachments: ResponseAttachments
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    fun enrichUserArtists(spotifyId: String, rawArtists: List<SafeArtistObject>): List<EnrichableArtists> {
        val artistIds = rawArtists.map { it.id }
        val dbArtistGenres: Map<String, List<String>> = artistRepository.findByUserIdAndArtistIdsWithGenres(spotifyId, artistIds)
            .associate { it.artistId to it.genres.map { g -> g.genre } }

        val enrichedUserArtists = rawArtists.map { rawArtist ->
            enrichWithGenresFromDb(rawArtist, dbArtistGenres)
        }

        val artistsWithNoGenresCount = enrichedUserArtists.count { it.artistObject.genres.isEmpty() }
        if (artistsWithNoGenresCount > 0) {
            // TODO: redundant db call - try to keep it in one context with oauth2 pers service
            val gptUsagesLeft = spotifyUserService.findBySpotifyIdOrThrow(spotifyId).gptUsagesLeft
            if (gptUsagesLeft > 0) {
                val artistsAvailableToEnrichCount = min(artistsWithNoGenresCount, gptUsagesLeft)
                responseAttachments.advisories.add(Advisory.GPT_ENRICHMENT_AVAILABLE.withDetailArgs(artistsAvailableToEnrichCount.toString()))
            }
        }
        return enrichedUserArtists
    }

    // TODO: remove artists from db if spotify genres appeared
    private fun enrichWithGenresFromDb(rawArtist: SafeArtistObject, dbArtistGenres: Map<String, List<String>>): EnrichableArtists {
        if (rawArtist.genres.isEmpty()) {
            val dbGenres = dbArtistGenres[rawArtist.id]
            if (dbGenres != null) {
                return EnrichableArtists(rawArtist.copy(genres = dbGenres))
            }
        }
        return EnrichableArtists(rawArtist.copy(), false)
    }
}