package org.taonity.artistinsightservice.persistence.genre

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ArtistGenreRepository : CrudRepository<ArtistGenreEntity, ArtistGenreId> {

    @Query("SELECT ag.genre FROM ArtistGenreEntity ag WHERE ag.artist.artistId = :artistId")
    fun findGenresByArtistId(@Param("artistId") artistId: String): List<String>
}