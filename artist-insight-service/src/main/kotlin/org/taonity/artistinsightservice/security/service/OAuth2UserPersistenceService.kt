package org.taonity.artistinsightservice.security.service

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.validation.Validator
import mu.KotlinLogging
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service
import org.taonity.artistinsightservice.artist.dto.SafePrivateUserObject
import org.taonity.artistinsightservice.artist.dto.ValidatedPrivateUserObject
import org.taonity.artistinsightservice.infrastructure.utils.validateOrThrow
import org.taonity.artistinsightservice.security.principal.SpotifyUserPrincipal
import org.taonity.artistinsightservice.user.service.SpotifyUserService
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
        val validatedUserRequest = requireNotNull(userRequest) { "OAuth2UserRequest must not be null" }

        val oAuth2User: OAuth2User = try {
            super.loadUser(validatedUserRequest)
        } catch (e: OAuth2AuthenticationException) {
            LOGGER.error(e) { "OAuth2 user loading failed" }
            throw e
        }

        val privateUserObject: PrivateUserObject = try {
            objectMapper.convertValue<PrivateUserObject>(oAuth2User.attributes)
        } catch (e: Exception) {
            throw RuntimeException("Failed to covert PrivateUserObject attributes map: ${oAuth2User.attributes}", e)
        }
        val safePrivateUserObject = validatePrivateUserObjectOrThrow(privateUserObject)

        val spotifyUserPrincipal: SpotifyUserPrincipal = SpotifyUserPrincipal.of(safePrivateUserObject, oAuth2User)
        spotifyUserService.createOrUpdateUser(spotifyUserPrincipal, validatedUserRequest.accessToken.tokenValue)
        return spotifyUserPrincipal
    }

    private fun validatePrivateUserObjectOrThrow(privateUserObject: PrivateUserObject): SafePrivateUserObject {
        val validatedPrivateUserObject = ValidatedPrivateUserObject.of(privateUserObject)
        validator.validateOrThrow(validatedPrivateUserObject) { errorMessage ->
            RuntimeException("Validation failed for user $validatedPrivateUserObject with error: $errorMessage")
        }
        return validatedPrivateUserObject.toSafe()
    }

}