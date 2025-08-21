package org.taonity.artistinsightservice

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.persistence.app_settings.AppSettingsEntity
import org.taonity.artistinsightservice.persistence.app_settings.AppSettingsRepository
import org.taonity.artistinsightservice.persistence.user.SpotifyUserRepository

@Service
class GptUsageService(
    private val spotifyUserRepository: SpotifyUserRepository,
    private val appSettingsRepository: AppSettingsRepository,
    @Value("\${app.initial-global-gpt-usages}") private val initialGlobalGptUsages: Int
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

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
    fun consumeGlobalUsage(): Boolean {
        val settings = appSettingsRepository.findByIdForUpdate(0)
            ?: AppSettingsEntity(globalGptUsagesLeft = initialGlobalGptUsages)
        if (settings.globalGptUsagesLeft <= 0) {
            return false
        }
        settings.globalGptUsagesLeft--
        appSettingsRepository.save(settings)
        return true
    }
}
