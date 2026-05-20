package org.taonity.artistinsightservice.artist.dto

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.taonity.spotify.model.ArtistObject
import org.taonity.spotify.model.ExternalUrlObject
import org.taonity.spotify.model.FollowersObject

class SafeArtistObjectTest {

    @Test
    fun `fromApi maps a complete ArtistObject to SafeArtistObject`() {
        val api = validArtistObject()

        val result = SafeArtistObject.fromApi(api)

        assertThat(result.id).isEqualTo("artist-1")
        assertThat(result.name).isEqualTo("The Artist")
        assertThat(result.genres).isEmpty()
        assertThat(result.popularity).isEqualTo(42)
    }

    @Test
    fun `fromApi throws when id is blank`() {
        val api = validArtistObject().apply { id = "" }

        assertThatThrownBy { SafeArtistObject.fromApi(api) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("id")
    }

    @Test
    fun `fromApi throws when name is null`() {
        val api = validArtistObject().apply { name = null }

        assertThatThrownBy { SafeArtistObject.fromApi(api) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("name")
    }

    @Test
    fun `fromApi throws when genres is null`() {
        val api = validArtistObject().apply { genres = null }

        assertThatThrownBy { SafeArtistObject.fromApi(api) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("genres")
    }

    private fun validArtistObject() = ArtistObject().apply {
        id = "artist-1"
        name = "The Artist"
        genres = ArrayList()
        href = "https://api.spotify.com/v1/artists/artist-1"
        images = ArrayList()
        externalUrls = ExternalUrlObject()
        followers = FollowersObject()
        popularity = 42
    }
}
