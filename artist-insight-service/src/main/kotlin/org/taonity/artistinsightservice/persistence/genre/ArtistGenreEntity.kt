package org.taonity.artistinsightservice.persistence.genre

import jakarta.persistence.*
import org.taonity.artistinsightservice.persistence.artist.ArtistEntity
import java.io.Serializable
import java.util.Objects

@Entity
@Table(name = "artist_genres")
@IdClass(ArtistGenreId::class)
class ArtistGenreEntity(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", nullable = false)
    val artist: ArtistEntity,
    @Id
    val genre: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArtistGenreEntity) return false
        return artist.artistId == other.artist.artistId && genre == other.genre
    }

    override fun hashCode(): Int = Objects.hash(artist.artistId, genre)
}

data class ArtistGenreId(
    val artist: String = "",
    val genre: String = ""
) : Serializable