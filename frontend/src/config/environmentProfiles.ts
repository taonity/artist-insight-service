export const FRONTEND_PROFILES = ['local', 'stage', 'prod'] as const
export type FrontendProfile = (typeof FRONTEND_PROFILES)[number]
export const FRONTEND_PROFILE_CONFIG = {
  local: {
    localBackendUrl: 'http://127.0.0.1:8080',
    publicBackendUrl: 'http://127.0.0.1:8080',
    csrfCookieName: 'XSRF-TOKEN-LOCAL',
    kofiUrl: 'http://127.0.0.1:8080',
  },
  stage: {
    localBackendUrl: 'http://app:8080',
    publicBackendUrl: 'https://artist-insight-api-stage.taonity.org',
    csrfCookieName: 'XSRF-TOKEN-ARTIST-INSIGHT-STAGE',
    kofiUrl: 'https://ko-fi.com',
  },
  prod: {
    localBackendUrl: 'http://app:8080',
    publicBackendUrl: 'https://artist-insight-api.taonity.org',
    csrfCookieName: 'XSRF-TOKEN-ARTIST-INSIGHT-PROD',
    kofiUrl: 'https://ko-fi.com',
  },
} satisfies Record<FrontendProfile, object>
export function isFrontendProfile(value: string): value is FrontendProfile {
  return FRONTEND_PROFILES.some((profile) => profile === value)
}
