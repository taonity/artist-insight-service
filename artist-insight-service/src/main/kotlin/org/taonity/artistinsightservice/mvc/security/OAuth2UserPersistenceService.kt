package org.taonity.artistinsightservice.mvc.security

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.validation.Validator
import mu.KotlinLogging
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.SafePrivateUserObject
import org.taonity.artistinsightservice.ValidatedPrivateUserObject
import org.taonity.artistinsightservice.persistence.user.SpotifyUserService
import org.taonity.spotify.model.PrivateUserObject

@Service
class OAuth2UserPersistenceService(
    private val spotifyUserService: SpotifyUserService,
    private val validator: Validator
) : DefaultOAuth2UserService() {

    companion object {
        private val objectMapper = jacksonObjectMapper()
        private val LOGGER = KotlinLogging.logger {}
    }

    override fun loadUser(userRequest: OAuth2UserRequest?): OAuth2User {

        val oAuth2User: OAuth2User = try {
            super.loadUser(userRequest)
        } catch (e: OAuth2AuthenticationException) {
            LOGGER.error { e.message }
            throw e
        }

        val privateUserObject: PrivateUserObject = try {
            objectMapper.convertValue<PrivateUserObject>(oAuth2User.attributes)
        } catch (e: Exception) {
            throw RuntimeException("Failed to covert PrivateUserObject attributes map: ${oAuth2User.attributes}", e)
        }
        val safePrivateUserObject = validatePrivateUserObjectOrThrow(privateUserObject)

        val spotifyUserPrincipal: SpotifyUserPrincipal = SpotifyUserPrincipal.of(safePrivateUserObject, oAuth2User)
        spotifyUserService.createOrUpdateUser(spotifyUserPrincipal, userRequest!!.accessToken.tokenValue)
        return spotifyUserPrincipal
    }

    private fun validatePrivateUserObjectOrThrow(privateUserObject: PrivateUserObject): SafePrivateUserObject {
        val validationPrivateUserObject = ValidatedPrivateUserObject.of(privateUserObject)
        val violations = validator.validate(validationPrivateUserObject)
        val safePrivateUserObject = if (violations.isEmpty()) {
            validationPrivateUserObject.toSafe()
        } else {
            val errorMessage = violations.joinToString("; ") { "${it.propertyPath}: ${it.message}" }
            throw RuntimeException("Validation failed for user $validationPrivateUserObject with error: $errorMessage")
        }
        return safePrivateUserObject
    }

}