package org.taonity.artistinsightservice.donation.kofi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.Duration

@Service
class KofiService(
    @Value("\${kofi.url}")
    private val kofiUrl: String,
) {

    companion object {
        private val restClient = RestClient.builder()
            .requestFactory(buildClientHttpRequestFactory())
            .build()

        private fun buildClientHttpRequestFactory(): ClientHttpRequestFactory {
            val clientHttpRequestFactory = HttpComponentsClientHttpRequestFactory()
            clientHttpRequestFactory.setConnectionRequestTimeout(Duration.ofSeconds(5))
            clientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(5))
            return clientHttpRequestFactory
        }
    }

    fun getMainPage() : ResponseEntity<String> {
        return restClient.get()
            .uri(kofiUrl)
            .header("User-Agent", "artist-insight/1.0")
            .retrieve()
            .toEntity(String::class.java)
    }

    fun getKofiUrl() : String = kofiUrl
}