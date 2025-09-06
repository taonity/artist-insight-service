package org.taonity.artistinsightservice.openai

class OpenAITimeoutException: RuntimeException {
    constructor(message: String, ex: Exception?): super(message, ex)
    constructor(message: String): super(message)
}