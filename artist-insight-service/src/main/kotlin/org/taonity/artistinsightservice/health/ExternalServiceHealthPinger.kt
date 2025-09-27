package org.taonity.artistinsightservice.health

interface ExternalServiceHealthPinger {
    val name: String

    fun ping(): HealthCheckResult
}
