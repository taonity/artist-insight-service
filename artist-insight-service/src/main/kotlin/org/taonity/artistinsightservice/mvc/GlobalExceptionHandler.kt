package org.taonity.artistinsightservice.mvc

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException
import org.taonity.artistinsightservice.attachments.Advisory
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
    fun handleException(e: Exception): ResponseEntity<ServerErrorResponse> {
        LOGGER.error(e) {}
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ServerErrorResponse(ServerErrorCode.UNKNOWN))
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleException(e: NoResourceFoundException) {
        LOGGER.debug(e) {}
    }

    @ExceptionHandler(OpenAITimeoutException::class)
    fun handleOpenAITimeoutException(e: OpenAITimeoutException): ResponseEntity<AdvisoryResponse> {
        val advisoryResponse = AdvisoryResponse(setOf(Advisory.OPENAI_TIMEOUT.toDto()))
        LOGGER.error(e) {}
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(advisoryResponse)
    }

    @ExceptionHandler(SpotifyClientException::class)
    fun handleSpotifyClientException(e: SpotifyClientException): ResponseEntity<AdvisoryResponse> {
        val advisoryResponse = AdvisoryResponse(setOf(Advisory.SPOTIFY_PROBLEM.toDto()))
        LOGGER.error(e) {}
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(advisoryResponse)
    }

    @ExceptionHandler(SpotifyTimeoutException::class)
    fun handleSpotifyTimeoutException(e: SpotifyTimeoutException): ResponseEntity<AdvisoryResponse> {
        val advisoryResponse = AdvisoryResponse(setOf(Advisory.SPOTIFY_TIMEOUT.toDto()))
        LOGGER.error(e) {}
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(advisoryResponse)
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException::class)
    fun handleMissingFieldExceptions(e: org.springframework.http.converter.HttpMessageNotReadableException) : ResponseEntity<ClientErrorResponse> {
        val cause = e.cause
        return when (cause) {
            is MissingKotlinParameterException, is MismatchedInputException -> {
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ClientErrorResponse(ClientErrorCode.MISSING_FIELD, cause.message ?: ""))
            }
            else -> {
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ClientErrorResponse(ClientErrorCode.MISSING_FIELD, e.message ?: ""))
            }
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(e: Exception) : ResponseEntity<ClientErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ClientErrorResponse(ClientErrorCode.VALIDATION_ERROR, e.message ?: ""))
    }
}