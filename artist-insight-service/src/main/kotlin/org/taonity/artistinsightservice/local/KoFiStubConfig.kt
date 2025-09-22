package org.taonity.artistinsightservice.local

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.client.RestClient


@Configuration
@Profile("stub-kofi")
class KoFiStubConfig {

    @Bean
    fun kofiRestClient(): RestClient {

        return RestClient.builder()
            .build()
    }
}