package org.taonity.artistinsightservice.mvc.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class OAuth2AuthenticationFailureHandler(
    @Value("\${app.login-url}") private val loginUrl: String
) : SimpleUrlAuthenticationFailureHandler() {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val errorMessage = when {
            exception is OAuth2AuthenticationException && exception.error.errorCode == "invalid_user_info_response" -> {
                LOGGER.warn { "User authentication failed due to Spotify whitelist: ${exception.message}" }
                "Access denied. Your Spotify account is not authorized to use this application. Please contact the administrator."
            }
            else -> {
                LOGGER.error(exception) { "Authentication failed: ${exception.message}" }
                "Authentication failed. Please try again."
            }
        }

        val targetUrl = UriComponentsBuilder.fromUriString(loginUrl)
            .queryParam("error", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
            .build()
            .toUriString()

        redirectStrategy.sendRedirect(request, response, targetUrl)
    }
}
