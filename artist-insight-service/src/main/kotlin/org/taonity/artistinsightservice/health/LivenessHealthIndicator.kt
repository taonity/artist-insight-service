package org.taonity.artistinsightservice.health

import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component("liveness")
class LivenessHealthIndicator : HealthIndicator {

    private val startedAt: Instant = Instant.now()

    override fun health(): Health {
        val uptime = Duration.between(startedAt, Instant.now())
        return Health.up()
            .withDetail("startedAt", startedAt.toString())
            .withDetail("uptimeSeconds", uptime.seconds)
            .withDetail("uptimeHumanReadable", formatDuration(uptime))
            .build()
    }

    private fun formatDuration(duration: Duration): String {
        val hours = duration.toHours()
        val minutes = duration.toMinutesPart()
        val seconds = duration.toSecondsPart()
        return String.format("%02dh %02dm %02ds", hours, minutes, seconds)
    }
}
