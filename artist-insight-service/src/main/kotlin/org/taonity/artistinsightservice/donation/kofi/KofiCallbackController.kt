package org.taonity.artistinsightservice.donation.kofi

import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class KofiCallbackController (
    private val kofiCallbackService: KofiCallbackService
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    @PostMapping("/callback/kofi", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    @Transactional
    fun callback(@RequestParam("data") kofiWebhookDataJson: String) {
        LOGGER.info { "Handling /callback/kofi endpoint" }
        kofiCallbackService.topUpChatGptUsages(kofiWebhookDataJson)
    }
}