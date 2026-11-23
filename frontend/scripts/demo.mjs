import { spawn, spawnSync } from 'node:child_process'
import { cpSync, mkdtempSync, readdirSync, rmSync, statSync } from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const root = fileURLToPath(new URL('../../', import.meta.url))
const frontend = fileURLToPath(new URL('../', import.meta.url))
const backendTarget = path.join(root, 'artist-insight-service', 'target')
const command = process.argv[2]
const env = {
  ...process.env,
  FRONTEND_PROFILE: 'demo',
  LOCAL_BACKEND_URL: 'http://127.0.0.1:8088',
  HOSTNAME: '127.0.0.1',
  PORT: '3008',
}

function run(executable, args, cwd, shell = false) {
  const result = spawnSync(executable, args, { cwd, shell, env, stdio: 'inherit' })
  if (result.error) throw result.error
  if (result.status !== 0)
    throw new Error(`${executable} exited with ${result.status ?? result.signal}`)
}

if (command === 'build') {
  const windows = process.platform === 'win32'
  run(
    windows ? 'mvn.cmd' : 'mvn',
    ['-B', '-pl', 'artist-insight-service', '-am', 'package', '-DskipTests'],
    root,
    windows,
  )
  run(process.execPath, ['node_modules/next/dist/bin/next', 'build'], frontend)
} else if (command === 'backend' || command === 'frontend') {
  const backend = command === 'backend'
  let runtime
  let args
  if (backend) {
    const [archive] = readdirSync(backendTarget)
      .filter((name) => /^artist-insight-service-.+\.jar$/.test(name))
      .map((name) => path.join(backendTarget, name))
      .sort((a, b) => statSync(b).mtimeMs - statSync(a).mtimeMs)
    if (!archive) throw new Error('Build the demo backend with npm run demo:build first')
    runtime = mkdtempSync(path.join(backendTarget, 'screenshot-runtime-'))
    // WireMock resolves its fixtures from regular classpath jars.
    run('jar', ['xf', archive, 'BOOT-INF/classes', 'BOOT-INF/lib'], runtime)
    const classpath = [
      path.join(runtime, 'BOOT-INF/classes'),
      path.join(runtime, 'BOOT-INF/lib/*'),
    ].join(path.delimiter)
    args = [
      '-Duser.timezone=UTC',
      '-cp',
      classpath,
      'org.taonity.artistinsightservice.ArtistInsightApplicationKt',
      '--spring.profiles.active=h2,stub-spotify,stub-openai,stub-kofi,stub-mail,local,demo-data,demo-settings',
      '--server.address=127.0.0.1',
      '--server.port=8088',
      '--spring.datasource.url=jdbc:h2:mem:artist_insight_screenshots;MODE=PostgreSQL;DB_CLOSE_DELAY=-1',
      '--kofi.url=http://127.0.0.1:8088',
      '--logging.level.org.hibernate.SQL=WARN',
    ]
  } else {
    for (const directory of ['.next/static', 'public']) {
      cpSync(path.join(frontend, directory), path.join(frontend, '.next/standalone', directory), {
        recursive: true,
      })
    }
    args = ['.next/standalone/server.js']
  }

  const child = spawn(backend ? 'java' : process.execPath, args, {
    cwd: backend ? root : frontend,
    env,
    stdio: 'inherit',
  })
  for (const signal of ['SIGINT', 'SIGTERM']) process.on(signal, () => child.kill(signal))
  function cleanup() {
    if (runtime && path.dirname(path.resolve(runtime)) === backendTarget) {
      rmSync(runtime, { recursive: true, force: true })
    }
  }
  child.on('error', (error) => {
    cleanup()
    throw error
  })
  child.on('exit', (code) => {
    cleanup()
    process.exit(code ?? 1)
  })
} else {
  throw new Error('Use demo.mjs with build, backend or frontend')
}
