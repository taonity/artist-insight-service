package org.taonity.artistinsightservice.infrastructure.utils

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
        val binder = Binder.get(environment)
        val mergedIds = environment.propertySources
            .asSequence()
            .filterIsInstance<EnumerablePropertySource<*>>()
            .flatMap { propertySource ->
                propertySource.propertyNames
                    .asSequence()
                    .filter { it.contains("-mergeable.$SPRING_CLOUD_CONTRACT_STUBRUNNER_IDS") }
                    .flatMap { key ->
                        binder.bind(key, List::class.java)
                            .orElse(emptyList<Any>())
                            ?.asSequence()
                            .orEmpty()
                            .filterIsInstance<String>()
                    }
            }
            .toSet()

        if (mergedIds.isNotEmpty()) {
            val mergedIdsString = mergedIds.joinToString(",")
            System.setProperty(SPRING_CLOUD_CONTRACT_STUBRUNNER_IDS, mergedIdsString)
            logger.info("Merged $SPRING_CLOUD_CONTRACT_STUBRUNNER_IDS property with $mergedIdsString")
        }
    }

    override fun getOrder(): Int = Ordered.LOWEST_PRECEDENCE
}
