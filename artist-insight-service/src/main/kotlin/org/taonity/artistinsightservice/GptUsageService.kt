package org.taonity.artistinsightservice

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.persistence.app_settings.AppSettingsRepository
import org.taonity.artistinsightservice.persistence.user.SpotifyUserRepository

@Service
class GptUsageService(
    private val spotifyUserRepository: SpotifyUserRepository,
    private val appSettingsRepository: AppSettingsRepository
) {
    @Transactional
    fun consumeUserUsage(spotifyId: String): Boolean {
        val user = spotifyUserRepository.findBySpotifyIdForUpdate(spotifyId)
            ?: throw IllegalArgumentException("User with id $spotifyId not found")
        if (user.gptUsagesLeft <= 0) {
            return false
        }
        user.gptUsagesLeft--
        spotifyUserRepository.save(user)
        return true
    }

    @Transactional
    fun consumeUserAndGlobalUsage(spotifyId: String): Boolean {
        val user = spotifyUserRepository.findBySpotifyIdForUpdate(spotifyId)
            ?: throw IllegalArgumentException("User with id $spotifyId not found")
        val settings = appSettingsRepository.findByIdForUpdate(0)
            ?: throw IllegalStateException("Application settings not found")
        if (user.gptUsagesLeft <= 0 || settings.globalGptUsagesLeft <= 0) {
            return false
        }
        user.gptUsagesLeft--
        settings.globalGptUsagesLeft--
        spotifyUserRepository.save(user)
        appSettingsRepository.save(settings)
        return true
    }
}
