package org.taonity.artistinsightservice.share.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "shared_link_artist")
class SharedLinkArtistEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_link_id", nullable = false)
    val sharedLink: SharedLinkEntity,

    val artistId: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SharedLinkArtistEntity) return false
        return sharedLink.id == other.sharedLink.id && artistId == other.artistId
    }

    override fun hashCode(): Int = Objects.hash(sharedLink.id, artistId)
}
