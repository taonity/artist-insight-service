package org.taonity.artistinsightservice.persistence.genre

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.stereotype.Repository

interface ArtistGenreRepositoryCustom {
    fun getGenresAndUserLink(artistId: String, spotifyId: String): ArtistGenresAndUserLinkDto
}

@Repository
class ArtistGenreRepositoryImpl(
    @PersistenceContext private val em: EntityManager
) : ArtistGenreRepositoryCustom {

    override fun getGenresAndUserLink(artistId: String, spotifyId: String): ArtistGenresAndUserLinkDto {
        val genres = em.createQuery(
            """
            SELECT ag.genre 
            FROM ArtistGenreEntity ag
            WHERE ag.artist.artistId = :artistId
            """, String::class.java
        ).setParameter("artistId", artistId)
            .resultList

        val hasArtist = em.createQuery(
            """
            SELECT COUNT(e) > 0 
            FROM SpotifyUserEnrichedArtistsEntity e
            WHERE e.user.spotifyId = :spotifyId AND e.artist.artistId = :artistId
            """, Boolean::class.java
        ).setParameter("spotifyId", spotifyId)
            .setParameter("artistId", artistId)
            .singleResult

        return ArtistGenresAndUserLinkDto(genres, hasArtist)
    }
}