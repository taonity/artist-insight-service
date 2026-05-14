package org.taonity.artistinsightservice.share.repository

import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.taonity.artistinsightservice.share.entity.SharedLinkArtistEntity
import org.taonity.artistinsightservice.share.entity.SharedLinkArtistId
import java.util.UUID

@Repository
interface SharedLinkArtistRepository : CrudRepository<SharedLinkArtistEntity, SharedLinkArtistId> {

    @Modifying
    @Query("DELETE FROM SharedLinkArtistEntity sla WHERE sla.sharedLink.id = :sharedLinkId")
    fun deleteAllBySharedLinkId(@Param("sharedLinkId") sharedLinkId: UUID): Int
}
