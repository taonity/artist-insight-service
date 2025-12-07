package org.taonity.artistinsightservice.persistence.artist

data class ArtistEnrichmentInfo(
    val genres: List<String>,
    val isLinkedToUser: Boolean
)
