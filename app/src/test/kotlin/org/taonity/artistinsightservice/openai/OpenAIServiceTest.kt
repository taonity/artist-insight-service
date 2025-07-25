package org.taonity.artistinsightservice.openai

import com.openai.springboot.OpenAIClientAutoConfiguration
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [
    OpenAIConfig::class,
    OpenAIService::class,
    OpenAIClientAutoConfiguration::class
],)
class OpenAIServiceTest (
   @Autowired val openAIService: OpenAIService
) {

    @Test
    fun provideGenres() {
        println(openAIService.provideGenres("Old Sorcery"))
    }
}