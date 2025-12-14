package org.taonity.artistinsightservice.infrastructure.exception

data class ClientErrorResponse(
    val clientErrorCode: ClientErrorCode,
    val errorMessage: String
)