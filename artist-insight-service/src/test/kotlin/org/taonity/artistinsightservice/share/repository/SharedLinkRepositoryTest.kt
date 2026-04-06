package org.taonity.artistinsightservice.share.repository

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
@Disabled("Manual only")
@ActiveProfiles("datasource-proxy")
class SharedLinkRepositoryTest {

    @Autowired
    lateinit var sharedLinkRepository: SharedLinkRepository

    @Test
    fun testRepo() {
        println(sharedLinkRepository.findByUserId("3126nx54y24ryqyza3qxcchi4wry"))
    }
}