package org.taonity.artistinsightservice.mvc

import org.taonity.spotify.model.ArtistObject

data class FollowingsResponse(
    val artists: List<ArtistObject>,
    val genreEnriched: Boolean
)
