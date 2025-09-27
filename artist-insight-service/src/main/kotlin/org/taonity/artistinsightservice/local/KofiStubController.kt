package org.taonity.artistinsightservice.local

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient
import org.taonity.artistinsightservice.donation.kofi.KofiWebhookData
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@RestController
@Profile("stub-kofi")
class KofiStubController(
    @Qualifier("kofiRestClient") private val kofiRestClient: RestClient
) {
    companion object {
        private val LOGGER = KotlinLogging.logger {}
        private val objectMapper = jacksonObjectMapper()
            .registerKotlinModule()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
    }

    @GetMapping("/N4N11KVW3E")
    fun callback() {
        LOGGER.info { "Handling /N4N11KVW3E endpoint" }

        val kofiWebhookData = buildKofiWebhookData()

        val kofiWebhookDataJson = objectMapper.writeValueAsString(kofiWebhookData)
        val encoded: String = URLEncoder.encode(kofiWebhookDataJson, StandardCharsets.UTF_8)
        kofiRestClient.post()
            .uri("http://localhost:9016/callback/kofi")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body("data=" + encoded)
            .exchange { request, response ->
                LOGGER.info { "App returned status ${response.statusCode} on callback post" }
            }
    }

    @GetMapping("/")
    fun home() : String {
        LOGGER.info { "Handling / endpoint" }
        return "Home page"
    }

    private fun buildKofiWebhookData() = KofiWebhookData(
        "e9c903f3-ec7f-456c-9c14-3f14217d33e3",
        "f26c39fb-7b0c-4ad1-adb8-01dd3c4e48b9",
        "2025-09-10T18:56:36Z",
        "Donation",
        true,
        "Jo Example",
        "3126nx54y24ryqyza3qxcchi4wry",
        "3.00",
        "https://ko-fi.com/Home/CoffeeShop?txid=00000000-1111-2222-3333-444444444444",
        "jo.example@example.com",
        "USD",
        false,
        false,
        "00000000-1111-2222-3333-444444444444",
        null,
        null,
        null,
        "Jo#4105",
        "012345678901234567"
    )
}
