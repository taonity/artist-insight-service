package org.taonity.artistinsightservice.persistence.user

import jakarta.persistence.*

@Entity
@SequenceGenerator(name = "default_generator", sequenceName = "spotify_user_seq", allocationSize = 1)
@Table(name = "spotify_user")
data class SpotifyUserEntity(
    @Id
    var spotifyId: String,
    var displayName: String,
    var tokenValue: String,
    var gptUsagesLeft: Int
) {
    override fun toString(): String {
        return "SpotifyUser(spotifyId='$spotifyId', displayName='$displayName', tokenValue='$tokenValue', gptUsagesLeft=$gptUsagesLeft)"
    }

    fun updateDetails(displayName: String, tokenValue: String): SpotifyUserEntity {
        this.displayName = displayName
        this.tokenValue = tokenValue
        return this
    }
}