package org.taonity.artistinsightservice.health

import mu.KotlinLogging
import org.springframework.boot.actuate.health.Status
import org.springframework.stereotype.Component
import org.taonity.artistinsightservice.spotify.SpotifyService
import java.time.Duration
import java.time.Instant

@Component
class SpotifyHealthPinger(
    private val spotifyService: SpotifyService,
) : ExternalServiceHealthPinger {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private const val MAX_BODY_PREVIEW_CHARS = 160
    }

    override val name: String = "spotify"

    override fun ping(): HealthCheckResult {
        val start = Instant.now()
        val url = spotifyService.getHealthCheckUserUrl()
        return try {
            val responseEntity = spotifyService.getHealthCheckUser()
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            val statusCode = responseEntity.statusCode
            val details = LinkedHashMap<String, Any?>()
            details["url"] = url
            details["statusCode"] = statusCode
            details["responseTimeMs"] = elapsedMs

            val healthy = statusCode.is2xxSuccessful

            if (!healthy) {
                details["responsePreview"] = responseEntity.body?.take(MAX_BODY_PREVIEW_CHARS)
            }

            HealthCheckResult(
                status = if (healthy) Status.UP else Status.DOWN,
                details = details
            )
        } catch (exception: Exception) {
            val elapsedMs = Duration.between(start, Instant.now()).toMillis()
            LOGGER.warn(exception) { "Spotify availability check failed for $url" }
            val details = mapOf(
                "url" to url,
                "responseTimeMs" to elapsedMs,
                "error" to (exception.message ?: exception::class.simpleName)
            )
            HealthCheckResult(Status.DOWN, details)
        }
    }
}
