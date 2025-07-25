package org.taonity.artistinsightservice.mvc

data class SpotifyResponse<T>(
    val artists: T
)