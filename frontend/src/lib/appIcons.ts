import { getServerEnv } from './env'

export function getAppIcons() {
  const stage = getServerEnv().profile === 'stage'
  return {
    favicon: stage ? '/favicon-stage.ico' : '/favicon.ico',
    appleTouchIcon: stage ? '/icons/icon-stage-180.png' : '/icons/apple-touch-icon.png',
    manifestIconPrefix: stage ? '/icons/icon-stage' : '/icons/icon',
  }
}
