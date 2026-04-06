package org.taonity.artistinsightservice.share.service

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.taonity.artistinsightservice.artist.dto.SafeArtistObject
import org.taonity.artistinsightservice.artist.entity.ArtistEntity
import org.taonity.artistinsightservice.artist.entity.ArtistGenreEntity
import org.taonity.artistinsightservice.artist.repository.ArtistRepository
import org.taonity.artistinsightservice.integration.spotify.service.SpotifyService
import org.taonity.artistinsightservice.share.entity.SharedLinkArtistEntity
import org.taonity.artistinsightservice.share.entity.SharedLinkEntity
import org.taonity.artistinsightservice.share.exception.ShareLinkExpiredException
import org.taonity.artistinsightservice.share.exception.ShareLinkNotFoundException
import org.taonity.artistinsightservice.share.repository.SharedLinkRepository
import org.taonity.artistinsightservice.user.entity.SpotifyUserEntity
import org.taonity.artistinsightservice.user.service.SpotifyUserService
import org.taonity.spotify.model.ExternalUrlObject
import org.taonity.spotify.model.FollowersObject
import org.taonity.spotify.model.PublicUserObject
import java.time.OffsetDateTime

class ShareServiceTest {

    private val sharedLinkRepository: SharedLinkRepository = mock()
    private val spotifyUserService: SpotifyUserService = mock()
    private val spotifyService: SpotifyService = mock()
    private val artistRepository: ArtistRepository = mock()
    private val entityManager: EntityManager = mock()

    private val service = ShareService(
        sharedLinkRepository, spotifyUserService, spotifyService, artistRepository, entityManager
    )

    private val testUser = SpotifyUserEntity("spotify-123", "TestUser", "token", 10)

    private fun safeArtist(id: String, name: String, genres: List<String> = emptyList()) = SafeArtistObject(
        id = id, name = name, genres = genres, href = "https://api.spotify.com/v1/artists/$id",
        images = mutableListOf(), externalUrls = ExternalUrlObject().apply { spotify = "https://open.spotify.com/artist/$id" },
        followers = FollowersObject().apply { total = 100 }, popularity = 50
    )

    // --- createOrUpdateShareLink ---

    @Test
    fun `createOrUpdateShareLink creates new link when none exists`() {
        val artists = listOf(safeArtist("a1", "Artist1"))
        `when`(spotifyService.fetchFollowings()).thenReturn(artists)
        `when`(sharedLinkRepository.findByUserId("spotify-123")).thenReturn(null)
        `when`(spotifyUserService.findBySpotifyIdOrThrow("spotify-123")).thenReturn(testUser)
        `when`(sharedLinkRepository.findByShareCodeWithArtists(anyString())).thenReturn(null)
        `when`(sharedLinkRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = service.createOrUpdateShareLink("spotify-123")

        assertThat(result.shareCode).hasSize(8)
        assertThat(result.expiresAt).isAfter(OffsetDateTime.now().plusDays(29))
        verify(sharedLinkRepository).save(any())
    }

    @Test
    fun `createOrUpdateShareLink updates existing link`() {
        val artists = listOf(safeArtist("a1", "Artist1"))
        `when`(spotifyService.fetchFollowings()).thenReturn(artists)

        val existingLink = SharedLinkEntity(
            user = testUser,
            shareCode = "oldCode1",
            expiresAt = OffsetDateTime.now().plusDays(5)
        )
        `when`(sharedLinkRepository.findByUserId("spotify-123")).thenReturn(existingLink)
        `when`(sharedLinkRepository.save(any())).thenAnswer { it.arguments[0] }

        val result = service.createOrUpdateShareLink("spotify-123")

        assertThat(result.shareCode).isEqualTo("oldCode1")
        assertThat(result.expiresAt).isAfter(OffsetDateTime.now().plusDays(29))
        verify(entityManager).flush()
    }

    // --- getShareLinkStatus ---

    @Test
    fun `getShareLinkStatus returns null when no link exists`() {
        `when`(sharedLinkRepository.findByUserId("spotify-123")).thenReturn(null)

        val result = service.getShareLinkStatus("spotify-123")

        assertThat(result).isNull()
    }

    @Test
    fun `getShareLinkStatus returns null when link is expired`() {
        val expiredLink = SharedLinkEntity(
            user = testUser,
            shareCode = "expired1",
            expiresAt = OffsetDateTime.now().minusDays(1)
        )
        `when`(sharedLinkRepository.findByUserId("spotify-123")).thenReturn(expiredLink)

        val result = service.getShareLinkStatus("spotify-123")

        assertThat(result).isNull()
    }

    @Test
    fun `getShareLinkStatus returns response when link is valid`() {
        val validLink = SharedLinkEntity(
            user = testUser,
            shareCode = "validCd1",
            expiresAt = OffsetDateTime.now().plusDays(10)
        )
        `when`(sharedLinkRepository.findByUserId("spotify-123")).thenReturn(validLink)

        val result = service.getShareLinkStatus("spotify-123")

        assertThat(result).isNotNull
        assertThat(result!!.shareCode).isEqualTo("validCd1")
    }

    // --- getSharedArtists ---

    @Test
    fun `getSharedArtists throws ShareLinkNotFoundException for unknown code`() {
        `when`(sharedLinkRepository.findByShareCodeWithArtists("unknown")).thenReturn(null)

        assertThatThrownBy { service.getSharedArtists("unknown") }
            .isInstanceOf(ShareLinkNotFoundException::class.java)
    }

    @Test
    fun `getSharedArtists throws ShareLinkExpiredException for expired link`() {
        val expiredLink = SharedLinkEntity(
            user = testUser,
            shareCode = "expired1",
            expiresAt = OffsetDateTime.now().minusDays(1)
        )
        `when`(sharedLinkRepository.findByShareCodeWithArtists("expired1")).thenReturn(expiredLink)

        assertThatThrownBy { service.getSharedArtists("expired1") }
            .isInstanceOf(ShareLinkExpiredException::class.java)
    }

    @Test
    fun `getSharedArtists returns empty list when link has no artists`() {
        val link = SharedLinkEntity(
            user = testUser,
            shareCode = "emptyArt",
            expiresAt = OffsetDateTime.now().plusDays(10)
        )
        `when`(sharedLinkRepository.findByShareCodeWithArtists("emptyArt")).thenReturn(link)

        val userProfile = PublicUserObject()
        userProfile.displayName = "TestUser"
        userProfile.images = mutableListOf()
        `when`(spotifyService.fetchUserPublicProfile("spotify-123")).thenReturn(userProfile)

        val result = service.getSharedArtists("emptyArt")

        assertThat(result.artists).isEmpty()
        assertThat(result.owner.displayName).isEqualTo("TestUser")
    }

    @Test
    fun `getSharedArtists returns artists with enriched genres from DB`() {
        val link = SharedLinkEntity(
            user = testUser,
            shareCode = "withArt1",
            expiresAt = OffsetDateTime.now().plusDays(10)
        )
        val sharedArtistEntity = SharedLinkArtistEntity(sharedLink = link, artistId = "a1")
        link.artists.add(sharedArtistEntity)
        `when`(sharedLinkRepository.findByShareCodeWithArtists("withArt1")).thenReturn(link)

        val userProfile = PublicUserObject()
        userProfile.displayName = "TestUser"
        userProfile.images = mutableListOf()
        `when`(spotifyService.fetchUserPublicProfile("spotify-123")).thenReturn(userProfile)

        val artist = safeArtist("a1", "Artist1")
        `when`(spotifyService.fetchArtistsByIds(listOf("a1"))).thenReturn(listOf(artist))

        val artistEntity = ArtistEntity("a1", "Artist1")
        val genreEntity = ArtistGenreEntity(artistEntity, "Rock")
        artistEntity.genres.add(genreEntity)
        `when`(artistRepository.findByUserIdAndArtistIdsWithGenres("spotify-123", listOf("a1")))
            .thenReturn(listOf(artistEntity))

        val result = service.getSharedArtists("withArt1")

        assertThat(result.artists).hasSize(1)
        assertThat(result.artists[0].enrichedGenres).containsExactly("Rock")
    }

    // --- deleteShareLink ---

    @Test
    fun `deleteShareLink delegates to repository`() {
        service.deleteShareLink("spotify-123")

        verify(sharedLinkRepository).deleteByUserSpotifyId("spotify-123")
    }
}
