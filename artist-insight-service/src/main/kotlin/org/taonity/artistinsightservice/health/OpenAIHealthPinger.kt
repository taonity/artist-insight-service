package org.taonity.artistinsightservice.health

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Status
import org.springframework.stereotype.Component
import java.time.Duration
import kotlin.sequences.asSequence

@Component
class OpenAIHealthPinger(
    @Value("\${health.external.openai.health-url:https://api.openai.com/v1/models}")
    private val openAiHealthUrl: String,
    @Value("\${openai.api-key:}")
    private val apiKey: String,
    @Value("\${health.external.request-timeout-ms:3000}")
    requestTimeoutMs: Long
) : HttpExternalServiceHealthPinger(Duration.ofMillis(requestTimeoutMs)) {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private val objectMapper = jacksonObjectMapper()
    }

    override val name: String = "openai"

    override fun ping(): HealthCheckResult {
        if (apiKey.isBlank()) {
            return HealthCheckResult(
                Status.DOWN,
                mapOf(
                    "url" to openAiHealthUrl,
                    "message" to "OpenAI API key is not configured"
                )
            )
        }

        return performGet(
            url = openAiHealthUrl,
            headers = mapOf(
                "Authorization" to "Bearer $apiKey",
                "Accept" to "application/json"
            ),
            detailAugmentor = { response, details ->
                if (response.statusCode() in 200..299) {
                    try {
                        val root = objectMapper.readTree(response.body())
                        val modelsNode = root.path("data")
                        if (modelsNode.isArray) {
                            details["modelCount"] = modelsNode.size()
                            val firstModel = modelsNode.elements().asSequence().firstOrNull()?.path("id")?.asText()
                            if (!firstModel.isNullOrBlank()) {
                                details["firstModel"] = firstModel
                            }
                        }
                    } catch (exception: Exception) {
                        LOGGER.warn(exception) { "Failed to parse OpenAI models response" }
                        details["parsingError"] = exception.message
                    }
                }
            }
        )
    }
}
