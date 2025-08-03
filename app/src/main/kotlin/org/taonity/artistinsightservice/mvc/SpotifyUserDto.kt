package org.taonity.artistinsightservice.mvc

import org.taonity.artistinsightservice.SafePrivateUserObject
import org.taonity.spotify.model.PrivateUserObject

data class SpotifyUserDto(
    val privateUserObject: SafePrivateUserObject,
    val gptUsagesLeft: Int
)