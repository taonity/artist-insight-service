package org.taonity.artistinsightservice.mvc

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.util.UriComponentsBuilder
import org.taonity.artistinsightservice.SafePrivateUserObject
import org.taonity.spotify.model.ImageObject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureStubRunner(
    ids = ["org.taonity:spotify-contracts:1.0-SNAPSHOT:stubs:8100"],
    stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
//@Sql(scripts = ["classpath:sql/test-data.sql"])
//TODO: pay attention
@DirtiesContext
class UserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc


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

    private fun authorizeOAuth2(): MockHttpSession {
        val mockHttpSession = MockHttpSession()
        val authenticationMvcResult = mockMvc.perform(
            get("/oauth2/authorization/spotify-artist-insight-service")
                .session(mockHttpSession)
        )
            .andExpect(status().is3xxRedirection)
            .andReturn()

        val state = getState(authenticationMvcResult)

        mockMvc.perform(
            get("/login/oauth2/code/spotify-artist-insight-service")
                .session(mockHttpSession)
                .param(
                    "code",
                    "AQDnprqgaHL-OgQl3T8ezARXH1UehFE0uYHbsgETI88sNI_2omVFSA6XOyMjG9W1xuTb-BqazM-iDfvgf4t9n8sYqRuJwsu2gDfc6Hv0Uyz6R7IvEkeGEAb9FzVV6vDOLSf5tm2j0ZKuoFeygYMVECTK3kvb0Ee5x7Kto-XTPcIyhAqVxbdN7ebxpFOtX-tYIQLM5p343HPAxr-mClIhbSrj-9_-t_7H3bQKJO-H7rR7ka5CvUU_YFAvcdwDjskxCZMBIotpiouflrK0n7NW"
                )
                .param("state", state)
        )
            .andExpect(status().is3xxRedirection)
        return mockHttpSession
    }

    private fun getState(authenticationMvcResult: MvcResult): String? {
        val location = authenticationMvcResult.response.getHeader("Location")
        val rawState = UriComponentsBuilder.fromUriString(location!!)
            .build()
            .queryParams
            .getFirst("state")
        val state = URLDecoder.decode(rawState, StandardCharsets.UTF_8)
        return state
    }
}

