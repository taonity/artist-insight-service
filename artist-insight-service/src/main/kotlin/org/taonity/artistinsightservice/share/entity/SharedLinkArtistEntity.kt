package org.taonity.artistinsightservice.share.entity

import jakarta.persistence.*
import org.taonity.artistinsightservice.artist.entity.ArtistEntity
import java.io.Serializable
import java.util.*

@Entity
@Table(name = "shared_link_artist")
@IdClass(SharedLinkArtistId::class)
class SharedLinkArtistEntity(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_link_id", nullable = false)
    var sharedLink: SharedLinkEntity,

    @Id
    @Column(name = "artist_id", nullable = false)
    var artistId: String,
) {
    @JvmField
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", insertable = false, updatable = false)
    var artist: ArtistEntity? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SharedLinkArtistEntity) return false
        return sharedLink.id == other.sharedLink.id && artistId == other.artistId
    }

    override fun hashCode(): Int = Objects.hash(sharedLink.id, artistId)
}

data class SharedLinkArtistId(
    val sharedLink: UUID = UUID.randomUUID(),
    val artistId: String = ""
) : Serializable