package org.taonity.artistinsightservice.health

import mu.KotlinLogging
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.taonity.artistinsightservice.donation.kofi.service.KofiService
import java.time.Duration
import java.time.Instant

@Component("kofi")
class KofiHealthIndicator(
    private val kofiService: KofiService,
) : HealthIndicator {

    companion object {
        private const val MAX_BODY_PREVIEW_CHARS = 160
        private val LOGGER = KotlinLogging.logger {}
    }

    override fun health(): Health {
        val url = kofiService.getKofiUrl()
        val start = Instant.now()
        return try {
            val responseEntity = kofiService.getMainPage()
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            val statusCode = responseEntity.statusCode
            val healthy = statusCode.is2xxSuccessful || statusCode.is3xxRedirection
            val builder = if (healthy) Health.up() else Health.down()
            builder.withDetail("url", url)
                .withDetail("statusCode", statusCode)
                .withDetail("responseTimeMs", elapsedMs)
            if (statusCode.is3xxRedirection) {
                responseEntity.headers.location?.let { builder.withDetail("redirectLocation", it) }
            }
            if (!healthy) {
                builder.withDetail("responsePreview", responseEntity.body?.take(MAX_BODY_PREVIEW_CHARS) ?: "")
            }
            builder.build()
        } catch (exception: Exception) {
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            // Ko-fi sometimes serves a 4xx for unauthenticated probe requests; that still
            // proves the endpoint is reachable, so we treat it as UP rather than DOWN.
            if (exception is HttpClientErrorException && exception.statusCode.is4xxClientError) {
                return Health.up()
                    .withDetail("url", url)
                    .withDetail("responseTimeMs", elapsedMs)
                    .withDetail("error", exception.message?.take(MAX_BODY_PREVIEW_CHARS) ?: exception::class.simpleName ?: "unknown")
                    .build()
            }
            LOGGER.warn { "Ko-fi availability check failed for $url" }
            Health.down()
                .withDetail("url", url)
                .withDetail("responseTimeMs", elapsedMs)
                .withDetail("error", exception.message ?: exception::class.simpleName ?: "unknown")
                .build()
        }
    }
}
