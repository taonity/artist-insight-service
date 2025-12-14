package org.taonity.artistinsightservice.user.dto

import org.taonity.artistinsightservice.artist.dto.SafePrivateUserObject

data class SpotifyUserDto(
    val privateUserObject: SafePrivateUserObject,
    val gptUsagesLeft: Int
)