package org.taonity.artistinsightservice.persistence.genre

data class ArtistGenresAndUserLinkDto(
    val genres: List<String>,
    val userHasArtist: Boolean
)
