package org.taonity.artistinsightservice.artist.repository

import net.ttddyy.dsproxy.QueryCountHolder
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Sql("classpath:sql/test-data.sql")
@ActiveProfiles("datasource-proxy", "h2")
@Disabled("Manual only")
class ArtistRepositoryTest {

    @Autowired
    lateinit var artistsRepository: ArtistRepository

    @Test
    fun `finds artists with genres eagerly fetched within 1 query`() {
        val selectsBefore = QueryCountHolder.getGrandTotal().select
        val artists = artistsRepository.findByUserIdAndArtistIdsWithGenres("3126nx54y24ryqyza3qxcchi4wry", listOf("21bOoXa6JISSaqYu2oYbWy"))
        assertThat(artists).hasSize(1)
        assertThat(artists[0].genres).hasSize(4)
        Assertions.assertThat(QueryCountHolder.getGrandTotal().select - selectsBefore)
            .`as`("ArtistEntity list should be fetched with a single SELECT")
            .isEqualTo(1)
    }
}