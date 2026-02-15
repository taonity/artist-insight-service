package org.taonity.artistinsightservice.devaccess.interceptor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.http.entity.ContentType
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import org.taonity.artistinsightservice.infrastructure.exception.ClientErrorCode
import org.taonity.artistinsightservice.infrastructure.exception.ClientErrorResponse
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class RateLimitInterceptor : HandlerInterceptor {
    @Value("\${app.dev-access.rate-limit.send-email.capacity}")
    private var capacity: Int = 1

    @Value("\${app.dev-access.rate-limit.send-email.refill-duration}")
    private lateinit var refillDuration: String

    private val buckets = ConcurrentHashMap<String, Bucket>()

    companion object {
        private val objectMapper = jacksonObjectMapper()
    }

    private fun getLimit(): Bandwidth {
        val duration = Duration.parse(refillDuration)
        return Bandwidth.classic(capacity.toLong(), Refill.intervally(capacity.toLong(), duration))
    }

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (request.requestURI == "/development-access-request") {
            val ip = request.remoteAddr ?: "unknown"
            val bucket = buckets.computeIfAbsent(ip) { Bucket.builder().addLimit(getLimit()).build() }
            if (!bucket.tryConsume(1)) {
                writeResponse(response)
                return false
            }
        }
        return true
    }

    private fun writeResponse(response: HttpServletResponse) {
        response.contentType = ContentType.APPLICATION_JSON.mimeType
        response.status = HttpStatus.TOO_MANY_REQUESTS.value()
        val clientErrorResponse = ClientErrorResponse(
            ClientErrorCode.TOO_MANY_REQUESTS,
            "Maximum request rate is 1 request per $refillDuration with $capacity capacity"
        )
        response.writer.write(objectMapper.writeValueAsString(clientErrorResponse))
        response.writer.flush()
    }
}