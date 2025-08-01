package org.taonity.artistinsightservice.mvc

import org.taonity.spotify.model.ArtistObject

data class EnrichableArtistObject(
    val artistObject: ArtistObject,
    val genreEnriched: Boolean
)
