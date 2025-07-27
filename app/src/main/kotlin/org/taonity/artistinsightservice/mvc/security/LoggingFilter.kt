package org.taonity.artistinsightservice.mvc.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

// TODO: remove if not needed
@Component
class LoggingFilter(
    private val httpServletLoggingService: HttpServletLoggingService
) : OncePerRequestFilter() {


    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {

//        val contentCachingRequestWrapper = httpServletLoggingService.logRequestWithWrapping(request)
//        filterChain.doFilter(contentCachingRequestWrapper, response)
        filterChain.doFilter(request, response)
    }


}