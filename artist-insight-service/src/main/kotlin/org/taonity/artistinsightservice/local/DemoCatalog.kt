package org.taonity.artistinsightservice.local

import org.taonity.artistinsightservice.console.entity.ConsoleRole

data class DemoPerson(
    val id: String,
    val name: String,
    val credits: Int,
    val role: ConsoleRole,
    val avatar: String?,
)

object DemoCatalog {
    private val firstNames =
        listOf(
            "Aven",
            "Liora",
            "Koa",
            "Nemi",
            "Tavi",
            "Eris",
            "Oren",
            "Rumi",
            "Sora",
            "Elio",
            "Veya",
            "Noor",
            "Ivo",
            "Zeli",
            "Arlo",
            "Mika",
            "Lumi",
            "Riven",
            "Soli",
            "Nola",
        )
    private val lastNames = listOf("Vell", "Marlow", "Cade", "Serein", "Orla", "Fenn")
    private val credits = listOf(0, 3, 10, 24, 58, 120, 204, 360, 8, 75, 500)
    private val artwork = listOf("abstract", "landscape", "fox", "vinyl", "jazz", "cosmos")

    fun artworkAvatar(index: Int): String =
        "/demo/avatars/art-${artwork[index.mod(artwork.size)]}.webp"

    val people: List<DemoPerson> =
        (0 until 120).map { index ->
            val id =
                when (index) {
                    0 -> "stub-owner"
                    1 -> "stub-admin"
                    2 -> "stub-user"
                    else -> "demo-listener-${index.toString().padStart(3, '0')}"
                }
            val name =
                when (index) {
                    0 -> "Mira Vell"
                    1 -> "Koa Marlow"
                    2 -> "Rumi Cade"
                    else ->
                        "${firstNames[index % firstNames.size]} ${lastNames[index / firstNames.size]}"
                }
            val role =
                when {
                    index == 0 -> ConsoleRole.OWNER
                    index == 1 || index % 19 == 0 -> ConsoleRole.ADMIN
                    else -> ConsoleRole.NONE
                }
            DemoPerson(
                id,
                name,
                if (index == 0) 10 else credits[index % credits.size],
                role,
                when {
                    index == 0 -> "/demo/avatars/portrait-mira-vell.webp"
                    index == 2 -> "/demo/avatars/portrait-rumi-cade.webp"
                    index % 5 == 0 -> null
                    index in setOf(7, 28, 61, 91, 117) ->
                        "/demo/avatars/portrait-${(index % 16 + 1).toString().padStart(2, '0')}.svg"
                    else -> artworkAvatar(index / 5 + index % 5)
                },
            )
        }
    val artistNames =
        listOf(
            "Juniper Signal",
            "Velvet Circuit",
            "Moss Atlas",
            "Lunar Sidewalk",
            "Amber Transit and the Last Train Through the Northern Lights",
            "Paper Tides",
            "Quiet Comet",
            "Cobalt Orchard",
            "Sunday Static",
            "Glass Lanterns",
            "Silver Fern Radio",
            "Orbit of Pines",
            "Neon Thimble",
            "Soft Meridian",
            "Copper Echo",
            "Violet Causeway",
            "Cloud Postal Service for Letters That Never Reached the Moon",
            "Linen Satellites",
            "Fable District and the Midnight Parade at the Edge of Town",
            "Golden Undertow and the Silent Choir Beneath the Boardwalk",
            "Cedar Frequency",
            "Echo Loom",
            "Drift Almanac",
            "Pocket Aurora",
            "Indigo Relay",
            "Fern Arithmetic",
            "Hollow Sunroom",
            "Mellow Compass",
            "Marble Seasons",
            "Parallel Clover",
            "Afterglow Index and the Orchestras of the Sleeping Coast",
            "Bloom Operator",
            "Distant Meadow",
            "Honeycomb Avenue",
            "Northbound Velvet",
            "Daybreak Theory",
        )

    val enrichedGenresByArtistIndex =
        mapOf(
            1 to listOf("downtempo", "ambient techno"),
            4 to listOf(
                "post-rock", "instrumental rock", "ambient rock", "cinematic drone",
                "chamber rock", "experimental guitar", "progressive rock",
            ),
            11 to listOf("indie folk", "acoustic folk"),
            22 to listOf("ambient", "minimalism"),
        )
    val spotifyGenreOverrides =
        mapOf(
            18 to listOf(
                "indie folk", "orchestral folk", "chamber pop", "americana",
                "acoustic ballads", "baroque pop", "singer-songwriter",
            ),
            25 to listOf(
                "modern jazz", "jazz fusion", "broken beat", "spiritual jazz",
                "experimental soul", "instrumental hip hop", "nu jazz",
            ),
            31 to listOf(
                "ambient pop", "dream pop", "leftfield electronic", "downtempo",
                "synth-pop", "balearic beat", "experimental ambient", "shoegaze",
            ),
        )
    val artistIndexesWithoutGenres = setOf(2, 5, 9, 14, 20)

    fun artistId(index: Int): String = "fictional-artist-$index"
}
