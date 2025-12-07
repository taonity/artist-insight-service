package org.taonity.artistinsightservice.persistence.user

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.followings.dto.SafeArtistObject
import org.taonity.artistinsightservice.persistence.artist.ArtistEntity
import org.taonity.artistinsightservice.persistence.artist.ArtistRepository

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

            val existingGenreNames = artistEntity.genres.map { it.genre }.toSet()
            genres.filterNot { it in existingGenreNames }.forEach { artistEntity.addGenre(it) }

            user.addEnrichedArtist(artistEntity)
        }

        spotifyUserRepository.save(user)
    }
}
