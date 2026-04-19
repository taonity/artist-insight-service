package org.taonity.artistinsightservice.integration.openai.service

import com.openai.springboot.OpenAIClientAutoConfiguration
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.taonity.artistinsightservice.integration.openai.config.OpenAIConfig

@SpringBootTest(classes = [
    OpenAIConfig::class,
    OpenAIService::class,
    OpenAIClientAutoConfiguration::class
],)
@Disabled("Manual only")
@ActiveProfiles("stub-openai")
class OpenAIServiceTest {

    @Autowired
    lateinit var openAIService: OpenAIService

    @Test
    fun `openai contract stub returns genres`() {
        Assertions.assertThat(openAIService.provideGenres("Old Sorcery"))
            .isEqualTo(listOf("Ambient","Dungeon Synth","Dark Ambient","Electronic"))
    }
}