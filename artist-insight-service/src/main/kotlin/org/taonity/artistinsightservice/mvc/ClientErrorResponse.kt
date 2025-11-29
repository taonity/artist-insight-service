package org.taonity.artistinsightservice.mvc

data class ClientErrorResponse(
    val clientErrorCode: ClientErrorCode,
    val errorMessage: String
)