package org.taonity.artistinsightservice.user

import org.taonity.artistinsightservice.followings.dto.SafePrivateUserObject

data class SpotifyUserDto(
    val privateUserObject: SafePrivateUserObject,
    val gptUsagesLeft: Int
)