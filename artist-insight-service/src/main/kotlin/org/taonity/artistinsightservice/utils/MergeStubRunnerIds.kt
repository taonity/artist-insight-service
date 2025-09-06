package org.taonity.artistinsightservice.utils

import org.apache.commons.logging.Log
import org.springframework.boot.SpringApplication
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.boot.logging.DeferredLogFactory
import org.springframework.core.Ordered
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.EnumerablePropertySource

class MergeStubRunnerIds(
    private val deferredLogFactory: DeferredLogFactory,
    private val logger: Log = deferredLogFactory.getLog(MergeStubRunnerIds::class.java)
) : EnvironmentPostProcessor, Ordered {

    override fun postProcessEnvironment(environment: ConfigurableEnvironment?, application: SpringApplication?) {
        if (environment == null) {
            logger.error("ConfigurableEnvironment is null")
            return
        }

        val mergedIds = mutableSetOf<String>()
        val binder = Binder.get(environment)

        // Find all ...-mergeable.stubrunner.ids keys and merge their arrays
        environment.propertySources.forEach { propertySource ->
            if (propertySource is EnumerablePropertySource<*>) {
                propertySource.propertyNames.forEach { key ->
                    if (key.contains("-mergeable.stubrunner.ids")) {
                        val ids = binder.bind(key, List::class.java).orElse(emptyList<Any>())
                        mergedIds.addAll(ids.filterIsInstance<String>())
                    }
                }
            }
        }

        // Merge into stubrunner.ids
        if (mergedIds.isNotEmpty()) {
            val mergedIdsString = mergedIds.joinToString(",")
            System.setProperty("stubrunner.ids", mergedIdsString)
            logger.info("Merged stubrunner.ids property with $mergedIdsString")
        }
    }

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE
}
