package org.taonity.artistinsightservice.followings.service

import mu.KotlinLogging
import org.junit.jupiter.api.Test
import org.taonity.artistinsightservice.artist.dto.SafeArtistObject
import org.taonity.spotify.model.ArtistObject

//TODO: improve test
class FollowingsServiceTest {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    @Test
    fun fetchRawFollowings() {
        val artist = ArtistObject()
        artist.name = "dfdf"
        artist.id = "dfdf"
        artist.genres = ArrayList()
        artist.href = "dfdf"
        artist.images = ArrayList()
        val list = listOf(artist)
        val output = list.mapNotNull {
            try {
                SafeArtistObject.fromApi(it)
            } catch (e: IllegalArgumentException) {
                LOGGER.warn { "Validation failed: ${e.message}" }
                null
            }
        }
        println(output)
    }
}