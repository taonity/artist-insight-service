package org.taonity.artistinsightservice.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Status
import org.springframework.stereotype.Component
import org.taonity.artistinsightservice.openai.OpenAIService
import java.time.Duration

@Component
class OpenAIHealthPinger(
    private val openAIService: OpenAIService,
    @Value("\${health.external.openai.health-url:https://api.openai.com/v1/models}")
    private val openAiHealthUrl: String,
    @Value("\${openai.api-key:}")
    private val apiKey: String,
    @Value("\${health.external.request-timeout-ms:3000}")
    requestTimeoutMs: Long
) : ExternalServiceHealthPinger {

    override val name: String = "openai"

    override fun ping(): HealthCheckResult {
        if (apiKey.isBlank()) {
            return HealthCheckResult(
                Status.DOWN,
                mapOf(
                    "url" to openAiHealthUrl,
                    "message" to "OpenAI API key is not configured",
                )
            )
        }

        return openAIService.checkAvailability(
            url = openAiHealthUrl,
            apiKey = apiKey,
            requestTimeout = Duration.ofMillis(requestTimeoutMs)
        )
    }
}
