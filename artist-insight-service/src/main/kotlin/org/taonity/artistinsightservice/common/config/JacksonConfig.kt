package org.taonity.artistinsightservice.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
class JacksonConfig {

    /**
     * Re-publishes Spring Boot's default Jackson configuration as the primary [ObjectMapper].
     *
     * Declaring [snakeCaseObjectMapper] below satisfies [org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration]'s
     * `@ConditionalOnMissingBean(ObjectMapper.class)` check, which would otherwise prevent the default mapper from being created.
     * Without this primary bean, Spring MVC message converters would fall back to the snake-case mapper for all responses.
     */
    @Bean
    @Primary
    fun primaryObjectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper =
        builder.createXmlMapper(false).build()

    @Bean(name = ["snakeCaseObjectMapper"])
    fun snakeCaseObjectMapper(): ObjectMapper =
        ObjectMapper()
            .registerKotlinModule()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
}
