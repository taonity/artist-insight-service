package org.taonity.artistinsightservice.devaccess.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.taonity.artistinsightservice.devaccess.entity.DevAccessRequestEntity

@Repository
interface DevAccessRepository : JpaRepository<DevAccessRequestEntity, Long>