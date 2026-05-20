package org.taonity.artistinsightservice.web.exception

data class ClientErrorResponse(
    val clientErrorCode: ClientErrorCode,
    val errorMessage: String
)