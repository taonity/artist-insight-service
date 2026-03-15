package org.taonity.artistinsightservice

import jakarta.persistence.EntityManager
import net.ttddyy.dsproxy.QueryCountHolder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.taonity.artistinsightservice.artist.entity.ArtistGenreId
import org.taonity.artistinsightservice.artist.repository.ArtistGenreRepository
import org.taonity.artistinsightservice.artist.repository.ArtistRepository
import org.taonity.artistinsightservice.settings.repository.AppSettingsRepository
import org.taonity.artistinsightservice.share.repository.SharedLinkRepository
import org.taonity.artistinsightservice.user.entity.UserArtistLinkId
import org.taonity.artistinsightservice.user.repository.SpotifyUserRepository
import org.taonity.artistinsightservice.user.repository.UserArtistLinkRepository
import java.util.*

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Sql("classpath:sql/test-data.sql")
@ActiveProfiles("datasource-proxy", "h2")
class LazyFetchingArchitectureTest {

    @Autowired lateinit var entityManager: EntityManager
    @Autowired lateinit var spotifyUserRepository: SpotifyUserRepository
    @Autowired lateinit var artistRepository: ArtistRepository
    @Autowired lateinit var artistGenreRepository: ArtistGenreRepository
    @Autowired lateinit var userArtistLinkRepository: UserArtistLinkRepository
    @Autowired lateinit var sharedLinkRepository: SharedLinkRepository
    @Autowired lateinit var appSettingsRepository: AppSettingsRepository

    @TestFactory
    fun `findById executes exactly one select`(): List<DynamicTest> {
        val cases = listOf(
            "SpotifyUserEntity" to { spotifyUserRepository.findById("3126nx54y24ryqyza3qxcchi4wry") },
            "ArtistEntity" to { artistRepository.findById("21bOoXa6JISSaqYu2oYbWy") },
            "ArtistGenreEntity" to { artistGenreRepository.findById(ArtistGenreId(artist = "21bOoXa6JISSaqYu2oYbWy", genre = "Folk")) },
            "UserArtistLinkEntity" to { userArtistLinkRepository.findById(UserArtistLinkId(user = "3126nx54y24ryqyza3qxcchi4wry", artist = "21bOoXa6JISSaqYu2oYbWy")) },
            "SharedLinkEntity" to { sharedLinkRepository.findById(UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890")) },
            "AppSettingsEntity" to { appSettingsRepository.findById(0) },
        )

        return cases.map { (entityName, repoCall) ->
            dynamicTest(entityName) {
                entityManager.flush()
                entityManager.clear()
                val selectsBefore = QueryCountHolder.getGrandTotal().select
                repoCall()
                assertThat(QueryCountHolder.getGrandTotal().select - selectsBefore)
                    .`as`("$entityName should be fetched with a single SELECT")
                    .isEqualTo(1)
            }
        }
    }
}

