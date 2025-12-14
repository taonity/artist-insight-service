package org.taonity.artistinsightservice.settings.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.settings.entity.AppSettingsEntity
import org.taonity.artistinsightservice.settings.repository.AppSettingsRepository
import org.taonity.artistinsightservice.user.entity.SpotifyUserEntity
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository

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
        val user = spotifyUserRepository.findByIdForUpdate(spotifyId)
            ?: throw IllegalArgumentException("User with id $spotifyId not found")
        if (user.gptUsagesLeft <= 0) {
            return false
        }
        user.gptUsagesLeft--
        return true
    }

    @Transactional
    fun consumeGlobalUsage(): Boolean {
        val settings = appSettingsRepository.findByIdForUpdate(0)

        if (settings == null) {
            val newSettings = AppSettingsEntity(globalGptUsagesLeft = initialGlobalGptUsages - 1)
            appSettingsRepository.save(newSettings)
            return true
        }

        if (settings.globalGptUsagesLeft <= 0) {
            return false
        }
        settings.globalGptUsagesLeft--
        return true
    }

    @Transactional
    fun topUpUserUsage(
        amountDouble: Double,
        spotifyId: String
    ) {
        val spotifyUserEntity: SpotifyUserEntity = spotifyUserRepository.findByIdForUpdate(spotifyId)
            ?: throw IllegalArgumentException("Failed to find spotify user in db by spotifyId $spotifyId")

        //TODO: move to db config table
        val gptUsagesToTopUpDouble = amountDouble / 0.1
        val gptUsagesToTopUp = gptUsagesToTopUpDouble.toInt()

        spotifyUserEntity.gptUsagesLeft += gptUsagesToTopUp

        val before = spotifyUserEntity.gptUsagesLeft - gptUsagesToTopUp
        val after = spotifyUserEntity.gptUsagesLeft
        LOGGER.info { "User ${spotifyUserEntity.spotifyId} topped up gpt usages by $gptUsagesToTopUp ($before -> $after)" }
    }
}