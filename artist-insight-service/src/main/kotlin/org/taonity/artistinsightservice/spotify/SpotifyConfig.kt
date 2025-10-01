package org.taonity.artistinsightservice.spotify

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
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
    fun spotifyAuthorisationCodeRestClient(authorizedClientManager: OAuth2AuthorizedClientManager): RestClient {
        val requestInterceptor = OAuth2ClientHttpRequestInterceptor(authorizedClientManager)

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

    @Bean
    fun authorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository: ClientRegistrationRepository,
                                                             authorizedClientService: OAuth2AuthorizedClientService
    ) : AuthorizedClientServiceOAuth2AuthorizedClientManager {
        return AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService)
    }

    @Bean
    fun spotifyClientCredentialsRestClient(authorizedClientServiceOAuth2AuthorizedClientManager: AuthorizedClientServiceOAuth2AuthorizedClientManager): RestClient {
        val requestInterceptor = OAuth2ClientHttpRequestInterceptor(authorizedClientServiceOAuth2AuthorizedClientManager)

        return RestClient.builder()
            .requestInterceptor(requestInterceptor)
            .requestFactory(buildClientHttpRequestFactory())
            .build()
    }

    // TODO: Find best values for timeouts
    private fun buildClientHttpRequestFactory(): ClientHttpRequestFactory {
        val clientHttpRequestFactory = HttpComponentsClientHttpRequestFactory()
        clientHttpRequestFactory.setConnectTimeout(Duration.ofSeconds(5))
        clientHttpRequestFactory.setConnectionRequestTimeout(Duration.ofSeconds(5))
        clientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(5))
        return clientHttpRequestFactory
    }
}
