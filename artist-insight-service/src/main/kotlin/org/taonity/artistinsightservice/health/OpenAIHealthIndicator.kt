package org.taonity.artistinsightservice.health

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.stereotype.Component
import org.taonity.artistinsightservice.integration.openai.service.OpenAIService
import java.time.Duration
import java.time.Instant

@Component("openai")
class OpenAIHealthIndicator(
    private val openAIService: OpenAIService,
    @Value("\${openai.base-url}")
    private val openAiBaseUrl: String,
) : HealthIndicator {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    override fun health(): Health {
        val url = "$openAiBaseUrl/models"
        val start = Instant.now()
        return try {
            val models = openAIService.getModels()
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            val builder = Health.up()
                .withDetail("url", url)
                .withDetail("responseTimeMs", elapsedMs)
            try {
                builder.withDetail("modelCount", models.size)
                models.firstOrNull()?.let { builder.withDetail("firstModel", it) }
            } catch (exception: Exception) {
                LOGGER.warn(exception) { "Failed to parse OpenAI models response" }
                builder.withDetail("parsingError", exception.message ?: "unknown")
            }
            builder.build()
        } catch (exception: Exception) {
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            LOGGER.warn { "OpenAI availability check failed for $url" }
            Health.down()
                .withDetail("url", url)
                .withDetail("responseTimeMs", elapsedMs)
                .withDetail("error", exception.message ?: exception::class.simpleName ?: "unknown")
                .build()
        }
    }
}
