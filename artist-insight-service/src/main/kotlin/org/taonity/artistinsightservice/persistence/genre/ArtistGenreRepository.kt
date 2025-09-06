package org.taonity.artistinsightservice.persistence.genre

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ArtistGenreRepository : CrudRepository<ArtistGenreEntity, ArtistGenreId>, ArtistGenreRepositoryCustom
