package org.taonity.artistinsightservice.health

import org.springframework.boot.health.contributor.Status

data class HealthCheckResult(
    val status: Status,
    val details: Map<String, Any?>
)
