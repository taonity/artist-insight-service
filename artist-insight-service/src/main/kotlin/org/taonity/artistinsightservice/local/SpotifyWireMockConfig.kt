package org.taonity.artistinsightservice.local

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import mu.KotlinLogging
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("stub-spotify")
class SpotifyWireMockConfig {

    companion object {
        private val LOGGER = KotlinLogging.logger {}

        private val server: WireMockServer by lazy {
            WireMockServer(
                wireMockConfig()
                    .port(8100)
                    .usingFilesUnderClasspath("wiremock/spotify")
                    .globalTemplating(true)
            ).also {
                it.start()
                Runtime.getRuntime().addShutdownHook(Thread { it.stop() })
                LOGGER.info { "Spotify WireMock stub started on port ${it.port()}" }
            }
        }

        @JvmStatic
        @Bean
        fun spotifyWireMockInitializer(): BeanFactoryPostProcessor = BeanFactoryPostProcessor {
            server // Force lazy init before other beans
        }
    }

    @Bean(destroyMethod = "")
    fun spotifyWireMockServer(): WireMockServer = server
}
