package org.taonity.artistinsightservice.followings.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.taonity.spotify.model.ImageObject
import org.taonity.spotify.model.PrivateUserObject

data class SafePrivateUserObject(
    val id: String,
    val displayName: String,
    val images: MutableList<ImageObject>
)

data class ValidatedPrivateUserObject(
    @field:NotBlank
    @field:NotNull
    val id: String?,
    @field:NotBlank
    @field:NotNull
    val displayName: String?,
    @field:NotNull
    val images: MutableList<ImageObject>?

) {
    companion object {
        fun of(privateUserObject: PrivateUserObject) : ValidatedPrivateUserObject {
            return ValidatedPrivateUserObject(
                privateUserObject.id,
                privateUserObject.displayName,
                privateUserObject.images
            )
        }
    }
    fun toSafe(): SafePrivateUserObject {
        return SafePrivateUserObject(
            id!!,
            displayName!!,
            images!!,
        )
    }
}