package org.taonity.artistinsightservice.mvc

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.taonity.artistinsightservice.followings.dto.SafePrivateUserObject
import org.taonity.artistinsightservice.user.SpotifyUserDto
import org.taonity.spotify.model.ImageObject


@AutoConfigureStubRunner(
    ids = ["org.taonity:spotify-contracts:1.0-SNAPSHOT:stubs:8100"],
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
//@Sql(scripts = ["classpath:sql/test-data.sql"])
//TODO: pay attention
@DirtiesContext
class UserControllerTest: ControllerTestsBaseClass() {


    companion object {
        private val OBJECT_MAPPER = jacksonObjectMapper()
        private const val USER_ID = "3126nx54y24ryqyza3qxcchi4wry"
        private const val USER_DISPLAY_NAME = "TestUser"
        private const val USER_GPT_USAGES_LEFT = 10
        private val USER_IMAGES = arrayListOf(
            ImageObject().apply {
                url = "https://i.scdn.co/image/ab67616d00001e02ff9ca10b55ce82ae553c8228"
                height = 300
                width = 300
            }
        )
        private val EXPECTED_SPOTIFY_USER_DTO = SpotifyUserDto(
            SafePrivateUserObject(
                USER_ID,
                USER_DISPLAY_NAME,
                USER_IMAGES
            ),
            USER_GPT_USAGES_LEFT
        )
    }


    @Test
    fun `get user`() {
        val mockHttpSession = authorizeOAuth2()

        val userMvcResult = mockMvc.perform(
            get("/user")
                .session(mockHttpSession))
            .andExpect(status().isOk)
            .andReturn()

        val spotifyUserDto: SpotifyUserDto = OBJECT_MAPPER.readValue(userMvcResult.response.contentAsString)

        assertThat(spotifyUserDto).isEqualTo(EXPECTED_SPOTIFY_USER_DTO)
    }
}

