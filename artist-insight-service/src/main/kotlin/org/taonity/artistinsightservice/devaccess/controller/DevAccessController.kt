package org.taonity.artistinsightservice.devaccess.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.taonity.artistinsightservice.devaccess.repository.DevAccessRepository
import org.taonity.artistinsightservice.devaccess.dto.DevAccessRequestDto
import org.taonity.artistinsightservice.devaccess.entity.DevAccessRequestEntity
import org.taonity.artistinsightservice.devaccess.service.MailService

@RestController
class DevAccessController @Autowired constructor(
    private val devAccessRepository: DevAccessRepository,
    private val mailService: MailService
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    @PostMapping("/development-access-request")
    fun submitDevelopmentAccessRequest(
        @Valid @RequestBody dto: DevAccessRequestDto,
        request: HttpServletRequest
    ) {
        LOGGER.info { "Development access request processing started" }

        val accessRequest = DevAccessRequestEntity(
            email = dto.email,
            message = dto.message,
            ipAddress = request.remoteAddr ?: "unknown",
            userAgent = request.getHeader("User-Agent")
        )

        val savedRequest = devAccessRepository.save(accessRequest)
        LOGGER.info { "Development access request saved with ID: ${savedRequest.id}" }

        mailService.sendDevelopmentAccessRequestEmail(savedRequest)
    }
}