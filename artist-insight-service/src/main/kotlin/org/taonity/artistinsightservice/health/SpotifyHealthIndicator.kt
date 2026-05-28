package org.taonity.artistinsightservice.health

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.stereotype.Component
import org.taonity.artistinsightservice.integration.spotify.service.SpotifyService
import java.time.Duration
import java.time.Instant


@Component("spotify")
class SpotifyHealthIndicator(
    private val spotifyService: SpotifyService,
) : HealthIndicator {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private const val MAX_BODY_PREVIEW_CHARS = 160
    }

    override fun health(): Health {
        val start = Instant.now()
        val url = spotifyService.getHealthCheckUserUrl()
        return try {
            val responseEntity = spotifyService.getHealthCheckUser()
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            val statusCode = responseEntity.statusCode
            val builder = if (statusCode.is2xxSuccessful) Health.up() else Health.down()
            builder.withDetail("url", url)
                .withDetail("statusCode", statusCode)
                .withDetail("responseTimeMs", elapsedMs)
            if (!statusCode.is2xxSuccessful) {
                builder.withDetail("responsePreview", responseEntity.body?.take(MAX_BODY_PREVIEW_CHARS) ?: "")
            }
            builder.build()
        } catch (exception: Exception) {
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            LOGGER.warn { "Spotify availability check failed for $url" }
            Health.down()
                .withDetail("url", url)
                .withDetail("responseTimeMs", elapsedMs)
                .withDetail("error", exception.message ?: exception::class.simpleName ?: "unknown")
                .build()
        }
    }
}
