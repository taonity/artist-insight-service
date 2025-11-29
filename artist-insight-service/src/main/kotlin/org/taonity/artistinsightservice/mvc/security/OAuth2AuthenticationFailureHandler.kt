package org.taonity.artistinsightservice.mvc.security

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.stereotype.Component

enum class AuthenticationErrorCode {
    UNAUTHORIZED_SPOTIFY_ACCOUNT,
    AUTHENTICATION_FAILED
}

@Component
class OAuth2AuthenticationFailureHandler(
    @Value("\${app.login-url}") private val loginUrl: String
) : SimpleUrlAuthenticationFailureHandler() {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private const val AUTH_ERROR_COOKIE_NAME = "auth_error"
    }

    override fun onAuthenticationFailure(
        request: HttpServletRequest,
        response: HttpServletResponse,
        exception: AuthenticationException
    ) {
        val errorCode = when {
            exception is OAuth2AuthenticationException && exception.error.errorCode == "invalid_user_info_response" -> {
                LOGGER.warn { "User authentication failed due to Spotify whitelist: ${exception.message}" }
                AuthenticationErrorCode.UNAUTHORIZED_SPOTIFY_ACCOUNT
            }
            else -> {
                LOGGER.error(exception) { "Authentication failed: ${exception.message}" }
                AuthenticationErrorCode.AUTHENTICATION_FAILED
            }
        }

        val cookie = Cookie(AUTH_ERROR_COOKIE_NAME, errorCode.name)
        cookie.path = "/"
        cookie.maxAge = 60
        cookie.isHttpOnly = false
        response.addCookie(cookie)

        redirectStrategy.sendRedirect(request, response, loginUrl)
    }
}
