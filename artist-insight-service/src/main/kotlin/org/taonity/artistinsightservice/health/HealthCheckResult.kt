package org.taonity.artistinsightservice.health

import org.springframework.boot.actuate.health.Status

data class HealthCheckResult(
    val status: Status,
    val details: Map<String, Any?>
)
