package org.taonity.artistinsightservice.persistence.user

import jakarta.persistence.*
import org.taonity.artistinsightservice.persistence.spotify_user_enriched_artists.SpotifyUserEnrichedArtistsEntity

@Entity
@Table(name = "spotify_user")
class SpotifyUserEntity(
    @Id
    var spotifyId: String,
    var displayName: String,
    var tokenValue: String,
    var gptUsagesLeft: Int,
//    @OneToMany(mappedBy = "user")
//    val enrichedArtists: List<SpotifyUserEnrichedArtistsEntity> = emptyList()
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