package org.taonity.artistinsightservice.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.taonity.artistinsightservice.donation.kofi.KoFiService
import java.time.Duration

@Component
class KoFiHealthPinger(
    private val koFiService: KoFiService,
    @Value("\${health.external.kofi.health-url:https://ko-fi.com}")
    private val kofiHealthUrl: String,
    @Value("\${health.external.request-timeout-ms:3000}")
    requestTimeoutMs: Long
) : ExternalServiceHealthPinger {

    override val name: String = "kofi"

    override fun ping(): HealthCheckResult {
        return koFiService.checkAvailability(
            url = kofiHealthUrl,
            requestTimeout = Duration.ofMillis(requestTimeoutMs)
        )
    }
}
