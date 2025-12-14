package org.taonity.artistinsightservice.artist.dto

data class SpotifyResponse<T>(
    val artists: T
)