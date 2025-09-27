package org.taonity.artistinsightservice.health

import mu.KotlinLogging
import org.springframework.boot.actuate.health.Status
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.util.LinkedHashMap

abstract class HttpExternalServiceHealthPinger(
    private val requestTimeout: Duration
) : ExternalServiceHealthPinger {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private const val MAX_BODY_PREVIEW_CHARS = 160
    }

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(requestTimeout)
        .build()

    protected fun performGet(
        url: String,
        headers: Map<String, String> = emptyMap(),
        healthyStatusPredicate: (Int) -> Boolean = { statusCode -> statusCode in 200..299 },
        detailAugmentor: (HttpResponse<String>, MutableMap<String, Any?>) -> Unit = { _, _ -> }
    ): HealthCheckResult {
        val requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(requestTimeout)
            .GET()

        headers.forEach { (key, value) ->
            requestBuilder.header(key, value)
        }

        val request = requestBuilder.build()
        val start = Instant.now()

        return try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            val details = LinkedHashMap<String, Any?>()
            details["url"] = url
            details["statusCode"] = response.statusCode()
            details["responseTimeMs"] = elapsedMs

            val healthy = healthyStatusPredicate(response.statusCode())

            if (!healthy) {
                val bodyPreview = response.body().take(MAX_BODY_PREVIEW_CHARS)
                details["responsePreview"] = bodyPreview
            }

            detailAugmentor(response, details)

            HealthCheckResult(
                status = if (healthy) Status.UP else Status.DOWN,
                details = details
            )
        } catch (exception: Exception) {
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            LOGGER.warn(exception) { "Health check failed for $url" }
            val details = mapOf(
                "url" to url,
                "responseTimeMs" to elapsedMs,
                "error" to (exception.message ?: exception::class.simpleName)
            )
            HealthCheckResult(Status.DOWN, details)
        }
    }
}
