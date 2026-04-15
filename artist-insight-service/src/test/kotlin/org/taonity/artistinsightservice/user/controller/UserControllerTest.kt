package org.taonity.artistinsightservice.user.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.taonity.artistinsightservice.artist.dto.SafePrivateUserObject
import org.taonity.artistinsightservice.other.ControllerTestsBaseClass
import org.taonity.artistinsightservice.user.dto.SpotifyUserDto
import org.taonity.spotify.model.ImageObject

@AutoConfigureStubRunner(
    ids = ["org.taonity:spotify-contracts:+:stubs:8100"],
    stubsMode = StubRunnerProperties.StubsMode.CLASSPATH
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
            MockMvcRequestBuilders.get("/user")
                .session(mockHttpSession))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val spotifyUserDto: SpotifyUserDto = OBJECT_MAPPER.readValue(userMvcResult.response.contentAsString)

        Assertions.assertThat(spotifyUserDto).isEqualTo(EXPECTED_SPOTIFY_USER_DTO)
    }

    @Test
    fun `delete user`() {
        val mockHttpSession = authorizeOAuth2()

        // Verify user exists
        mockMvc.perform(
            MockMvcRequestBuilders.get("/user")
                .session(mockHttpSession))
            .andExpect(MockMvcResultMatchers.status().isOk)

        // Delete user
        mockMvc.perform(
            MockMvcRequestBuilders.delete("/user")
                .session(mockHttpSession)
                .with(csrf()))
            .andExpect(MockMvcResultMatchers.status().isNoContent)

        // Verify user is gone — findBySpotifyIdOrThrow will throw, resulting in 500
        mockMvc.perform(
            MockMvcRequestBuilders.get("/user")
                .session(mockHttpSession))
            .andExpect(MockMvcResultMatchers.status().isInternalServerError)
    }
}