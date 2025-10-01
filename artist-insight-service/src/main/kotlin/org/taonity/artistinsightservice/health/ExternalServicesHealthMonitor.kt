package org.taonity.artistinsightservice.health

import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.Status
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.LinkedHashMap
import java.util.concurrent.ConcurrentHashMap

@Service
class ExternalServicesHealthMonitor(
    private val pingers: List<ExternalServiceHealthPinger>,
    @Value("\${health.external.refresh-interval-ms}")
    private val refreshIntervalMs: Long,
    @Value("\${health.external.initial-delay-ms}")
    private val initialDelayMs: Long
) {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    private val cache = ConcurrentHashMap<String, Health>()
    private val pingerByName = pingers.associateBy { it.name }
    private val serviceNames = pingerByName.keys.sorted()

    @PostConstruct
    fun initialise() {
        LOGGER.info { "Running initial external service health checks" }
        refreshAll()
    }

    @Scheduled(
        initialDelayString = "\${health.external.initial-delay-ms}",
        fixedDelayString = "\${health.external.refresh-interval-ms}"
    )
    fun scheduledRefresh() {
        refreshAll()
    }

    private fun refreshAll() {
        serviceNames.forEach { serviceName ->
            val pinger = pingerByName.getValue(serviceName)
            val health = buildHealth(pinger)
            cache[serviceName] = health
            LOGGER.debug { "Updated health status for $serviceName to ${health.status}" }
        }
    }

    private fun buildHealth(pinger: ExternalServiceHealthPinger): Health {
        val result = try {
            pinger.ping()
        } catch (exception: Exception) {
            LOGGER.error(exception) { "Health pinger ${pinger.name} threw an unexpected exception" }
            HealthCheckResult(
                Status.DOWN,
                mapOf("error" to (exception.message ?: exception::class.simpleName))
            )
        }

        val details = LinkedHashMap(result.details)
        details["lastCheckedAt"] = Instant.now().toString()
        details["refreshIntervalMs"] = refreshIntervalMs
        details["initialDelayMs"] = initialDelayMs

        return Health.status(result.status).withDetails(details).build()
    }

    fun getHealth(name: String): Health {
        return cache[name] ?: Health.unknown()
            .withDetail("message", "No health check result recorded yet")
            .build()
    }

    fun snapshot(): Map<String, Health> {
        return serviceNames.associateWith { serviceName -> getHealth(serviceName) }
    }

    fun serviceNames(): List<String> = serviceNames
}
