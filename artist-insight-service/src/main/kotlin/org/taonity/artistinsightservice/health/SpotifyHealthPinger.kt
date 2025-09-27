package org.taonity.artistinsightservice.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.taonity.artistinsightservice.spotify.SpotifyService
import java.time.Duration

@Component
class SpotifyHealthPinger(
    private val spotifyService: SpotifyService,
    @Value("\${health.external.spotify.health-url:https://api.spotify.com/v1}")
    private val spotifyHealthUrl: String,
    @Value("\${health.external.request-timeout-ms:3000}")
    requestTimeoutMs: Long
) : ExternalServiceHealthPinger {

    override val name: String = "spotify"

    override fun ping(): HealthCheckResult {
        return spotifyService.checkAvailability(
            url = spotifyHealthUrl,
            requestTimeout = Duration.ofMillis(requestTimeoutMs)
        )
    }
}
