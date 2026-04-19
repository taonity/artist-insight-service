package org.taonity.artistinsightservice.devaccess.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.taonity.artistinsightservice.devaccess.entity.DevAccessRequestStatus
import org.taonity.artistinsightservice.devaccess.repository.DevAccessRepository
import org.taonity.artistinsightservice.devaccess.service.AppMailSender
import org.taonity.artistinsightservice.other.ControllerTestsBaseClass

@TestPropertySource(properties = [
    "app.dev-access.enabled=true",
    "app.dev-access.rate-limit.send-email.capacity=10",
    "app.dev-access.rate-limit.send-email.refill-duration=PT1M"
])
@Sql("classpath:sql/clear-data.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class DevAccessControllerTest : ControllerTestsBaseClass() {

    @MockitoBean
    lateinit var appMailSender: AppMailSender

    @Autowired
    lateinit var devAccessRepository: DevAccessRepository

    companion object {
        private val OBJECT_MAPPER = jacksonObjectMapper()
    }

    @Test
    fun `submit development access request`() {
        val requestBody = OBJECT_MAPPER.writeValueAsString(
            mapOf("email" to "test@example.com", "message" to "I would like access")
        )

        mockMvc.perform(
            post("/development-access-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf())
        )
            .andExpect(status().isOk)

        val savedRequests = devAccessRepository.findAll().toList()
        assertThat(savedRequests).hasSize(1)
        assertThat(savedRequests[0].email).isEqualTo("test@example.com")
        assertThat(savedRequests[0].message).isEqualTo("I would like access")
        assertThat(savedRequests[0].status).isEqualTo(DevAccessRequestStatus.PENDING)
    }

    @Test
    fun `submit request with invalid email returns 400`() {
        val requestBody = OBJECT_MAPPER.writeValueAsString(
            mapOf("email" to "not-an-email", "message" to "I would like access")
        )

        mockMvc.perform(
            post("/development-access-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `submit request with blank email returns 400`() {
        val requestBody = OBJECT_MAPPER.writeValueAsString(
            mapOf("email" to "", "message" to "I would like access")
        )

        mockMvc.perform(
            post("/development-access-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `submit request with blank message returns 400`() {
        val requestBody = OBJECT_MAPPER.writeValueAsString(
            mapOf("email" to "test@example.com", "message" to "")
        )

        mockMvc.perform(
            post("/development-access-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
                .with(csrf())
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `submit request without csrf returns 403`() {
        val requestBody = OBJECT_MAPPER.writeValueAsString(
            mapOf("email" to "test@example.com", "message" to "I would like access")
        )

        mockMvc.perform(
            post("/development-access-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andExpect(status().isForbidden)
    }
}
