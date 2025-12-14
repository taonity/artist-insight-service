package org.taonity.artistinsightservice.artist.service

data class ArtistEnrichmentInfo(
    val genres: List<String>,
    val isLinkedToUser: Boolean
)
