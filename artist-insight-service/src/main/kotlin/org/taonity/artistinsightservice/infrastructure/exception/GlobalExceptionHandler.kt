package org.taonity.artistinsightservice.infrastructure.exception

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException
import org.taonity.artistinsightservice.advisory.Advisory
import org.taonity.artistinsightservice.advisory.AdvisoryResponse
import org.taonity.artistinsightservice.integration.openai.exception.OpenAITimeoutException
import org.taonity.artistinsightservice.integration.spotify.exception.SpotifyClientException
import org.taonity.artistinsightservice.integration.spotify.exception.SpotifyTimeoutException
import org.taonity.artistinsightservice.share.exception.ShareLinkExpiredException
import org.taonity.artistinsightservice.share.exception.ShareLinkNotFoundException

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

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleMissingFieldExceptions(e: HttpMessageNotReadableException) : ResponseEntity<ClientErrorResponse> {
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

    @ExceptionHandler(ShareLinkNotFoundException::class)
    fun handleShareLinkNotFoundException(e: ShareLinkNotFoundException): ResponseEntity<ClientErrorResponse> {
        LOGGER.debug(e) {}
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ClientErrorResponse(ClientErrorCode.SHARE_LINK_NOT_FOUND, e.message ?: "Share link not found"))
    }

    @ExceptionHandler(ShareLinkExpiredException::class)
    fun handleShareLinkExpiredException(e: ShareLinkExpiredException): ResponseEntity<ClientErrorResponse> {
        LOGGER.debug(e) {}
        return ResponseEntity.status(HttpStatus.GONE)
            .body(ClientErrorResponse(ClientErrorCode.SHARE_LINK_EXPIRED, e.message ?: "Share link has expired"))
    }
}