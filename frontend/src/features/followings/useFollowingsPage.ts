'use client'

import { useEffect, useRef, useState } from 'react'
import {
  buildFollowingsCsvData,
  checkExistingShareLink,
  createShareLink,
  fetchFollowings,
} from '@/features/followings/api'
import { useUser } from '@/hooks/useUser'
import { DEFAULT_NETWORK_ERROR_MESSAGE } from '@/lib/clientApi'
import { getCookie } from '@/lib/cookies'
import { getRuntimeConfig } from '@/lib/runtimeConfig'
import type { Advisory } from '@/types/advisory'
import type { EnrichableArtistObject } from '@/types/followings'

export function useFollowingsPage() {
  const [enrichableArtistObjects, setArtists] = useState<EnrichableArtistObject[]>([])
  const [advisories, setAdvisories] = useState<Advisory[]>([])
  const [loading, setLoading] = useState(true)
  const [enriching, setEnriching] = useState(false)
  const [gptUsagesLeft, setGptUsagesLeft] = useState(0)
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [shareLoading, setShareLoading] = useState(false)
  const [shareCopiedNotification, setShareCopiedNotification] = useState(false)
  const [hasExistingShareLink, setHasExistingShareLink] = useState(false)
  const [csrfCookieName, setCsrfCookieName] = useState('XSRF-TOKEN')
  const shareNotificationTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  const user = useUser({ onError: setErrorMessage })

  useEffect(() => {
    void getRuntimeConfig().then((config) => setCsrfCookieName(config.csrfCookieName))
  }, [])

  useEffect(() => {
    if (!user) {
      return
    }

    setGptUsagesLeft(user.gptUsagesLeft)
    void loadUserFollowings()
    void loadExistingShareLinkState()
  }, [user])

  useEffect(() => {
    return () => {
      if (shareNotificationTimeoutRef.current) {
        clearTimeout(shareNotificationTimeoutRef.current)
      }
    }
  }, [])

  const loadUserFollowings = async () => {
    setLoading(true)

    try {
      const result = await fetchFollowings('/api/followings', 30000)

      if (!result.ok) {
        if (result.advisories) {
          setAdvisories(result.advisories)
        } else if (result.errorMessage) {
          setErrorMessage(result.errorMessage)
        }
        return
      }

      setArtists(result.data.artists)
      setAdvisories(result.data.advisories)
    } catch {
      setErrorMessage(DEFAULT_NETWORK_ERROR_MESSAGE)
    } finally {
      setLoading(false)
    }
  }

  const loadEnrichedFollowings = async () => {
    setEnriching(true)

    try {
      const result = await fetchFollowings('/api/followings/enriched', 60000)

      if (!result.ok) {
        if (result.advisories) {
          setAdvisories(result.advisories)
        } else if (result.errorMessage) {
          setErrorMessage(result.errorMessage)
        }
        return
      }

      setArtists(result.data.artists)
      setAdvisories(result.data.advisories)
      setGptUsagesLeft(result.data.gptUsagesLeft ?? 0)
    } catch {
      setErrorMessage(DEFAULT_NETWORK_ERROR_MESSAGE)
    } finally {
      setEnriching(false)
    }
  }

  const loadExistingShareLinkState = async () => {
    setHasExistingShareLink(await checkExistingShareLink())
  }

  const handleShare = async () => {
    const xsrfToken = getCookie(csrfCookieName)
    if (!xsrfToken) {
      setErrorMessage('Unable to verify your request. Please refresh the page and try again.')
      return
    }

    setShareLoading(true)
    const result = await createShareLink(xsrfToken)

    try {
      if (result.ok) {
        const fullUrl = `${window.location.origin}/share/${result.data.shareCode}`
        setHasExistingShareLink(true)

        try {
          await navigator.clipboard.writeText(fullUrl)
          setShareCopiedNotification(true)
          if (shareNotificationTimeoutRef.current) {
            clearTimeout(shareNotificationTimeoutRef.current)
          }
          shareNotificationTimeoutRef.current = setTimeout(() => {
            setShareCopiedNotification(false)
            shareNotificationTimeoutRef.current = null
          }, 8000)
        } catch {
          setErrorMessage('Failed to copy link to clipboard.')
        }
      } else {
        setErrorMessage(result.message)
      }
    } finally {
      setShareLoading(false)
    }
  }

  return {
    user,
    enrichableArtistObjects,
    advisories,
    loading,
    enriching,
    gptUsagesLeft,
    errorMessage,
    shareLoading,
    shareCopiedNotification,
    hasExistingShareLink,
    csvData: buildFollowingsCsvData(enrichableArtistObjects),
    clearErrorMessage: () => setErrorMessage(null),
    loadEnrichedFollowings,
    handleShare,
  }
}