package org.taonity.artistinsightservice.logging

import mu.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Component

@Component
class BuildPropertiesLogging(
    private val buildProperties: BuildProperties

) : CommandLineRunner  {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    override fun run(vararg args: String?) {
        LOGGER.info { "BuildProperties - time: ${buildProperties.time}, version: ${buildProperties.version}" }
    }
}