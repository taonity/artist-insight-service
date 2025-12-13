package org.taonity.artistinsightservice.health

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.health.contributor.*
import org.springframework.stereotype.Component
import java.util.stream.Stream

@Component("externalServices")
@ConditionalOnProperty(value = ["health.enabled"], havingValue = "true")
class ExternalServicesHealthContributor(
    monitor: ExternalServicesHealthMonitor
) : CompositeHealthContributor {

    private val contributors: Map<String, HealthIndicator> = monitor.serviceNames()
        .associateWith { serviceName -> CachedHealthIndicator(monitor, serviceName) }

    override fun getContributor(name: String): HealthContributor? = contributors[name]


    override fun stream(): Stream<HealthContributors.Entry> {
        return contributors.entries
            .map { (name, indicator) -> HealthContributors.Entry(name, indicator) }
            .toMutableList()
            .stream()
    }

    private class CachedHealthIndicator(
        private val monitor: ExternalServicesHealthMonitor,
        private val serviceName: String
    ) : HealthIndicator {
        override fun health() = monitor.getHealth(serviceName)
    }
}
