package org.taonity.artistinsightservice.infrastructure.exception

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.taonity.artistinsightservice.advisory.Advisory
import org.taonity.artistinsightservice.integration.openai.exception.OpenAITimeoutException
import org.taonity.artistinsightservice.integration.spotify.exception.SpotifyClientException
import org.taonity.artistinsightservice.integration.spotify.exception.SpotifyTimeoutException
import org.taonity.artistinsightservice.share.exception.ShareLinkExpiredException
import org.taonity.artistinsightservice.share.exception.ShareLinkNotFoundException

class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun `handleException returns 500 with UNKNOWN error code`() {
        val response = handler.handleException(RuntimeException("something broke"))

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body!!.serverErrorCode).isEqualTo(ServerErrorCode.UNKNOWN)
    }

    @Test
    fun `handleOpenAITimeoutException returns 504 with OPENAI_TIMEOUT advisory`() {
        val response = handler.handleOpenAITimeoutException(OpenAITimeoutException("timeout"))

        assertThat(response.statusCode).isEqualTo(HttpStatus.GATEWAY_TIMEOUT)
        assertThat(response.body!!.advisories).hasSize(1)
        assertThat(response.body!!.advisories.first().code).isEqualTo(Advisory.OPENAI_TIMEOUT.name)
    }

    @Test
    fun `handleSpotifyClientException returns 500 with SPOTIFY_PROBLEM advisory`() {
        val response = handler.handleSpotifyClientException(SpotifyClientException("error"))

        assertThat(response.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        assertThat(response.body!!.advisories).hasSize(1)
        assertThat(response.body!!.advisories.first().code).isEqualTo(Advisory.SPOTIFY_PROBLEM.name)
    }

    @Test
    fun `handleSpotifyTimeoutException returns 504 with SPOTIFY_TIMEOUT advisory`() {
        val response = handler.handleSpotifyTimeoutException(SpotifyTimeoutException("timeout"))

        assertThat(response.statusCode).isEqualTo(HttpStatus.GATEWAY_TIMEOUT)
        assertThat(response.body!!.advisories).hasSize(1)
        assertThat(response.body!!.advisories.first().code).isEqualTo(Advisory.SPOTIFY_TIMEOUT.name)
    }

    @Test
    fun `handleShareLinkNotFoundException returns 404 with SHARE_LINK_NOT_FOUND`() {
        val response = handler.handleShareLinkNotFoundException(ShareLinkNotFoundException("not found"))

        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(response.body!!.clientErrorCode).isEqualTo(ClientErrorCode.SHARE_LINK_NOT_FOUND)
        assertThat(response.body!!.errorMessage).isEqualTo("not found")
    }

    @Test
    fun `handleShareLinkExpiredException returns 410 with SHARE_LINK_EXPIRED`() {
        val response = handler.handleShareLinkExpiredException(ShareLinkExpiredException("expired"))

        assertThat(response.statusCode).isEqualTo(HttpStatus.GONE)
        assertThat(response.body!!.clientErrorCode).isEqualTo(ClientErrorCode.SHARE_LINK_EXPIRED)
        assertThat(response.body!!.errorMessage).isEqualTo("expired")
    }

    @Test
    fun `handleValidationExceptions returns 400 with VALIDATION_ERROR`() {
        val response = handler.handleValidationExceptions(RuntimeException("field invalid"))

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body!!.clientErrorCode).isEqualTo(ClientErrorCode.VALIDATION_ERROR)
        assertThat(response.body!!.errorMessage).isEqualTo("field invalid")
    }
}
