package org.taonity.artistinsightservice.openai

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.openai.client.OpenAIClient
import com.openai.models.ChatModel
import com.openai.models.chat.completions.ChatCompletionCreateParams
import com.openai.models.chat.completions.ChatCompletionMessageParam
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam
import com.openai.models.chat.completions.ChatCompletionUserMessageParam
import mu.KotlinLogging
import org.springframework.boot.actuate.health.Status
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.health.HealthCheckResult
import org.taonity.artistinsightservice.utils.hasCause
import java.io.InterruptedIOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.util.LinkedHashMap
import kotlin.sequences.asSequence

@Service
class OpenAIService(
    private val openAIClient: OpenAIClient
) {

    companion object {
        private val objectMapper = jacksonObjectMapper()
        private val LOGGER = KotlinLogging.logger {}
        private const val MAX_BODY_PREVIEW_CHARS = 160
    }

    fun provideGenres(artistName: String): List<String> {
        val systemPrompt = """
            You are a music expert. When given an artist or band's name, 
            you return their genres as a JSON array.
            Return only the JSON array. No explanation.
        """.trimIndent()

        // TODO: handle prompt injection
        val userPrompt = "Provide the main genres of the artist \"$artistName\"."

        val request = chatCompletionCreateParams(systemPrompt, userPrompt)

        val response = try {
            openAIClient.chat().completions().create(request)
        } catch (e: Exception) {
            if (e.hasCause(InterruptedIOException::class.java)) {
                throw OpenAITimeoutException("OpenAI timed out", e)
            }
            throw OpenAIClientException("OpenAI completion threw an exception", e)
        }

        val content = response.choices()
            .firstOrNull()
            ?.message()
            ?.content()
            ?.orElse(null)
            ?: throw OpenAIClientException("No genre content returned from OpenAI.")

        val genres: List<String> = try {
            objectMapper.readValue(content)
        } catch (e: Exception) {
            throw OpenAIClientException("Failed to parse genres JSON: $content", e)
        }

        return genres
    }

    fun checkAvailability(url: String, apiKey: String, requestTimeout: Duration): HealthCheckResult {
        val httpClient = HttpClient.newBuilder()
            .connectTimeout(requestTimeout)
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(requestTimeout)
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "application/json")
            .GET()
            .build()
        val start = Instant.now()

        return try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            val statusCode = response.statusCode()
            val details = LinkedHashMap<String, Any?>()
            details["url"] = url
            details["statusCode"] = statusCode
            details["responseTimeMs"] = elapsedMs

            val healthy = statusCode in 200..299

            if (healthy) {
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
            } else {
                details["responsePreview"] = response.body().take(MAX_BODY_PREVIEW_CHARS)
            }

            HealthCheckResult(
                status = if (healthy) Status.UP else Status.DOWN,
                details = details
            )
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

    private fun chatCompletionCreateParams(
        systemPrompt: String,
        userPrompt: String
    ) = ChatCompletionCreateParams.builder()
        .model(ChatModel.GPT_4)
        .messages(
            listOf(
                ChatCompletionMessageParam.ofSystem(
                    ChatCompletionSystemMessageParam.builder().content(systemPrompt).build()
                ),
                ChatCompletionMessageParam.ofUser(
                    ChatCompletionUserMessageParam.builder().content(userPrompt).build()
                )
            )
        )
        .build()
}
