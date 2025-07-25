package org.taonity.artistinsightservice.persistence.genre

import org.springframework.stereotype.Service

@Service
class ArtistGenreService(
    private val artistGenreRepository: ArtistGenreRepository
) {
    fun getGenres(artistName: String): List<String> {
        return artistGenreRepository.findAllByArtistName(artistName)
            .map { it.genre }
    }

    fun saveGenres(artistName: String, genres: List<String>) {
        val entities = genres.map { genre -> ArtistGenre(artistName, genre) }
        artistGenreRepository.saveAll(entities)
    }

}
