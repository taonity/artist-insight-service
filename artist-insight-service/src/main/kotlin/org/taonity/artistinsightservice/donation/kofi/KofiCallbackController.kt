package org.taonity.artistinsightservice.donation.kofi

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.taonity.artistinsightservice.persistence.user.SpotifyUserEntity
import org.taonity.artistinsightservice.persistence.user.SpotifyUserRepository

@RestController
class KofiCallbackController (
    private val spotifyUserRepository: SpotifyUserRepository
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private val objectMapper = jacksonObjectMapper()
            .registerKotlinModule()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }

    @PostMapping("/callback/kofi", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    @Transactional
    fun callback(@RequestParam("data") kofiWebhookDataJson: String) {
        LOGGER.info { "Handling /callback/kofi endpoint" }
        val kofiWebhookData: KofiWebhookData = try {
            objectMapper.readValue(kofiWebhookDataJson)
        } catch (e: Exception) {
            LOGGER.error { "Failed to parser KofiWebhookData $kofiWebhookDataJson" }
            throw e
        }
        LOGGER.info { "$kofiWebhookData" }

        val message = kofiWebhookData.message
        if (message.isEmpty()) {
            // make warn
            LOGGER.warn { "Kofi callback message is empty" }
            return
        }

        val messageWords = message.split(" ")
        val spotifyId = if (messageWords.size == 1) {
            messageWords[0]
        } else {
            val spotifyIdOpt = messageWords.stream().filter { it.length >= 26 }
                .findFirst()
            if (spotifyIdOpt.isEmpty) {
                // make warn
                LOGGER.warn { "Failed to find spotifyId in kofi callback message: $message" }
                return
            }
            spotifyIdOpt.get()
        }

        val spotifyUserEntity: SpotifyUserEntity = spotifyUserRepository.findBySpotifyIdForUpdate(spotifyId)
            ?: run {
                // make warn
                throw KofiCallbackHandlingException("Failed to find spotify user in db by spotifyId $spotifyId from message $message")
            }

        val amountString = kofiWebhookData.amount
        if (amountString.isEmpty()) {
            // make error
            throw KofiCallbackHandlingException("Kofi callback amount field is empty")
        }

        val amountDouble = try {
            amountString.toDouble()
        } catch (e: Exception) {
            LOGGER.error { "Failed to convert amount string $amountString to double" }
            throw e
        }

        //TODO: move to db config table
        val gptUsagesToTopUpDouble = amountDouble / 0.1
        val gptUsagesToTopUp = gptUsagesToTopUpDouble.toInt()

        spotifyUserEntity.gptUsagesLeft += gptUsagesToTopUp

        spotifyUserRepository.save(spotifyUserEntity)

        val before = spotifyUserEntity.gptUsagesLeft - gptUsagesToTopUp
        val after = spotifyUserEntity.gptUsagesLeft
        LOGGER.info { "User $spotifyId topped up gpt usages by $gptUsagesToTopUp ($before -> $after)" }
    }
}