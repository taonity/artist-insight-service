package org.taonity.artistinsightservice.devaccess

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DevAccessRepository : JpaRepository<DevAccessRequestEntity, Long>