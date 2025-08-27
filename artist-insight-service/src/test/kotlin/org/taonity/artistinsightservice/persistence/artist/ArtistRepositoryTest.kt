package org.taonity.artistinsightservice.persistence.artist

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.jdbc.Sql

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Sql("classpath:sql/test-data.sql")
@Disabled("Manual only")
class ArtistRepositoryTest {

    @Autowired
    lateinit var artistsRepository: ArtistRepository

    @Test
    fun testFindAllBySpotifyUser_SpotifyId() {
        println(artistsRepository.findAllByUserIdWithGenres("3126nx54y24ryqyza3qxcchi4wry"))
    }
}