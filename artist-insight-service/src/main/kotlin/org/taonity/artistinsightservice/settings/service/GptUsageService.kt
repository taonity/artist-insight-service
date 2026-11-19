package org.taonity.artistinsightservice.settings.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.taonity.artistinsightservice.settings.entity.AppSettingsEntity
import org.taonity.artistinsightservice.settings.repository.AppSettingsRepository
import org.taonity.artistinsightservice.user.entity.SpotifyUserEntity
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository

@Service
class GptUsageService(
    private val spotifyUserRepository: SpotifyUserRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val runtimeSettings: RuntimeSettings,
    @Value("\${app.initial-global-gpt-usages}") private val initialGlobalGptUsages: Int
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun consumeUserUsage(spotifyId: String): Boolean {
        val user = spotifyUserRepository.findByIdForUpdate(spotifyId)
            ?: throw IllegalArgumentException("User with id $spotifyId not found")
        if (user.gptUsagesLeft <= 0) {
            return false
        }
        user.gptUsagesLeft--
        return true
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun refundUserUsage(spotifyId: String) {
        val user = spotifyUserRepository.findByIdForUpdate(spotifyId)
            ?: throw IllegalArgumentException("User with id $spotifyId not found")
        user.gptUsagesLeft++
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun consumeGlobalUsage(): Boolean {
        val settings = appSettingsRepository.findByIdForUpdate(0)
            ?: return appSettingsRepository.save(
                AppSettingsEntity(globalGptUsagesLeft = initialGlobalGptUsages - 1)
            ).let { true }

        if (settings.globalGptUsagesLeft <= 0) {
            return false
        }
        settings.globalGptUsagesLeft--
        return true
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun refundGlobalUsage() {
        val settings = appSettingsRepository.findByIdForUpdate(0) ?: return
        settings.globalGptUsagesLeft++
    }

    @Transactional
    fun topUpUserUsage(
        amountDouble: Double,
        spotifyId: String
    ) {
        val spotifyUserEntity: SpotifyUserEntity = spotifyUserRepository.findByIdForUpdate(spotifyId)
            ?: throw IllegalArgumentException("Failed to find spotify user in db by spotifyId $spotifyId")

        require(amountDouble.isFinite() && amountDouble >= 0) { "Donation must be positive and finite" }
        val gptUsagesToTopUp = java.math.BigDecimal.valueOf(amountDouble)
            .divide(runtimeSettings.effective().donationCreditPrice, 0, java.math.RoundingMode.DOWN).intValueExact()

        val before = spotifyUserEntity.gptUsagesLeft
        spotifyUserEntity.gptUsagesLeft = Math.addExact(spotifyUserEntity.gptUsagesLeft, gptUsagesToTopUp)
        val after = spotifyUserEntity.gptUsagesLeft

        LOGGER.info { "User ${spotifyUserEntity.spotifyId} topped up gpt usages by $gptUsagesToTopUp ($before -> $after)" }
    }
}