package org.taonity.artistinsightservice.mvc

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockHttpSession
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@SpringBootTest
@AutoConfigureMockMvc
class ControllerTestsBaseClass {

    @Autowired
    lateinit var mockMvc: MockMvc

    fun authorizeOAuth2(): MockHttpSession {
        val session = MockHttpSession()
        val authResult = mockMvc.perform(
            get("/oauth2/authorization/spotify-artist-insight-service").session(session)
        )
            .andExpect(status().is3xxRedirection)
            .andReturn()
        val state = getState(authResult)
        mockMvc.perform(
            get("/login/oauth2/code/spotify-artist-insight-service")
                .session(session)
                .param(
                    "code",
                    "AQDnprqgaHL-OgQl3T8ezARXH1UehFE0uYHbsgETI88sNI_2omVFSA6XOyMjG9W1xuTb-BqazM-iDfvgf4t9n8sYqRuJwsu2gDfc6Hv0Uyz6R7IvEkeGEAb9FzVV6vDOLSf5tm2j0ZKuoFeygYMVECTK3kvb0Ee5x7Kto-XTPcIyhAqVxbdN7ebxpFOtX-tYIQLM5p343HPAxr-mClIhbSrj-9_-t_7H3bQKJO-H7rR7ka5CvUU_YFAvcdwDjskxCZMBIotpiouflrK0n7NW"
                )
                .param("state", state)
        )
            .andExpect(status().is3xxRedirection)
        return session
    }

    private fun getState(authenticationMvcResult: org.springframework.test.web.servlet.MvcResult): String {
        val location = authenticationMvcResult.response.getHeader("Location")
        val rawState = UriComponentsBuilder.fromUriString(location!!)
            .build()
            .queryParams
            .getFirst("state")
        return URLDecoder.decode(rawState, StandardCharsets.UTF_8)
    }
}