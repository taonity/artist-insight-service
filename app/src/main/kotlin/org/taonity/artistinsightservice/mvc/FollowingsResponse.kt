package org.taonity.artistinsightservice.mvc

data class FollowingsResponse(
    val artists: List<EnrichableArtistObject>,
    val genreEnriched: Boolean
)
