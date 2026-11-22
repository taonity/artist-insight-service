package org.taonity.artistinsightservice.local

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import jakarta.annotation.PreDestroy
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
@Profile("demo-data & local & stub-spotify & !prod & !stage")
class DemoSpotifyData(
    private val spotifyWireMockServer: WireMockServer,
    private val mapper: ObjectMapper,
) {
    private val mappings = mutableListOf<StubMapping>()

    @EventListener(ApplicationReadyEvent::class)
    fun install() {
        val genres =
            listOf(
                listOf("indie pop", "dream pop"),
                listOf("ambient", "electronic"),
                listOf("jazz", "nu jazz"),
                listOf("folk", "chamber pop"),
                listOf("post-rock"),
                listOf("soul", "alternative r&b"),
            )
        val artists =
            DemoCatalog.artistNames.mapIndexed { index, name ->
                mapOf(
                    "id" to "fictional-artist-$index",
                    "name" to name,
                    "genres" to genres[index % genres.size],
                    "href" to "https://music.example/artists/fictional-artist-$index",
                    "external_urls" to
                        mapOf("spotify" to "https://music.example/artists/fictional-artist-$index"),
                    "images" to
                        listOf(
                            mapOf(
                                "url" to DemoCatalog.artworkAvatar(index),
                                "width" to 96,
                                "height" to 96,
                            )
                        ),
                    "followers" to mapOf("total" to 257 + index * index * 83),
                    "popularity" to 23 + index * 7 % 75,
                )
            }
        register(
            "/v1/me/following",
            mapOf(
                "artists" to
                    mapOf(
                        "items" to artists,
                        "next" to null,
                        "total" to artists.size,
                        "limit" to 50,
                    )
            ),
        )
        register(
            "/v1/me",
            mapOf(
                "id" to "stub-user",
                "display_name" to DemoCatalog.people[2].name,
                "email" to "rumi.cade@listeners.example",
                "images" to images(DemoCatalog.people[2].avatar),
            ),
        )
        register("/v1/artists", mapOf("artists" to artists))
        DemoCatalog.people.take(3).forEach { person ->
            register(
                "/v1/users/${person.id}",
                mapOf(
                    "id" to person.id,
                    "display_name" to person.name,
                    "images" to images(person.avatar),
                ),
            )
        }
    }

    private fun images(url: String?): List<Map<String, Any>> =
        url?.let { listOf(mapOf("url" to it, "width" to 96, "height" to 96)) } ?: emptyList()

    private fun register(path: String, value: Any) {
        mappings +=
            spotifyWireMockServer.stubFor(
                get(urlPathEqualTo(path))
                    .atPriority(1)
                    .willReturn(
                        aResponse()
                            .withHeader("Content-Type", "application/json")
                            .withBody(mapper.writeValueAsString(value))
                    )
            )
    }

    @PreDestroy
    fun remove() {
        mappings.forEach(spotifyWireMockServer::removeStub)
        mappings.clear()
    }
}
