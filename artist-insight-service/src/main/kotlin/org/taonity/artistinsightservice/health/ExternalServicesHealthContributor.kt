package org.taonity.artistinsightservice.health

import org.springframework.boot.actuate.health.CompositeHealthContributor
import org.springframework.boot.actuate.health.HealthContributor
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.health.NamedContributor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component("externalServices")
@ConditionalOnProperty(value = ["health.enabled"], havingValue = "true")
class ExternalServicesHealthContributor(
    monitor: ExternalServicesHealthMonitor
) : CompositeHealthContributor {

    private val contributors: Map<String, HealthIndicator> = monitor.serviceNames()
        .associateWith { serviceName -> CachedHealthIndicator(monitor, serviceName) }

    override fun getContributor(name: String): HealthContributor? = contributors[name]

    override fun iterator(): MutableIterator<NamedContributor<HealthContributor>> {
        return contributors.entries
            .map { (name, indicator) -> NamedContributor.of<HealthContributor>(name, indicator) }
            .toMutableList()
            .iterator()
    }

    private class CachedHealthIndicator(
        private val monitor: ExternalServicesHealthMonitor,
        private val serviceName: String
    ) : HealthIndicator {
        override fun health() = monitor.getHealth(serviceName)
    }
}
