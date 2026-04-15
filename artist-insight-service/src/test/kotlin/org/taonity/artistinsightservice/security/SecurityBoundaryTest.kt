package org.taonity.artistinsightservice.security

import org.junit.jupiter.api.Test
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.taonity.artistinsightservice.other.ControllerTestsBaseClass

@AutoConfigureStubRunner(
    ids = ["org.taonity:spotify-contracts:+:stubs:8100"],
    stubsMode = StubRunnerProperties.StubsMode.CLASSPATH
)
class SecurityBoundaryTest : ControllerTestsBaseClass() {

    @Test
    fun `GET followings without auth returns 401`() {
        mockMvc.perform(get("/followings"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `GET followings enriched without auth returns 401`() {
        mockMvc.perform(get("/followings/enriched"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `GET user without auth returns 401`() {
        mockMvc.perform(get("/user"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `DELETE user without auth returns 401`() {
        mockMvc.perform(delete("/user").with(csrf()))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `POST share without auth returns 401`() {
        mockMvc.perform(post("/share").with(csrf()))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `GET share status without auth returns 401`() {
        mockMvc.perform(get("/share"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `DELETE share without auth returns 401`() {
        mockMvc.perform(delete("/share").with(csrf()))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `GET shared artists by code is public`() {
        // Public endpoint — should not return 401 (it returns 404 for non-existent code)
        mockMvc.perform(get("/share/someCode"))
            .andExpect(status().isNotFound)
    }
}
