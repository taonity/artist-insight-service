package org.taonity.artistinsightservice.share.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.taonity.artistinsightservice.share.entity.SharedLinkEntity
import java.util.*

@Repository
interface SharedLinkRepository : CrudRepository<SharedLinkEntity, UUID> {

    @Query("SELECT sl FROM SharedLinkEntity sl WHERE sl.user.spotifyId = :userId")
    fun findByUserId(@Param("userId") userId: String): SharedLinkEntity?

    @Query("""
        SELECT sl FROM SharedLinkEntity sl 
        LEFT JOIN FETCH sl.artists 
        WHERE sl.shareCode = :shareCode
    """)
    fun findByShareCodeWithArtists(@Param("shareCode") shareCode: String): SharedLinkEntity?

    fun deleteByUserSpotifyId(spotifyId: String)
}
