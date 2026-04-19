package org.taonity.artistinsightservice.share.controller

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.taonity.artistinsightservice.other.ControllerTestsBaseClass
import org.taonity.artistinsightservice.share.dto.ShareLinkResponse
import org.taonity.artistinsightservice.share.dto.SharedArtistsResponse

@Sql(scripts = ["classpath:sql/clear-data.sql", "classpath:sql/test-data.sql"])
@Sql(scripts = ["classpath:sql/clear-data.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class ShareControllerTest : ControllerTestsBaseClass() {

    companion object {
        private val OBJECT_MAPPER = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }

    @Test
    fun `create share link`() {
        val session = authorizeOAuth2()

        val createResult = mockMvc.perform(
            post("/share").session(session).with(csrf())
        )
            .andExpect(status().isOk)
            .andReturn()

        val response: ShareLinkResponse = OBJECT_MAPPER.readValue(createResult.response.contentAsString)
        assertThat(response.shareCode).isNotBlank()
        assertThat(response.expiresAt).isNotNull()
    }

    @Test
    fun `get share link status`() {
        val session = authorizeOAuth2()

        // Create share link first
        mockMvc.perform(post("/share").session(session).with(csrf()))
            .andExpect(status().isOk)

        // Get status
        val statusResult = mockMvc.perform(get("/share").session(session))
            .andExpect(status().isOk)
            .andReturn()

        val response: ShareLinkResponse = OBJECT_MAPPER.readValue(statusResult.response.contentAsString)
        assertThat(response.shareCode).isNotBlank()
        assertThat(response.expiresAt).isNotNull()
    }

    @Test
    fun `delete share link`() {
        val session = authorizeOAuth2()

        // Create share link first
        mockMvc.perform(post("/share").session(session).with(csrf()))
            .andExpect(status().isOk)

        // Delete
        mockMvc.perform(delete("/share").session(session).with(csrf()))
            .andExpect(status().isNoContent)

        // Verify it's gone
        mockMvc.perform(get("/share").session(session))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get shared artists by share code`() {
        val session = authorizeOAuth2()

        // Create share link (will use followings from Spotify stub — 5 artists)
        val createResult = mockMvc.perform(post("/share").session(session).with(csrf()))
            .andExpect(status().isOk)
            .andReturn()

        val createResponse: ShareLinkResponse = OBJECT_MAPPER.readValue(createResult.response.contentAsString)

        // Get shared artists (public endpoint, no auth needed)
        val sharedResult = mockMvc.perform(get("/share/${createResponse.shareCode}"))
            .andExpect(status().isOk)
            .andReturn()

        val sharedResponse: SharedArtistsResponse = OBJECT_MAPPER.readValue(sharedResult.response.contentAsString)
        assertThat(sharedResponse.artists).hasSize(5)
        assertThat(sharedResponse.owner.displayName).isEqualTo("TestUser")
    }

    @Test
    fun `get shared artists with non-existent share code returns 404`() {
        mockMvc.perform(get("/share/nonExistent"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `get share link status when none exists returns 404`() {
        val session = authorizeOAuth2()

        // Delete any pre-existing share link from test data
        mockMvc.perform(delete("/share").session(session).with(csrf()))
            .andExpect(status().isNoContent)

        mockMvc.perform(get("/share").session(session))
            .andExpect(status().isNotFound)
    }
}
