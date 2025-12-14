package org.taonity.artistinsightservice.user.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.taonity.artistinsightservice.user.entity.UserArtistLinkEntity
import org.taonity.artistinsightservice.user.entity.UserArtistLinkId

@Repository
interface UserArtistLinkRepository : CrudRepository<UserArtistLinkEntity, UserArtistLinkId> {
    fun deleteAllByUserSpotifyId(spotifyId: String)
    fun existsByUserSpotifyIdAndArtistArtistId(spotifyId: String, artistId: String): Boolean
}
