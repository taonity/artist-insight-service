package org.taonity.artistinsightservice.user.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.security.principal.SpotifyUserPrincipal
import org.taonity.artistinsightservice.user.entity.SpotifyUserEntity
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository
import org.taonity.artistinsightservice.user.repository.UserArtistLinkRepository

@Service
class SpotifyUserService(
    private val spotifyUserRepository: SpotifyUserRepository,
    private val userArtistLinkRepository: UserArtistLinkRepository,
    @Value("\${app.initial-user-gpt-usages}") private val initialUserGptUsages: Int
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    fun findBySpotifyIdOrThrow(spotifyId: String): SpotifyUserEntity {
        return spotifyUserRepository.findById(spotifyId)
            .orElseThrow { RuntimeException("SpotifyUserEntity with spotifyId $spotifyId was not found in DB") }
    }

    fun findBySpotifyId(spotifyId: String): SpotifyUserEntity? {
        return spotifyUserRepository.findById(spotifyId).orElse(null)
    }

    @Transactional
    fun createOrUpdateUser(spotifyUserPrincipal: SpotifyUserPrincipal, tokenValue: String) {
        val maskedTokenValue = maskSecret(tokenValue)
        val foundSpotifyUser = findBySpotifyId(spotifyUserPrincipal.getSpotifyId())

        if (foundSpotifyUser == null) {
            val newUser = SpotifyUserEntity(
                spotifyUserPrincipal.getSpotifyId(),
                spotifyUserPrincipal.getDisplayName(),
                maskedTokenValue,
                initialUserGptUsages
            )
            spotifyUserRepository.save(newUser)
            LOGGER.info { "User $newUser to be saved" }
        } else {
            foundSpotifyUser.updateDetails(spotifyUserPrincipal.getDisplayName(), maskedTokenValue)
            LOGGER.info { "User to be $foundSpotifyUser updated" }
        }
    }

    @Transactional
    fun deleteUserBySpotifyId(spotifyId: String) {
        if (!spotifyUserRepository.existsById(spotifyId)) {
            LOGGER.warn { "Attempted to delete non-existent user with spotifyId $spotifyId" }
            return
        }

        LOGGER.info { "Deleting user data for spotifyId $spotifyId" }
        userArtistLinkRepository.deleteAllByUserSpotifyId(spotifyId)
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