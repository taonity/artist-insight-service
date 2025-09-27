package org.taonity.artistinsightservice.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class KoFiHealthPinger(
    @Value("\${health.external.kofi.health-url:https://ko-fi.com}")
    private val kofiHealthUrl: String,
    @Value("\${health.external.request-timeout-ms:3000}")
    requestTimeoutMs: Long
) : HttpExternalServiceHealthPinger(Duration.ofMillis(requestTimeoutMs)) {

    override val name: String = "kofi"

    override fun ping(): HealthCheckResult {
        return performGet(
            url = kofiHealthUrl,
            healthyStatusPredicate = { statusCode -> statusCode in 200..399 },
            detailAugmentor = { response, details ->
                if (response.statusCode() in 300..399) {
                    val redirect = response.headers().firstValue("Location").orElse(null)
                    if (!redirect.isNullOrBlank()) {
                        details["redirectLocation"] = redirect
                    }
                }
            }
        )
    }
}
