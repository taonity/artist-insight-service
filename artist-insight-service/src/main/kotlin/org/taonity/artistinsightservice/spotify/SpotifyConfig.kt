package org.taonity.artistinsightservice.spotify

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor.ClientRegistrationIdResolver
import org.springframework.web.client.RestClient
import org.taonity.artistinsightservice.mvc.security.RestClientErrorException
import java.nio.charset.Charset
import java.time.Duration


@Configuration
class SpotifyConfig {

    companion object {
        private val objectMapper = jacksonObjectMapper()
    }

    @Bean
    fun spotifyRestClient(authorizedClientManager: OAuth2AuthorizedClientManager): RestClient {
        val requestInterceptor = OAuth2ClientHttpRequestInterceptor(authorizedClientManager)
        requestInterceptor.setClientRegistrationIdResolver(clientRegistrationIdResolver())

        return RestClient.builder()
            .requestInterceptor(requestInterceptor)
            .requestFactory(buildClientHttpRequestFactory())
            .defaultStatusHandler(HttpStatusCode::isError) { req, res ->
                val responseBody = String(res.body.readAllBytes(), Charset.defaultCharset())
                val headersJson = objectMapper.writeValueAsString(req.headers)
                throw RestClientErrorException(
                    res.statusCode,
                    req.method,
                    req.uri.toString(),
                    headersJson,
                    responseBody
                )
            }
            .build()
    }

    private fun clientRegistrationIdResolver(): ClientRegistrationIdResolver {
        return ClientRegistrationIdResolver { request ->
            val authentication = SecurityContextHolder.getContext().authentication
            if (authentication is OAuth2AuthenticationToken) {
                authentication.authorizedClientRegistrationId
            } else {
                null
            }
        }
    }

    // TODO: Find best values for timeouts
    private fun buildClientHttpRequestFactory(): ClientHttpRequestFactory {
        val clientHttpRequestFactory = HttpComponentsClientHttpRequestFactory()
        clientHttpRequestFactory.setConnectTimeout(Duration.ofSeconds(2))
        clientHttpRequestFactory.setConnectionRequestTimeout(Duration.ofSeconds(2))
        clientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(2))
        return clientHttpRequestFactory
    }
}
