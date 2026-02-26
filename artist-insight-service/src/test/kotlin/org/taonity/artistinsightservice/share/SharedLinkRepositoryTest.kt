package org.taonity.artistinsightservice.share

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.jdbc.Sql
import org.taonity.artistinsightservice.share.repository.SharedLinkRepository

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