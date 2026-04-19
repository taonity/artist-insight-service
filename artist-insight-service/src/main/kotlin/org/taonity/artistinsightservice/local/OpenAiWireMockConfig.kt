package org.taonity.artistinsightservice.local

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import mu.KotlinLogging
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("stub-openai")
class OpenAiWireMockConfig {

    companion object {
        private val LOGGER = KotlinLogging.logger {}

        private val server: WireMockServer by lazy {
            WireMockServer(
                wireMockConfig()
                    .port(8101)
                    .usingFilesUnderClasspath("wiremock/openai")
                    .globalTemplating(true)
            ).also {
                it.start()
                Runtime.getRuntime().addShutdownHook(Thread { it.stop() })
                LOGGER.info { "OpenAI WireMock stub started on port ${it.port()}" }
            }
        }

        @JvmStatic
        @Bean
        fun openAiWireMockInitializer(): BeanFactoryPostProcessor = BeanFactoryPostProcessor {
            server // Force lazy init before other beans
        }
    }

    @Bean(destroyMethod = "")
    fun openAiWireMockServer(): WireMockServer = server
}
