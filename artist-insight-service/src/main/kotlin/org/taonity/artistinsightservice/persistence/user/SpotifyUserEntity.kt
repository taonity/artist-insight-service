package org.taonity.artistinsightservice.persistence.user

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.taonity.artistinsightservice.persistence.artist.ArtistEntity

@Entity
@Table(name = "spotify_user")
class SpotifyUserEntity(
    @Id
    val spotifyId: String,
    var displayName: String,
    var tokenValue: String,
    var gptUsagesLeft: Int,

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE], orphanRemoval = true)
    val enrichedArtists: MutableSet<UserArtistLinkEntity> = mutableSetOf()
) {
    fun addEnrichedArtist(artist: ArtistEntity) {
        val link = UserArtistLinkEntity(this, artist)
        if (link !in enrichedArtists) {
            enrichedArtists.add(link)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SpotifyUserEntity) return false
        return spotifyId == other.spotifyId
    }

    override fun hashCode(): Int = spotifyId.hashCode()

    override fun toString(): String {
        return "SpotifyUser(spotifyId='$spotifyId', displayName='$displayName', gptUsagesLeft=$gptUsagesLeft)"
    }

    fun updateDetails(displayName: String, tokenValue: String): SpotifyUserEntity {
        this.displayName = displayName
        this.tokenValue = tokenValue
        return this
    }
}