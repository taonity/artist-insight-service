package org.taonity.artistinsightservice.mvc

import org.taonity.artistinsightservice.SafePrivateUserObject

data class SpotifyUserDto(
    val privateUserObject: SafePrivateUserObject,
    val gptUsagesLeft: Int
)