package org.taonity.artistinsightservice.security.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.taonity.artistinsightservice.security.principal.SpotifyUserPrincipal

@Component
class UserMdcFilter : OncePerRequestFilter() {

    companion object {
        const val MDC_USER_ID = "userId"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication != null && authentication.isAuthenticated) {
                val principal = authentication.principal
                if (principal is SpotifyUserPrincipal) {
                    MDC.put(MDC_USER_ID, principal.getSpotifyId())
                }
            }
            filterChain.doFilter(request, response)
        } finally {
            MDC.remove(MDC_USER_ID)
        }
    }
}

