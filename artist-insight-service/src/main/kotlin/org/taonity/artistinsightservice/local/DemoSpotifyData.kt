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
                val id = DemoCatalog.artistId(index)
                val artistUrl = "https://music.example/artists/$id"
                val spotifyGenres =
                    if (
                        index in DemoCatalog.artistIndexesWithoutGenres ||
                        index in DemoCatalog.enrichedGenresByArtistIndex
                    ) emptyList()
                    else DemoCatalog.spotifyGenreOverrides[index] ?: genres[index % genres.size]
                mapOf(
                    "id" to id,
                    "name" to name,
                    "genres" to spotifyGenres,
                    "href" to artistUrl,
                    "external_urls" to mapOf("spotify" to artistUrl),
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
        val owner = DemoCatalog.people[0]
        val userProfile = privateProfile(DemoCatalog.people[2], "rumi.cade@listeners.example")
        register("/v1/me", userProfile)
        register("/v1/me-owner", privateProfile(owner, "mira.vell@listeners.example"))
        register("/v1/me-user", userProfile)
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

    private fun privateProfile(person: DemoPerson, email: String) =
        mapOf(
            "id" to person.id,
            "display_name" to person.name,
            "email" to email,
            "images" to images(person.avatar),
        )

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
