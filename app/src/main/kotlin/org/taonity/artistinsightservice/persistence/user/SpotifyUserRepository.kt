package org.taonity.artistinsightservice.persistence.user

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface SpotifyUserRepository : CrudRepository<SpotifyUserEntity, Long> {
    fun findBySpotifyId(spotifyId: String): SpotifyUserEntity?
}