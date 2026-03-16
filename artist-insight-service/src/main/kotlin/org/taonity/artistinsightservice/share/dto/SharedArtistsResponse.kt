package org.taonity.artistinsightservice.share.dto

data class SharedArtistsResponse(
    val owner: ShareOwnerInfo,
    val artists: List<SharedArtist>
)
