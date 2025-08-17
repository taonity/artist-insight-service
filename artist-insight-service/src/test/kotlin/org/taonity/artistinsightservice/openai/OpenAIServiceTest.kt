package org.taonity.artistinsightservice.openai

import com.openai.springboot.OpenAIClientAutoConfiguration
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties

@SpringBootTest(classes = [
    OpenAIConfig::class,
    OpenAIService::class,
    OpenAIClientAutoConfiguration::class
],)
@Disabled("Manual only")
//@AutoConfigureStubRunner(
//    ids = ["org.taonity:openai-contracts:1.0-SNAPSHOT:stubs:8101"],
//    stubsMode = StubRunnerProperties.StubsMode.LOCAL
//)
class OpenAIServiceTest (
   @Autowired val openAIService: OpenAIService
) {

    @Test
    fun provideGenres() {
        println(openAIService.provideGenres("Old Sorcery"))
    }
}