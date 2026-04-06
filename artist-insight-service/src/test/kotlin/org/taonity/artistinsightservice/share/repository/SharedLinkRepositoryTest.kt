package org.taonity.artistinsightservice.share.repository

import org.assertj.core.api.Assertions.assertThat
import org.hibernate.Hibernate
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import java.util.UUID

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Sql("classpath:sql/test-data.sql")
@Disabled("Manual only")
@ActiveProfiles("h2", "datasource-proxy")
class SharedLinkRepositoryTest {

    @Autowired
    lateinit var sharedLinkRepository: SharedLinkRepository

    @Test
    fun `shared link is fetched lazily`() {
        val sharedLink = sharedLinkRepository.findByUserId("3126nx54y24ryqyza3qxcchi4wry")
        assertThat(sharedLink?.id).isEqualTo(UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890"))
        assertThat(sharedLink?.user?.spotifyId).isEqualTo("3126nx54y24ryqyza3qxcchi4wry")
        assertThat(Hibernate.isInitialized(sharedLink?.user)).isFalse()
        assertThat(Hibernate.isInitialized(sharedLink?.artists)).isFalse()
        assertThat(sharedLink?.shareCode).isEqualTo("testCode")
    }
}