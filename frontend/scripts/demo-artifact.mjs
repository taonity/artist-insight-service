import { existsSync, readFileSync } from 'node:fs'
import path from 'node:path'

export function getDemoArchive(backendTarget) {
  // Reproducible JAR timestamps cannot identify the version that was just built.
  const metadata = path.join(backendTarget, 'maven-archiver', 'pom.properties')
  const buildFirst = 'Build the demo backend with npm run demo:build first'
  if (!existsSync(metadata)) throw new Error(buildFirst)
  const version = readFileSync(metadata, 'utf8').match(/^version=(.+)$/m)?.[1].trim()
  if (!version || /[/\\]/.test(version)) throw new Error('Invalid demo artifact version')
  const archive = path.join(backendTarget, `artist-insight-service-${version}.jar`)
  if (!existsSync(archive)) throw new Error(buildFirst)
  return archive
}
