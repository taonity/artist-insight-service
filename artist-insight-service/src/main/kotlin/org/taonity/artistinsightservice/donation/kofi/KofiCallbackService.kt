package org.taonity.artistinsightservice.donation.kofi

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.persistence.GptUsageService

@Service
class KofiCallbackService(
    private val gptUsageService: GptUsageService,
    @Value("\${kofi.verification-token}")
    private val kofiVerificationToken: String
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private val objectMapper = jacksonObjectMapper()
            .registerKotlinModule()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }
    
    fun topUpChatGptUsages(kofiWebhookDataJson: String) {
        val kofiWebhookData: KofiWebhookData = readKofiWebhookData(kofiWebhookDataJson)

        val message = kofiWebhookData.message

        if (!kofiWebhookAuthenticated(kofiWebhookData))
            return

        LOGGER.info { "Kofi webhook received with id ${kofiWebhookData.messageId}, message $message" }

        val spotifyId = findSpotifyIdInMessage(message)
            ?: return

        val amountDouble = parseDonationAmount(kofiWebhookData.amount)

        gptUsageService.topUpUserUsage(amountDouble, spotifyId)
    }


    private fun parseDonationAmount(amountString: String): Double {
        if (amountString.isEmpty()) {
            throw KofiCallbackHandlingException("Kofi callback amount field is empty")
        }

        return try {
            amountString.toDouble()
        } catch (e: Exception) {
            LOGGER.error { "Failed to convert amount string $amountString to double" }
            throw e
        }
    }

    private fun findSpotifyIdInMessage(message: String): String? {
        if (message.isEmpty()) {
            LOGGER.warn { "Kofi callback message is empty" }
            return null
        }

        val messageWords = message.split(" ")
        return if (messageWords.size == 1) {
            messageWords[0]
        } else {
            val spotifyIdOpt = messageWords.stream().filter { it.length >= 26 }
                .findFirst()
            if (spotifyIdOpt.isEmpty) {
                LOGGER.warn { "Failed to find spotifyId in kofi callback message: $message" }
                return null
            }
            spotifyIdOpt.get()
        }
    }

    private fun kofiWebhookAuthenticated(kofiWebhookData: KofiWebhookData): Boolean {
        if (kofiWebhookData.verificationToken == kofiVerificationToken) {
            return true
        }
        LOGGER.warn { """
                Kofi webhook rejected with id ${kofiWebhookData.messageId}, message ${kofiWebhookData.message} 
                verification token ${kofiWebhookData.verificationToken}
            """.trimIndent() }
        return false
    }

    private fun readKofiWebhookData(kofiWebhookDataJson: String): KofiWebhookData {
        val kofiWebhookData: KofiWebhookData = try {
            objectMapper.readValue(kofiWebhookDataJson)
        } catch (e: Exception) {
            LOGGER.error { "Failed to parser KofiWebhookData $kofiWebhookDataJson" }
            throw e
        }
        return kofiWebhookData
    }
}