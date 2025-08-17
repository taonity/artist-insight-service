package org.taonity.artistinsightservice

import jakarta.validation.Validation
import mu.KotlinLogging
import org.junit.jupiter.api.Test
import org.taonity.spotify.model.ArtistObject

//TODO: improve test
class FollowingsServiceTest {

    companion object {
        private val LOGGER = KotlinLogging.logger {}
    }

    @Test
    fun fetchRawFollowings() {
        val validator = Validation.buildDefaultValidatorFactory().getValidator();
        val artist = ArtistObject()
        artist.name = "dfdf"
        artist.id = "dfdf"
        artist.genres = ArrayList()
        artist.href = "dfdf"
        artist.images = ArrayList()
        val list = listOf(
            artist
        )
        val output = list.map(ValidatedArtistObject::of)
            .filter { validatedArtistObject ->
                val violations = validator.validate(validatedArtistObject)
                if (violations.isEmpty()) {
                    return@filter true
                }
                val errorMessage = violations.joinToString("; ") { "${it.propertyPath}: ${it.message}" }
                LOGGER.warn { "Validation failed: $errorMessage" }
                return@filter false
            }
            .map(ValidatedArtistObject::toSafe)

        println(output)
    }
}