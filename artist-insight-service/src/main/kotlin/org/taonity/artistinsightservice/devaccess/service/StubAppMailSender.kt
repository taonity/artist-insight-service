package org.taonity.artistinsightservice.devaccess.service

import mu.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.mail.SimpleMailMessage
import org.springframework.stereotype.Service

@Service
@Profile("stub-mail")
class StubAppMailSender : AppMailSender {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    override fun send(message: SimpleMailMessage) {
        LOGGER.info {
            "Stub mail accepted: to=${message.to?.joinToString() ?: ""}, cc=${message.cc?.joinToString() ?: ""}, subject=${message.subject ?: ""}"
        }
    }
}