package org.taonity.artistinsightservice.mvc

import org.taonity.artistinsightservice.SafeArtistObject

data class EnrichableArtists(
    val artistObject: SafeArtistObject,
    val genreEnriched: Boolean = true,
    val notEnoughUserGptUsages: Boolean = false,
    val notEnoughGlobalGptUsages: Boolean = false
)
