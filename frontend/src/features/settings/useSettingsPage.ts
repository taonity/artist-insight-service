'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import {
  deleteAccount,
  deleteShareLink,
  fetchShareLinkStatus,
  requestLogout,
} from '@/features/settings/api'
import { useUser } from '@/hooks/useUser'
import { DEFAULT_NETWORK_ERROR_MESSAGE } from '@/lib/clientApi'
import { getCookie } from '@/lib/cookies'
import { logError } from '@/lib/logger'
import { getRuntimeConfig } from '@/lib/runtimeConfig'
import type { ShareLink } from '@/types/share'

const csrfErrorMessage = 'Unable to verify your request. Please refresh the page and try again.'

export function useSettingsPage() {
  const router = useRouter()
  const [errorMessage, setErrorMessage] = useState<string | null>(null)
  const [isProcessing, setIsProcessing] = useState(false)
  const [csrfCookieName, setCsrfCookieName] = useState('XSRF-TOKEN')
  const [shareLink, setShareLink] = useState<ShareLink | null>(null)
  const [shareLinkLoading, setShareLinkLoading] = useState(true)
  const user = useUser({ onError: setErrorMessage })

  useEffect(() => {
    void getRuntimeConfig().then((config) => setCsrfCookieName(config.csrfCookieName))
    void loadShareLinkStatus()
  }, [])

  const handleLogout = async () => {
    const xsrfToken = getCookie(csrfCookieName)
    if (!xsrfToken) {
      logError('SettingsPage', `CSRF token not found for logout with CSRF_COOKIE_NAME=${csrfCookieName}`)
      setErrorMessage(csrfErrorMessage)
      return
    }

    setIsProcessing(true)
    try {
      await requestLogout(xsrfToken)
      router.replace('/login')
    } catch (error) {
      logError('SettingsPage', 'Logout failed', error)
      setErrorMessage('Unable to log out. Please try again.')
    } finally {
      setIsProcessing(false)
    }
  }

  const handleDeleteAccount = async () => {
    if (!window.confirm('Are you sure you want to delete your account? This action cannot be undone.')) {
      return
    }

    const xsrfToken = getCookie(csrfCookieName)
    if (!xsrfToken) {
      logError('SettingsPage', `CSRF token not found for delete account with CSRF_COOKIE_NAME=${csrfCookieName}`)
      setErrorMessage(csrfErrorMessage)
      return
    }

    setIsProcessing(true)
    try {
      const response = await deleteAccount(xsrfToken)

      if (response.status === 401) {
        router.replace('/login')
        return
      }

      if (response.status === 403) {
        logError('SettingsPage', 'Delete account forbidden - CSRF validation failed')
        setErrorMessage(csrfErrorMessage)
        return
      }

      if (!response.ok && response.status !== 204) {
        logError('SettingsPage', `Delete account failed with status: ${response.status}`)
        setErrorMessage('Failed to delete your account. Please try again.')
        return
      }

      try {
        await requestLogout(xsrfToken)
      } catch (error) {
        logError('SettingsPage', 'Logout after delete account failed', error)
      }

      router.replace('/login')
    } catch (error) {
      logError('SettingsPage', 'Delete account network error', error)
      setErrorMessage(DEFAULT_NETWORK_ERROR_MESSAGE)
    } finally {
      setIsProcessing(false)
    }
  }

  const loadShareLinkStatus = async () => {
    try {
      setShareLink(await fetchShareLinkStatus())
    } catch (error) {
      logError('SettingsPage', 'Failed to load share link status', error)
    } finally {
      setShareLinkLoading(false)
    }
  }

  const handleDeleteShareLink = async () => {
    if (
      !window.confirm(
        'Are you sure you want to delete your share link? Anyone with the link will no longer be able to access it.',
      )
    ) {
      return
    }

    const xsrfToken = getCookie(csrfCookieName)
    if (!xsrfToken) {
      logError('SettingsPage', 'CSRF token not found for delete share link')
      setErrorMessage(csrfErrorMessage)
      return
    }

    setIsProcessing(true)
    try {
      const response = await deleteShareLink(xsrfToken)

      if (response.status === 401) {
        router.replace('/login')
        return
      }

      if (response.status === 403) {
        logError('SettingsPage', 'Delete share link forbidden - CSRF validation failed')
        setErrorMessage(csrfErrorMessage)
        return
      }

      if (!response.ok && response.status !== 204) {
        logError('SettingsPage', `Delete share link failed with status: ${response.status}`)
        setErrorMessage('Failed to delete your share link. Please try again.')
        return
      }

      setShareLink(null)
    } catch (error) {
      logError('SettingsPage', 'Delete share link network error', error)
      setErrorMessage(DEFAULT_NETWORK_ERROR_MESSAGE)
    } finally {
      setIsProcessing(false)
    }
  }

  return {
    user,
    errorMessage,
    isProcessing,
    shareLink,
    shareLinkLoading,
    isLoadingUser: !user,
    clearErrorMessage: () => setErrorMessage(null),
    handleLogout,
    handleDeleteAccount,
    handleDeleteShareLink,
  }
}