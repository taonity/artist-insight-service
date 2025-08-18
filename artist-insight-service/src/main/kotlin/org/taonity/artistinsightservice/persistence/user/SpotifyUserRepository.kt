package org.taonity.artistinsightservice.persistence.user

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SpotifyUserRepository : CrudRepository<SpotifyUserEntity, String> {
    fun findBySpotifyId(spotifyId: String): SpotifyUserEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from SpotifyUserEntity u where u.spotifyId = :spotifyId")
    fun findBySpotifyIdForUpdate(@Param("spotifyId") spotifyId: String): SpotifyUserEntity?
}
