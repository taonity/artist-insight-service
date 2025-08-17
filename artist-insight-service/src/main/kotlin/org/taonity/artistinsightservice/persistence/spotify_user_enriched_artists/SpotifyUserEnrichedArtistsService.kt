package org.taonity.artistinsightservice.persistence.spotify_user_enriched_artists

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.SafeArtistObject
import org.taonity.artistinsightservice.persistence.artist.ArtistEntity
import org.taonity.artistinsightservice.persistence.artist.ArtistRepository
import org.taonity.artistinsightservice.persistence.genre.ArtistGenreEntity
import org.taonity.artistinsightservice.persistence.genre.ArtistGenreId
import org.taonity.artistinsightservice.persistence.genre.ArtistGenreRepository
import org.taonity.artistinsightservice.persistence.user.SpotifyUserRepository
import org.taonity.spotify.model.ArtistObject

@Service
class SpotifyUserEnrichedArtistsService(
    private val spotifyUserRepository: SpotifyUserRepository,
    private val artistRepository: ArtistRepository,
    private val artistGenreRepository: ArtistGenreRepository,
    private val spotifyUserEnrichedArtistsRepository: SpotifyUserEnrichedArtistsRepository
) {
    @Transactional
    fun saveEnrichedArtistsForUser(
        spotifyId: String,
        artistInputs: List<Pair<SafeArtistObject, List<String>>> // artist name + list of genres
    ) {
        val user = spotifyUserRepository.findBySpotifyId(spotifyId)
            ?: throw IllegalArgumentException("User with id $spotifyId not found")

        artistInputs.forEach { (artistObject, genres) ->
            // Create or fetch the artist
            val artistEntity = artistRepository.findById(artistObject.id).orElseGet {
                val newArtistEntity = ArtistEntity(artistObject.id, artistObject.name)
                artistRepository.save(newArtistEntity)
            }

            // Save genres
            genres.forEach { genre ->
                val genreId = ArtistGenreId(artistEntity.artistName, genre)
                if (!artistGenreRepository.existsById(genreId)) {
                    artistGenreRepository.save(ArtistGenreEntity(artistEntity, genre))
                }
            }

            // Add to enriched artists if not already linked
            val enrichedId = SpotifyUserEnrichedArtistsId(user.spotifyId, artistEntity.artistName)
            if (!spotifyUserEnrichedArtistsRepository.existsById(enrichedId)) {
                spotifyUserEnrichedArtistsRepository.save(SpotifyUserEnrichedArtistsEntity(user, artistEntity))
            }
        }
    }
}