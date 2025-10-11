package org.taonity.artistinsightservice.health

import mu.KotlinLogging
import org.springframework.boot.actuate.health.Status
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.taonity.artistinsightservice.donation.kofi.KofiService
import java.time.Duration
import java.time.Instant

@Component
class KofiHealthPinger(
    private val kofiService: KofiService,
) : ExternalServiceHealthPinger {

    companion object {
        private const val MAX_BODY_PREVIEW_CHARS = 160
        private val LOGGER = KotlinLogging.logger {}
    }

    override val name: String = "kofi"

    override fun ping(): HealthCheckResult {
        val url = kofiService.getKofiUrl()
        val start = Instant.now()
        return try {
            val responseEntity = kofiService.getMainPage()
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            val statusCode = responseEntity.statusCode
            val details = LinkedHashMap<String, Any?>()
            details["url"] = url
            details["statusCode"] = statusCode
            details["responseTimeMs"] = elapsedMs

            val healthy = statusCode.is2xxSuccessful || statusCode.is3xxRedirection

            if (statusCode.is3xxRedirection) {
                val redirect = responseEntity.headers.location
                details["redirectLocation"] = redirect
            }

            if (!healthy) {
                details["responsePreview"] = responseEntity.body?.take(MAX_BODY_PREVIEW_CHARS)
            }

            HealthCheckResult(
                status = if (healthy) Status.UP else Status.DOWN,
                details = details
            )
        } catch (exception: Exception) {
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            val details = mapOf(
                "url" to url,
                "responseTimeMs" to elapsedMs,
                "error" to (exception.message ?: exception::class.simpleName)
            )

            if (exception is HttpClientErrorException && exception.statusCode.is4xxClientError) {
               return HealthCheckResult(Status.UP, details)
            }
            LOGGER.warn { "Ko-fi availability check failed for $url" }

            HealthCheckResult(Status.DOWN, details)
        }
    }
}
