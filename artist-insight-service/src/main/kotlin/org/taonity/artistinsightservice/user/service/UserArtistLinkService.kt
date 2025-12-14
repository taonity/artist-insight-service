package org.taonity.artistinsightservice.user.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.artist.dto.SafeArtistObject
import org.taonity.artistinsightservice.artist.entity.ArtistEntity
import org.taonity.artistinsightservice.artist.repository.ArtistRepository
import org.taonity.artistinsightservice.user.entity.SpotifyUserEntity
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository

@Service
class UserArtistLinkService(
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
            val artistEntity: ArtistEntity = artistRepository.findById(artistObject.id).orElseGet {
                ArtistEntity(artistObject.id, artistObject.name).apply { addGenres(genres) }
            }

            val existingGenreNames = artistEntity.genres
                .map { it.genre }
                .toSet()
            genres.filterNot { it in existingGenreNames }
                .forEach { artistEntity.addGenre(it) }

            user.addEnrichedArtist(artistEntity)
        }
    }
}
