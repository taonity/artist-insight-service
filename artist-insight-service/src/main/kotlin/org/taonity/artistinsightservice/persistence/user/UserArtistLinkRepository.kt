package org.taonity.artistinsightservice.persistence.user

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserArtistLinkRepository : CrudRepository<UserArtistLinkEntity, UserArtistLinkId> {
    fun deleteAllByUserSpotifyId(spotifyId: String)
    fun existsByUserSpotifyIdAndArtistArtistId(spotifyId: String, artistId: String): Boolean
}
