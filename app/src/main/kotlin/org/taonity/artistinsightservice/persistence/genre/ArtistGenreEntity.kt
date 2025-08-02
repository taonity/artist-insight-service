package org.taonity.artistinsightservice.persistence.genre

import jakarta.persistence.*
import org.taonity.artistinsightservice.persistence.artist.ArtistEntity
import java.io.Serializable

@Entity
@Table(name = "artist_genres")
@IdClass(ArtistGenreId::class)
data class ArtistGenreEntity(
    @Id
    @ManyToOne
    @JoinColumn(name = "artist_id", nullable = false)
    val artist: ArtistEntity,
    @Id
    val genre: String
)

@Embeddable
data class ArtistGenreId(
    val artist: String,
    val genre: String
) : Serializable