package org.taonity.artistinsightservice.integration.openai.exception

class OpenAIClientException: RuntimeException {
    constructor(message: String, ex: Exception?): super(message, ex)
    constructor(message: String): super(message)
}