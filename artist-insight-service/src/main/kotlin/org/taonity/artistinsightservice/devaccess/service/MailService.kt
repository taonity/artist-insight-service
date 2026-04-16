package org.taonity.artistinsightservice.devaccess.service

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.SimpleMailMessage
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.devaccess.entity.DevAccessRequestEntity
import org.taonity.artistinsightservice.devaccess.entity.DevAccessRequestStatus
import org.taonity.artistinsightservice.devaccess.repository.DevAccessRepository

@ConditionalOnProperty(
    value = ["app.dev-access.enabled"],
    havingValue = "true",
    matchIfMissing = false)
@Service
class MailService(
    private val mailSender: AppMailSender,
    private val devAccessRepository: DevAccessRepository
) {
    @Value("\${app.dev-access.admin-email}")
    private lateinit var adminEmail: String

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    @Async
    fun sendDevelopmentAccessRequestEmail(request: DevAccessRequestEntity) {
        try {
            val emailSubject = "Development Access Request #${request.id}"
            val emailBody = buildEmailBody(request)

            val message = SimpleMailMessage()
            message.setTo(adminEmail)
            message.setCc(request.email)
            message.subject = emailSubject
            message.text = emailBody

            mailSender.send(message)

            request.status = DevAccessRequestStatus.SENT
            devAccessRepository.updateStatus(request.id, DevAccessRequestStatus.SENT)
            LOGGER.info { "Email sent successfully for request ID: ${request.id}" }
        } catch (e: Exception) {
            request.status = DevAccessRequestStatus.FAILED
            devAccessRepository.updateStatus(request.id, DevAccessRequestStatus.FAILED)
            LOGGER.error(e) { "Failed to send email for request ID: ${request.id}" }
        }
    }

    private fun buildEmailBody(request: DevAccessRequestEntity): String {
        return buildString {
            appendLine("New development access request:")
            appendLine("")
            appendLine("Request ID: ${request.id}")
            appendLine("Email: ${request.email}")
            appendLine("IP Address: ${request.ipAddress}")
            appendLine("User Agent: ${request.userAgent}")
            appendLine("Timestamp: ${request.createdAt}")
            appendLine("")
            appendLine("Message:")
            appendLine(request.message ?: "No message provided.")
        }
    }
}