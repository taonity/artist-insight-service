package org.taonity.artistinsightservice.integration.spotify.exception

class SpotifyTimeoutException: RuntimeException {
    constructor(message: String, ex: Exception?): super(message, ex)
    constructor(message: String): super(message)
}