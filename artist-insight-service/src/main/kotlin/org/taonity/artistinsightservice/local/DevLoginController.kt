package org.taonity.artistinsightservice.local

import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Profile("local & stub-spotify & !prod & !stage")
class DevLoginController {
    @GetMapping("/dev/stub-users")
    fun users() =
        listOf("owner", "admin", "user").map {
            mapOf("registrationId" to "stub-$it", "label" to "Local $it")
        }
}
