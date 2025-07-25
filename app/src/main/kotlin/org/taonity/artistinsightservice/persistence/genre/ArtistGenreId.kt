package org.taonity.artistinsightservice.persistence.genre

import jakarta.persistence.Embeddable
import java.io.Serializable

@Embeddable
data class ArtistGenreId(
    val artistName: String,
    val genre: String
) : Serializable
