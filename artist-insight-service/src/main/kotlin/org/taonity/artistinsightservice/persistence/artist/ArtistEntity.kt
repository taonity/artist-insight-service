package org.taonity.artistinsightservice.persistence.artist

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import org.taonity.artistinsightservice.persistence.genre.ArtistGenreEntity

@Entity
@Table(name = "artist")
class ArtistEntity(
    @Id
    val artistId: String,

    val artistName: String,

    // TODO: something is wrong here
    @OneToMany(mappedBy = "artist")
    val genres: List<ArtistGenreEntity> = emptyList()
)
