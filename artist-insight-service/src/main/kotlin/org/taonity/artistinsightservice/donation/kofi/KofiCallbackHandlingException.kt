package org.taonity.artistinsightservice.donation.kofi

class KofiCallbackHandlingException : RuntimeException {
    constructor(message: String, ex: Exception?): super(message, ex)
    constructor(message: String): super(message)
}