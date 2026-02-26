package org.taonity.artistinsightservice.share.entity

import jakarta.persistence.*
import org.taonity.artistinsightservice.user.entity.SpotifyUserEntity
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "shared_link")
class SharedLinkEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    val user: SpotifyUserEntity,

    val shareCode: String,

    val createdAt: OffsetDateTime = OffsetDateTime.now(),

    var expiresAt: OffsetDateTime,

    @OneToMany(mappedBy = "sharedLink", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
    val artists: MutableSet<SharedLinkArtistEntity> = mutableSetOf()
) {
    fun addArtists(artistIds: List<String>) {
        artistIds.forEach { artistId ->
            artists.add(SharedLinkArtistEntity(sharedLink = this, artistId = artistId))
        }
    }

    fun isExpired(): Boolean = OffsetDateTime.now().isAfter(expiresAt)

    override fun hashCode(): Int = id.hashCode()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SharedLinkEntity

        return id == other.id
    }
}
