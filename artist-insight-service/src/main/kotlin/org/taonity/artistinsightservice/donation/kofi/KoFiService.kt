package org.taonity.artistinsightservice.donation.kofi

import mu.KotlinLogging
import org.springframework.boot.actuate.health.Status
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.health.HealthCheckResult
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.Instant
import java.util.LinkedHashMap

@Service
class KoFiService {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private const val MAX_BODY_PREVIEW_CHARS = 160
    }

    fun checkAvailability(url: String, requestTimeout: Duration): HealthCheckResult {
        val httpClient = HttpClient.newBuilder()
            .connectTimeout(requestTimeout)
            .build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(requestTimeout)
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

            val healthy = statusCode in 200..399

            if (statusCode in 300..399) {
                val redirect = response.headers().firstValue("Location").orElse(null)
                if (!redirect.isNullOrBlank()) {
                    details["redirectLocation"] = redirect
                }
            }

            if (!healthy) {
                details["responsePreview"] = response.body().take(MAX_BODY_PREVIEW_CHARS)
            }

            HealthCheckResult(
                status = if (healthy) Status.UP else Status.DOWN,
                details = details
            )
        } catch (exception: Exception) {
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            LOGGER.warn(exception) { "Ko-fi availability check failed for $url" }
            val details = mapOf(
                "url" to url,
                "responseTimeMs" to elapsedMs,
                "error" to (exception.message ?: exception::class.simpleName)
            )
            HealthCheckResult(Status.DOWN, details)
        }
    }
}
