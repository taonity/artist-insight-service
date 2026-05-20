package org.taonity.artistinsightservice.integration.spotify.dto

data class SpotifyResponse<T>(
    val artists: T
)
