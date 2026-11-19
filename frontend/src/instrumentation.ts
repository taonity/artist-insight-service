export async function register() {
  if (process.env.NEXT_RUNTIME === 'nodejs') {
    const { getServerEnv } = await import('@/lib/env')
    if (process.env.NEXT_PHASE !== 'phase-production-build') getServerEnv()
    console.log(
      `[artist-insight-frontend] commit=${process.env.GIT_COMMIT_SHA} built=${process.env.BUILD_TIME}`,
    )
  }
}
