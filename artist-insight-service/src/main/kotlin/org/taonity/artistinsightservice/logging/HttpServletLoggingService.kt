package org.taonity.artistinsightservice.logging

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.util.ContentCachingRequestWrapper
import java.nio.charset.Charset
import java.util.*
import java.util.Objects.isNull

@Service
class HttpServletLoggingService(
    @Value ("\${app.minimised-http-servlet-logging}") private val minimisedHttpServletLogging: Boolean
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

        val wrappedRequest = ContentCachingRequestWrapper(request)

        if (filterEndpointIfEnabled(request)) {
            return wrappedRequest
        }

        val requestBody = if (isNull(request.characterEncoding)) {
            LOGGER.warn { "Request character encoding is null, using default UTF-8 for logging." }
            String(wrappedRequest.contentAsByteArray, Charset.defaultCharset())
        } else {
            String(wrappedRequest.contentAsByteArray, Charset.forName(request.characterEncoding))
        }

        val headersJson = getInterestedHeaders(request)
        val cookiesJson = getInterestedCookies(request)

        LOGGER.info(
            "[{}] {} with headers {}, cookies {}, body [{}]",
            request.method,
            request.requestURI,
            headersJson,
            cookiesJson,
            requestBody
        )
        return wrappedRequest
    }

    private fun filterEndpointIfEnabled(request: HttpServletRequest) =
        endpointBlacklist.contains(request.requestURI) || !minimisedHttpServletLogging

    private fun getInterestedCookies(request: HttpServletRequest): String? {
        val requestCookies = if (isNull(request.cookies)) {
            emptyList()
        } else {
            Arrays.stream(request.cookies)
                .filter { cookie -> filterCookieIfEnabled(cookie) }
                .map { cookie -> AbstractMap.SimpleEntry(cookie.name, cookie.value) }
                .toList()
        }
        val cookiesJson = objectMapper.writeValueAsString(requestCookies)
        return cookiesJson
    }

    private fun filterCookieIfEnabled(cookie: Cookie) =
        !cookiesLoggingBackList.contains(cookie.name) || !minimisedHttpServletLogging

    private fun getInterestedHeaders(request: HttpServletRequest): String? {
        val requestHeaders = Collections.list(request.headerNames).stream()
            .filter { headerName -> filterHeaderIfEnabled(headerName) }
            .map { headerName -> AbstractMap.SimpleEntry(headerName, request.getHeader(headerName)) }
            .toList()
        val headersJson = objectMapper.writeValueAsString(requestHeaders)
        return headersJson
    }

    private fun filterHeaderIfEnabled(headerName: String) =
        !headerLoggingBackList.contains(headerName) || !minimisedHttpServletLogging
}
