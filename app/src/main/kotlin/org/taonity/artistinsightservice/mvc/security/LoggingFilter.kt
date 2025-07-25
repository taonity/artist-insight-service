package org.taonity.artistinsightservice.mvc.security

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.apache.logging.log4j.util.Strings
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import java.util.Objects.isNull
import java.util.stream.Collectors

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
    "x-requested-with"
)

private val cookiesLoggingBackList = emptyList<String>()

@Component
class LoggingFilter : OncePerRequestFilter() {

    companion object {
        private val objectMapper = jacksonObjectMapper()
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
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

        filterChain.doFilter(wrappedRequest, response)
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