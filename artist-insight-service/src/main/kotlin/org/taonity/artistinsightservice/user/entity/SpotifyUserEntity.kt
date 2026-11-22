package org.taonity.artistinsightservice.user.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.taonity.artistinsightservice.artist.entity.ArtistEntity
import org.taonity.artistinsightservice.console.entity.ConsoleRole

@Entity
@Table(name = "spotify_user")
class SpotifyUserEntity(
    @Id val spotifyId: String,
    var displayName: String,
    var gptUsagesLeft: Int,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var consoleRole: ConsoleRole = ConsoleRole.NONE,
    @Column(name = "avatar_url") var avatarUrl: String? = null,
    @OneToMany(
        mappedBy = "user",
        fetch = FetchType.LAZY,
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
    )
    val enrichedArtists: MutableSet<UserArtistLinkEntity> = mutableSetOf(),
    @Column(length = 320) var email: String? = null,
) {
    fun addEnrichedArtist(artist: ArtistEntity) {
        val link = UserArtistLinkEntity(this, artist)
        if (link !in enrichedArtists) {
            enrichedArtists.add(link)
        }
    }

    override fun hashCode(): Int = spotifyId.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SpotifyUserEntity

        return spotifyId == other.spotifyId
    }

    override fun toString(): String {
        return "SpotifyUser(spotifyId='$spotifyId', displayName='$displayName', gptUsagesLeft=$gptUsagesLeft)"
    }

    fun updateDetails(displayName: String): SpotifyUserEntity {
        this.displayName = displayName
        return this
    }
}
