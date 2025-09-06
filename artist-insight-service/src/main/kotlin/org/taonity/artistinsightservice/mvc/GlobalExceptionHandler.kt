package org.taonity.artistinsightservice.mvc

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.taonity.artistinsightservice.Advisory
import org.taonity.artistinsightservice.openai.OpenAITimeoutException
import org.taonity.artistinsightservice.spotify.SpotifyClientException
import org.taonity.artistinsightservice.spotify.SpotifyTimeoutException

@RestControllerAdvice
class GlobalExceptionHandler {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception) {
        LOGGER.error(e) {}
    }

    @ExceptionHandler(OpenAITimeoutException::class)
    fun handleOpenAITimeoutException(e: OpenAITimeoutException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(setOf(Advisory.OPENAI_TIMEOUT.toDto()))
        LOGGER.error(e) {}
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(errorResponse)
    }

    @ExceptionHandler(SpotifyClientException::class)
    fun handleSpotifyClientException(e: SpotifyClientException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(setOf(Advisory.SPOTIFY_PROBLEM.toDto()))
        LOGGER.error(e) {}
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(SpotifyTimeoutException::class)
    fun handleSpotifyTimeoutException(e: SpotifyTimeoutException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(setOf(Advisory.SPOTIFY_TIMEOUT.toDto()))
        LOGGER.error(e) {}
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(errorResponse)
    }
}