package org.taonity.artistinsightservice.persistence.user

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.mvc.security.SpotifyUserPrincipal
import java.util.Objects.isNull

@Service
class SpotifyUserService(
    private val spotifyUserRepository: SpotifyUserRepository,
    @Value("\${app.initial-gpt-usages}") private val initialGptUsages: Int
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
                initialGptUsages
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

    private fun maskSecret(secret: String): String {
        val sb = StringBuilder(secret)
        for (i in 2 until secret.length - 2) {
            sb.setCharAt(i, '*')
        }
        return sb.toString()
    }

    fun decrementGptUsagesIfLeft(spotifyId: String): Boolean {
        val spotifyUser = spotifyUserRepository.findBySpotifyId(spotifyId)
        if (isNull(spotifyUser)) {
            throw RuntimeException("Failed to manage GPT usages, user with spotifyId [$spotifyId] not found in DB")
        }
        if (spotifyUser!!.gptUsagesLeft > 0) {
            spotifyUser.gptUsagesLeft--
            spotifyUserRepository.save(spotifyUser)
            return true
        }
        return false
    }
}