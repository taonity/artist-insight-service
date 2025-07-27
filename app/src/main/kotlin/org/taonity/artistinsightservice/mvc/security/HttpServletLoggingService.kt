package org.taonity.artistinsightservice.mvc.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.util.ContentCachingRequestWrapper
import java.nio.charset.Charset
import java.util.*
import java.util.AbstractMap
import java.util.Objects.isNull

@Service
class HttpServletLoggingService {
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
    }

    fun logRequestWithWrapping(request: HttpServletRequest): ContentCachingRequestWrapper {
        val wrappedRequest = ContentCachingRequestWrapper(request)
        val requestBody = String(wrappedRequest.contentAsByteArray, Charset.forName(request.characterEncoding))

        val headersJson = filterInterestedHeaders(request)
        val cookiesJson = filterInterestedCookies(request)

        LOGGER.info(
            "[{}] {} with interested headers {}, cookies {}, body [{}]",
            request.method,
            request.requestURI,
            headersJson,
            cookiesJson,
            requestBody
        )
        return wrappedRequest
    }

    private fun filterInterestedCookies(request: HttpServletRequest): String? {
        val requestCookies = if (isNull(request.cookies)) {
            emptyList()
        } else {
            Arrays.stream(request.cookies)
                .filter { cookie -> !cookiesLoggingBackList.contains(cookie.name) }
                .map { cookie -> AbstractMap.SimpleEntry(cookie.name, cookie.value) }
                .toList()
        }
        val cookiesJson = objectMapper.writeValueAsString(requestCookies)
        return cookiesJson
    }

    private fun filterInterestedHeaders(request: HttpServletRequest): String? {
        val requestHeaders = Collections.list(request.headerNames).stream()
            .filter { headerName -> !headerLoggingBackList.contains(headerName) }
            .map { headerName -> AbstractMap.SimpleEntry(headerName, request.getHeader(headerName)) }
            .toList()
        val headersJson = objectMapper.writeValueAsString(requestHeaders)
        return headersJson
    }
}
