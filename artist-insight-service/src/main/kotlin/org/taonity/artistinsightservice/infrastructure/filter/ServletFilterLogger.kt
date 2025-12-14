package org.taonity.artistinsightservice.infrastructure.filter

import jakarta.servlet.ServletContext
import mu.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class ServletFilterLogger(private val servletContext: ServletContext) : CommandLineRunner {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    override fun run(vararg args: String) {
        // `getFilterRegistrations` returns a map: filter name -> FilterRegistration
        servletContext.getFilterRegistrations().forEach { name, registration ->
            LOGGER.debug { "Filter registered: $name, class: ${registration.getClassName()}, " +
                    "mapping: ${registration.getUrlPatternMappings()}" }
        }
    }
}