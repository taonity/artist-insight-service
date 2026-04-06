package org.taonity.artistinsightservice.user.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.taonity.artistinsightservice.user.repository.UserArtistLinkRepository

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
@Sql("classpath:sql/test-data.sql")
@ActiveProfiles("datasource-proxy", "postgres")
@Rollback(false)
class UserArtistLinkRepositoryTest {
    @Autowired
    lateinit var userArtistLinkRepository: UserArtistLinkRepository

}