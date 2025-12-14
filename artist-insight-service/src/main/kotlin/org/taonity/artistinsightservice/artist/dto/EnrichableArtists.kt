package org.taonity.artistinsightservice.artist.dto

data class EnrichableArtists(
    val artistObject: SafeArtistObject,
    val genreEnriched: Boolean = true,
    val notEnoughUserGptUsages: Boolean = false,
    val notEnoughGlobalGptUsages: Boolean = false
)
