package org.taonity.artistinsightservice.mvc

import org.taonity.artistinsightservice.SafeArtistObject
import org.taonity.spotify.model.ArtistObject

data class EnrichableArtistObject(
    val artistObject: SafeArtistObject,
    val genreEnriched: Boolean
)
