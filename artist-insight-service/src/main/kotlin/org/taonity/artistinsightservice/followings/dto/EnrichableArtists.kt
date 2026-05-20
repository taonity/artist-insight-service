package org.taonity.artistinsightservice.followings.dto

import org.taonity.artistinsightservice.integration.spotify.dto.SafeArtistObject

data class EnrichableArtists(
    val artistObject: SafeArtistObject,
    val genreEnriched: Boolean = true,
    val notEnoughUserGptUsages: Boolean = false,
    val notEnoughGlobalGptUsages: Boolean = false
)
