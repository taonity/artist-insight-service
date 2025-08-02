package org.taonity.artistinsightservice.persistence.artist

import org.springframework.data.repository.CrudRepository

interface ArtistRepository : CrudRepository<ArtistEntity, String>