package org.taonity.artistinsightservice.openai

import com.openai.client.okhttp.OpenAIOkHttpClient
import com.openai.springboot.OpenAIClientCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class OpenAIConfig {
    @Bean
    fun customizer(): OpenAIClientCustomizer {
        return OpenAIClientCustomizer { builder: OpenAIOkHttpClient.Builder ->
            builder
                .maxRetries(2)
                .timeout(Duration.ofSeconds(3))
        }
    }
}
