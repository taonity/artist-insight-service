package org.taonity.artistinsightservice.persistence.genre

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ArtistGenreService(
    private val artistGenreRepository: ArtistGenreRepository
) {

    @Transactional(readOnly = true)
    fun getGenresAndUserStatus(artistId: String, spotifyId: String): ArtistGenresAndUserLinkDto {
        return artistGenreRepository.getGenresAndUserLink(artistId, spotifyId)
    }

//    fun getGenres(artistId: String): List<String> {
//        return artistGenreRepository.findAllByArtist_ArtistId(artistId)
//            .map { it.genre }
//    }

//    fun saveGenres(artistName: String, genres: List<String>) {
//        val entities = genres.map { genre -> ArtistGenreEntity(artistName, genre) }
//        artistGenreRepository.saveAll(entities)
//    }

}
