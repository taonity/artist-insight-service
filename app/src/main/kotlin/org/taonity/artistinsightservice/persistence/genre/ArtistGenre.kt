package org.taonity.artistinsightservice.persistence.genre

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table

@Entity
@Table(name = "artist_genres")
@IdClass(ArtistGenreId::class)
data class ArtistGenre(
    @Id val artistName: String,
    @Id val genre: String
)