package org.taonity.artistinsightservice.persistence.user

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.mvc.security.SpotifyUserPrincipal
import java.util.Objects.isNull

@Service
class SpotifyUserService(private val spotifyUserRepository: SpotifyUserRepository) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    fun createOrUpdateUser(spotifyUserPrincipal: SpotifyUserPrincipal, tokenValue: String) {
        val savedSpotifyUserEntity: SpotifyUserEntity? =
            spotifyUserRepository.findBySpotifyId(spotifyUserPrincipal.getSpotifyId())

        val spotifyUserEntityToSave = if (isNull(savedSpotifyUserEntity)) {
            LOGGER.info { "New user created" }
            SpotifyUserEntity(
                spotifyUserPrincipal.getSpotifyId(),
                spotifyUserPrincipal.getDisplayName(),
                tokenValue,
                2
            )
        } else {
            LOGGER.info { "Existing user updated" }
            savedSpotifyUserEntity!!.updateDetails(spotifyUserPrincipal.getDisplayName(), tokenValue)
        }

        spotifyUserRepository.save(spotifyUserEntityToSave)
        LOGGER.info { "User $spotifyUserEntityToSave saved" }
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