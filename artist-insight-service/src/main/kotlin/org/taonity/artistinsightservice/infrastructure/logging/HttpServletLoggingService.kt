package org.taonity.artistinsightservice.infrastructure.logging

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.ContentCachingRequestWrapper
import java.nio.charset.Charset
import java.util.AbstractMap
import java.util.Enumeration

@Service
class HttpServletLoggingService(
    @Value("\${app.minimised-http-servlet-logging}") private val minimisedHttpServletLogging: Boolean
) {
    companion object {
        private val objectMapper = jacksonObjectMapper()
        private val LOGGER = KotlinLogging.logger {}
        private val headerLoggingBackList = listOf(
            "host",
            "user-agent",
            "accept",
            "accept-language",
            "accept-encoding",
            "connection",
            "sec-fetch-dest",
            "sec-fetch-mode",
            "sec-fetch-site",
            "priority",
            "cookie",
            "upgrade-insecure-requests",
            "sec-fetch-user",
            "referer",
            "x-requested-with",
            "sec-ch-ua-platform",
            "sec-ch-ua",
            "sec-ch-ua-mobile",
            "origin"
        )
        private val cookiesLoggingBackList = emptyList<String>()
        private val endpointBlacklist = listOf(
            "/actuator/health"
        )
    }

    @PostConstruct
    private fun logMinimisedLoggingModeIfEnabled() {
        if (minimisedHttpServletLogging) {
            LOGGER.info { "Minimised logging mode enabled - app.minimised-http-servlet-logging=true" }
            LOGGER.info { "Following endpoints will be ignored: $endpointBlacklist" }
            LOGGER.info { "Following headers will be ignored: $headerLoggingBackList" }
            LOGGER.info { "Following cookies will be ignored: $cookiesLoggingBackList" }
        }
    }

    fun logRequestWithWrapping(request: HttpServletRequest): ContentCachingRequestWrapper {

        val wrappedRequest = ContentCachingRequestWrapper(request, 256 * 1024)

        if (shouldSkipEndpointLogging(request)) {
            return wrappedRequest
        }

        val requestBody = String(
            wrappedRequest.contentAsByteArray,
            request.characterEncoding?.let(Charset::forName) ?: Charsets.UTF_8
        )

        val headersJson = getInterestedHeaders(request)
        val cookiesJson = getInterestedCookies(request)

        LOGGER.debug(
            "[{}] {} with headers {}, cookies {}, body [{}]",
            request.method,
            request.requestURI,
            headersJson,
            cookiesJson,
            requestBody
        )
        return wrappedRequest
    }

    private fun shouldSkipEndpointLogging(request: HttpServletRequest) =
        minimisedHttpServletLogging && request.requestURI in endpointBlacklist

    private fun getInterestedCookies(request: HttpServletRequest): String = objectMapper.writeValueAsString(
        request.cookies
            .orEmpty()
            .asSequence()
            .filter(::filterCookieIfEnabled)
            .map { cookie -> AbstractMap.SimpleEntry(cookie.name, cookie.value) }
            .toList()
    )

    private fun filterCookieIfEnabled(cookie: Cookie) =
        cookie.name !in cookiesLoggingBackList || !minimisedHttpServletLogging

    private fun getInterestedHeaders(request: HttpServletRequest): String = objectMapper.writeValueAsString(
        request.headerNames
            .asSequence()
            .filter(::filterHeaderIfEnabled)
            .map { headerName -> AbstractMap.SimpleEntry(headerName, request.getHeader(headerName)) }
            .toList()
    )

    private fun filterHeaderIfEnabled(headerName: String) =
        headerName !in headerLoggingBackList || !minimisedHttpServletLogging

    private fun <T> Enumeration<T>.asSequence(): Sequence<T> = sequence {
        while (hasMoreElements()) {
            yield(nextElement())
        }
    }
}
