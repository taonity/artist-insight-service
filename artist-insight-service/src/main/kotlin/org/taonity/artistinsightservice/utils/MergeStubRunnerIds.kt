package org.taonity.artistinsightservice.utils

import org.apache.commons.logging.Log
import org.springframework.boot.EnvironmentPostProcessor
import org.springframework.boot.SpringApplication
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.logging.DeferredLogFactory
import org.springframework.core.Ordered
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.EnumerablePropertySource


class MergeStubRunnerIds(
    private val deferredLogFactory: DeferredLogFactory,
    private val logger: Log = deferredLogFactory.getLog(MergeStubRunnerIds::class.java)
) : EnvironmentPostProcessor, Ordered {

    companion object {
        private const val SPRING_CLOUD_CONTRACT_STUBRUNNER_IDS = "spring.cloud.contract.stubrunner.ids"
    }

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        val mergedIds = mutableSetOf<String>()
        val binder = Binder.get(environment)

        environment.propertySources.forEach { propertySource ->
            if (propertySource is EnumerablePropertySource<*>) {
                propertySource.propertyNames.forEach { key ->
                    if (key.contains("-mergeable.$SPRING_CLOUD_CONTRACT_STUBRUNNER_IDS")) {
                        val ids = binder.bind(key, List::class.java).orElse(emptyList<Any>())
                        mergedIds.addAll(ids!!.filterIsInstance<String>())
                    }
                }
            }
        }

        if (mergedIds.isNotEmpty()) {
            val mergedIdsString = mergedIds.joinToString(",")
            System.setProperty(SPRING_CLOUD_CONTRACT_STUBRUNNER_IDS, mergedIdsString)
            logger.info("Merged $SPRING_CLOUD_CONTRACT_STUBRUNNER_IDS property with $mergedIdsString")
        }
    }

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE
}
