package org.taonity.artistinsightservice.artist.entity

import jakarta.persistence.*
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
        if (javaClass != other?.javaClass) return false

        other as ArtistGenreEntity

        if (artist.artistId != other.artist.artistId) return false
        if (genre != other.genre) return false

        return true
    }

    override fun hashCode(): Int = Objects.hash(artist.artistId, genre)
}

data class ArtistGenreId(
    val artist: String = "",
    val genre: String = ""
) : Serializable
