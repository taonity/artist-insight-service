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
                credits[index % credits.size],
                role,
                when {
                    index == 2 || index % 5 == 0 -> null
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
            "Amber Transit",
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
            "Cloud Postal",
            "Linen Satellites",
            "Fable District",
            "Golden Undertow",
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
            "Afterglow Index",
            "Bloom Operator",
            "Distant Meadow",
            "Honeycomb Avenue",
            "Northbound Velvet",
            "Daybreak Theory",
        )
}
