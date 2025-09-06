package org.taonity.artistinsightservice.spotify

class SpotifyClientException: RuntimeException {
    constructor(message: String, ex: Exception?): super(message, ex)
    constructor(message: String): super(message)
}