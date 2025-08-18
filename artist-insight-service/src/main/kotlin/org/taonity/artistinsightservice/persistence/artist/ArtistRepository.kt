package org.taonity.artistinsightservice.persistence.artist

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface ArtistRepository : CrudRepository<ArtistEntity, String> {

    @Query("""
        SELECT DISTINCT a 
        FROM SpotifyUserEnrichedArtistsEntity uea
        JOIN uea.artist a
        LEFT JOIN FETCH a.genres g
        WHERE uea.user.spotifyId = :spotifyId
    """)
    fun findAllByUserIdWithGenres(@Param("spotifyId") spotifyId: String): List<ArtistEntity>
}