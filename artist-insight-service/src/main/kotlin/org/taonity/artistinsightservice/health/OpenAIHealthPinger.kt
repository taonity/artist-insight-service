package org.taonity.artistinsightservice.health

import mu.KotlinLogging
import org.springframework.boot.actuate.health.Status
import org.springframework.stereotype.Component
import org.taonity.artistinsightservice.openai.OpenAIService
import java.time.Duration
import java.time.Instant

@Component
class OpenAIHealthPinger(
    private val openAIService: OpenAIService,
) : ExternalServiceHealthPinger {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    override val name: String = "openai"

    override fun ping(): HealthCheckResult {
        val url = "https://api.openai.com/v1/models"
        val start = Instant.now()
        return try {
            val models = openAIService.getModels()
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            val details = LinkedHashMap<String, Any?>()
            details["url"] = url
            details["responseTimeMs"] = elapsedMs

            try {
                details["modelCount"] = models.size
                details["firstModel"] = models.firstOrNull()
            } catch (exception: Exception) {
                LOGGER.warn(exception) { "Failed to parse OpenAI models response" }
                details["parsingError"] = exception.message
            }

            HealthCheckResult(status = Status.UP, details = details)
        } catch (exception: Exception) {
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            LOGGER.warn(exception) { "OpenAI availability check failed for $url" }
            val details = mapOf(
                "url" to url,
                "responseTimeMs" to elapsedMs,
                "error" to (exception.message ?: exception::class.simpleName)
            )
            HealthCheckResult(Status.DOWN, details)
        }
    }
}
