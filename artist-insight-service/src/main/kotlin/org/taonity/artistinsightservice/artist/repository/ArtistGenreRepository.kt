package org.taonity.artistinsightservice.artist.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.taonity.artistinsightservice.artist.entity.ArtistGenreEntity
import org.taonity.artistinsightservice.artist.entity.ArtistGenreId

@Repository
interface ArtistGenreRepository : CrudRepository<ArtistGenreEntity, ArtistGenreId> {

    @Query("SELECT ag.genre FROM ArtistGenreEntity ag WHERE ag.artist.artistId = :artistId")
    fun findGenresByArtistId(@Param("artistId") artistId: String): List<String>
}
