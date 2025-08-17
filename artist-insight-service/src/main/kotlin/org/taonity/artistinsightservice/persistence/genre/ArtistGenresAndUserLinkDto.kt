package org.taonity.artistinsightservice.persistence.genre

import java.lang.Boolean

data class ArtistGenresAndUserLinkDto(
    val genres: List<String>,
    val userHasArtist: Boolean
)
