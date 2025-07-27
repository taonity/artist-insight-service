package org.taonity.artistinsightservice.mvc

import org.taonity.spotify.model.PrivateUserObject

data class SpotifyUserDto(
    val privateUserObject: PrivateUserObject,
    val gptUsagesLeft: Int
)