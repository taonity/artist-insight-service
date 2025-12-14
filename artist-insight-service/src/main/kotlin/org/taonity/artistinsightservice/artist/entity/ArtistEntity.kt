package org.taonity.artistinsightservice.artist.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "artist")
class ArtistEntity(
    @Id
    val artistId: String,

    val artistName: String,

    @OneToMany(mappedBy = "artist", fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST, CascadeType.MERGE], orphanRemoval = true)
    val genres: MutableSet<ArtistGenreEntity> = mutableSetOf()
) {
    fun addGenre(genre: String) {
        genres.add(ArtistGenreEntity(this, genre))
    }

    fun addGenres(genreNames: List<String>) {
        genreNames.forEach { addGenre(it) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArtistEntity) return false
        return artistId == other.artistId
    }

    override fun hashCode(): Int = artistId.hashCode()
}
