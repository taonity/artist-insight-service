package org.taonity.artistinsightservice.followings

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.attachments.Advisory
import org.taonity.artistinsightservice.attachments.ResponseAttachments
import org.taonity.artistinsightservice.followings.dto.SafeArtistObject
import org.taonity.artistinsightservice.followings.dto.EnrichableArtists
import org.taonity.artistinsightservice.persistence.artist.ArtistEntity
import org.taonity.artistinsightservice.persistence.artist.ArtistRepository
import org.taonity.artistinsightservice.persistence.user.SpotifyUserService
import java.util.Objects.nonNull
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

    fun enrichUserArtists(spotifyId: String, rawArtists: List<SafeArtistObject>) : List<EnrichableArtists> {
        val enrichedUserArtists = rawArtists.map {
            enrichWithGenresFromDb(spotifyId, it)
        }
        val artistsWithNoGenresCount = enrichedUserArtists.filter { it.artistObject.genres.isEmpty() }.size
        if (artistsWithNoGenresCount > 0) {
            val gptUsagesLeft = spotifyUserService.findBySpotifyIdOrThrow(spotifyId).gptUsagesLeft
            if (gptUsagesLeft > 0) {
                val artistsAvailableToEnrichCount = min(artistsWithNoGenresCount, gptUsagesLeft)
                responseAttachments.advisories.add(Advisory.GPT_ENRICHMENT_AVAILABLE.withDetailArgs(artistsAvailableToEnrichCount.toString()))
            }
        }
        return enrichedUserArtists
    }

    // TODO: remove artists from db if spotify genres appeared
    private fun enrichWithGenresFromDb(spotifyId: String, rawArtist: SafeArtistObject): EnrichableArtists {
        if (rawArtist.genres.isEmpty()) {
            val userArtistsFromDb: List<ArtistEntity> = artistRepository.findAllByUserIdWithGenres(spotifyId)
            val dbGenres = userArtistsFromDb.find { it.artistId == rawArtist.id }
                ?.genres
                ?.map { it.genre }
            if (nonNull(dbGenres)) {
                return EnrichableArtists(rawArtist.copy(genres = dbGenres!!))
            }
        }
        return EnrichableArtists(rawArtist.copy(), false)
    }
}