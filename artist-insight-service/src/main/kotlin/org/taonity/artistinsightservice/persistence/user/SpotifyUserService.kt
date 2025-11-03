package org.taonity.artistinsightservice.persistence.user

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.mvc.security.SpotifyUserPrincipal
import org.taonity.artistinsightservice.persistence.spotify_user_enriched_artists.SpotifyUserEnrichedArtistsRepository
import java.util.Objects.isNull

@Service
class SpotifyUserService(
    private val spotifyUserRepository: SpotifyUserRepository,
    private val spotifyUserEnrichedArtistsRepository: SpotifyUserEnrichedArtistsRepository,
    @Value("\${app.initial-user-gpt-usages}") private val initialUserGptUsages: Int
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    fun findBySpotifyIdOrThrow(spotifyId: String): SpotifyUserEntity {
        return spotifyUserRepository.findBySpotifyId(spotifyId)
            ?: throw RuntimeException("SpotifyUserEntity with spotifyId $spotifyId was not found in DB")
    }

    fun findBySpotifyId(spotifyId: String): SpotifyUserEntity? {
        return spotifyUserRepository.findBySpotifyId(spotifyId)
    }

    fun createOrUpdateUser(spotifyUserPrincipal: SpotifyUserPrincipal, tokenValue: String) {
        val maskedTokenValue = maskSecret(tokenValue)
        val foundSpotifyUser = findBySpotifyId(spotifyUserPrincipal.getSpotifyId())
        val spotifyUserEntityToSave: SpotifyUserEntity = if (isNull(foundSpotifyUser)) {
            LOGGER.info { "New user created" }
            SpotifyUserEntity(
                spotifyUserPrincipal.getSpotifyId(),
                spotifyUserPrincipal.getDisplayName(),
                maskedTokenValue,
                initialUserGptUsages
            )
        } else {
            LOGGER.info { "Existing user updated" }
            findBySpotifyIdOrThrow(spotifyUserPrincipal.getSpotifyId()).apply {
                updateDetails(spotifyUserPrincipal.getDisplayName(), maskedTokenValue)
            }
        }

        spotifyUserRepository.save(spotifyUserEntityToSave)
        LOGGER.info { "User $spotifyUserEntityToSave saved" }
    }

    @Transactional
    fun deleteUserBySpotifyId(spotifyId: String) {
        if (!spotifyUserRepository.existsById(spotifyId)) {
            LOGGER.warn { "Attempted to delete non-existent user with spotifyId $spotifyId" }
            return
        }

        LOGGER.info { "Deleting user data for spotifyId $spotifyId" }
        spotifyUserEnrichedArtistsRepository.deleteAllByUserSpotifyId(spotifyId)
        spotifyUserRepository.deleteById(spotifyId)
    }

    private fun maskSecret(secret: String): String {
        val sb = StringBuilder(secret)
        for (i in 2 until secret.length - 2) {
            sb.setCharAt(i, '*')
        }
        return sb.toString()
    }
}