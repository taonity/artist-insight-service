package org.taonity.artistinsightservice.persistence.spotify_user_enriched_artists

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.followings.dto.SafeArtistObject
import org.taonity.artistinsightservice.persistence.artist.ArtistEntity
import org.taonity.artistinsightservice.persistence.artist.ArtistRepository
import org.taonity.artistinsightservice.persistence.user.SpotifyUserEntity
import org.taonity.artistinsightservice.persistence.user.SpotifyUserRepository

@Service
class SpotifyUserEnrichedArtistsService(
    private val spotifyUserRepository: SpotifyUserRepository,
    private val artistRepository: ArtistRepository
) {

    @Transactional
    fun saveEnrichedArtistsForUser(
        spotifyId: String,
        artistInputs: List<Pair<SafeArtistObject, List<String>>>
    ) {
        val user: SpotifyUserEntity = spotifyUserRepository.findById(spotifyId)
            .orElseThrow { IllegalArgumentException("User with id $spotifyId not found") }

        artistInputs.forEach { (artistObject, genres) ->
            // Get or create artist with genres
            val artistEntity: ArtistEntity = artistRepository.findById(artistObject.id).orElseGet {
                ArtistEntity(artistObject.id, artistObject.name).apply { addGenres(genres) }
            }

            // For existing artists, add only new genres
            val existingGenreNames = artistEntity.genres.map { it.genre }.toSet()
            genres.filterNot { it in existingGenreNames }.forEach { artistEntity.addGenre(it) }

            // Link user to artist (cascade will persist artist and its genres)
            user.addEnrichedArtist(artistEntity)
        }

        // Single save - cascades: user -> enrichedArtists -> artist -> genres
        spotifyUserRepository.save(user)
    }
}