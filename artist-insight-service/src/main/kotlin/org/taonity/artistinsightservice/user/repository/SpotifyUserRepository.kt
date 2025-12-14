package org.taonity.artistinsightservice.user.repository

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.taonity.artistinsightservice.user.entity.SpotifyUserEntity

@Repository
interface SpotifyUserRepository : CrudRepository<SpotifyUserEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM SpotifyUserEntity u WHERE u.spotifyId = :spotifyId")
    fun findByIdForUpdate(@Param("spotifyId") spotifyId: String): SpotifyUserEntity?
}
