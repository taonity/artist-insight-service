import { mkdtempSync, mkdirSync, rmSync, utimesSync, writeFileSync } from 'node:fs'
import { tmpdir } from 'node:os'
import path from 'node:path'
import { afterEach, expect, it } from 'vitest'
import { getDemoArchive } from '../../scripts/demo-artifact.mjs'

const directories: string[] = []

function target() {
  const directory = mkdtempSync(path.join(tmpdir(), 'artist-insight-artifact-'))
  directories.push(directory)
  mkdirSync(path.join(directory, 'maven-archiver'))
  return directory
}

afterEach(() => {
  for (const directory of directories.splice(0)) {
    const resolved = path.resolve(directory)
    if (path.dirname(resolved) !== path.resolve(tmpdir()) || !path.basename(resolved).startsWith('artist-insight-artifact-')) {
      throw new Error('Test cleanup must stay inside its temporary directory')
    }
    rmSync(resolved, { recursive: true })
  }
})

it('selects the built version even when reproducible timestamps make an old JAR look newer', () => {
  const directory = target()
  writeFileSync(path.join(directory, 'maven-archiver', 'pom.properties'), 'version=main-SNAPSHOT\r\n')
  const current = path.join(directory, 'artist-insight-service-main-SNAPSHOT.jar')
  const stale = path.join(directory, 'artist-insight-service-0.55.0.jar')
  writeFileSync(current, '')
  writeFileSync(stale, '')
  utimesSync(current, new Date('1980-02-01'), new Date('1980-02-01'))
  utimesSync(stale, new Date(), new Date())
  expect(getDemoArchive(directory)).toBe(current)
})

it('requires the artifact identified by Maven instead of silently launching another version', () => {
  const directory = target()
  writeFileSync(path.join(directory, 'maven-archiver', 'pom.properties'), 'version=main-SNAPSHOT\n')
  writeFileSync(path.join(directory, 'artist-insight-service-0.55.0.jar'), '')
  expect(() => getDemoArchive(directory)).toThrow('Build the demo backend')
})
