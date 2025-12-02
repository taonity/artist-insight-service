package org.taonity.artistinsightservice.persistence.genre

data class ArtistEnrichmentInfo(
    val genres: List<String>,
    val isLinkedToUser: Boolean
)
