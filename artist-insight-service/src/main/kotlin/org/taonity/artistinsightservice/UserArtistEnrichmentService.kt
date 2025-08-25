package org.taonity.artistinsightservice

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.mvc.EnrichableArtists
import org.taonity.artistinsightservice.persistence.artist.ArtistEntity
import org.taonity.artistinsightservice.persistence.artist.ArtistRepository
import java.util.Objects.nonNull

@Service
class UserArtistEnrichmentService(
    private val artistRepository: ArtistRepository,
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    fun enrichUserArtists(spotifyId: String, rawArtists: List<SafeArtistObject>) : List<EnrichableArtists> {
        return rawArtists.map {
            enrichWithGenresFromDb(spotifyId, it)
        }
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