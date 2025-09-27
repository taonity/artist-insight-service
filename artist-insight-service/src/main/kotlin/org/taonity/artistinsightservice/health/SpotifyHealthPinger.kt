package org.taonity.artistinsightservice.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class SpotifyHealthPinger(
    @Value("\${health.external.spotify.health-url:https://api.spotify.com/v1}")
    private val spotifyHealthUrl: String,
    @Value("\${health.external.request-timeout-ms:3000}")
    requestTimeoutMs: Long
) : HttpExternalServiceHealthPinger(Duration.ofMillis(requestTimeoutMs)) {

    override val name: String = "spotify"

    override fun ping(): HealthCheckResult {
        return performGet(
            url = spotifyHealthUrl,
            healthyStatusPredicate = { statusCode ->
                statusCode in 200..299 || statusCode == 401 || statusCode == 403
            },
            detailAugmentor = { response, details ->
                if (response.statusCode() == 401 || response.statusCode() == 403) {
                    details["note"] = "Received ${response.statusCode()} indicating OAuth token is required for full access."
                }
            }
        )
    }
}
