package org.taonity.artistinsightservice.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.health.Status
import org.springframework.stereotype.Component
import java.util.LinkedHashMap

@Component("readiness")
class ReadinessHealthIndicator(
    private val externalServicesHealthMonitor: ExternalServicesHealthMonitor
) : HealthIndicator {

    override fun health(): Health {
        val snapshot = externalServicesHealthMonitor.snapshot()
        if (snapshot.isEmpty()) {
            return Health.unknown()
                .withDetail("message", "No external health checks completed yet")
                .build()
        }

        val allUp = snapshot.values.all { it.status == Status.UP }
        val builder = if (allUp) Health.up() else Health.down()

        val externalDetails = snapshot.mapValues { (_, health) ->
            val details = LinkedHashMap<String, Any?>()
            details["status"] = health.status.code
            health.details.forEach { (key, value) ->
                details[key] = value
            }
            details
        }

        builder.withDetail("externalServices", externalDetails)

        return builder.build()
    }
}
