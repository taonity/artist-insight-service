const { execSync } = require('child_process')

function git(command) {
  try {
    return execSync(`git ${command}`, { encoding: 'utf8' }).trim()
  } catch {
    return 'unknown'
  }
}

/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  images: {
    remotePatterns: [
      {
        protocol: 'https',
        hostname: 'i.scdn.co'
      }
    ]
  },
  output: 'standalone',
  experimental: {
    instrumentationHook: true,
  },
  env: {
    BUILD_TIME: new Date().toISOString(),
    GIT_COMMIT_SHA: git('rev-parse HEAD'),
    GIT_COMMIT_SHORT: git('rev-parse --short HEAD'),
    GIT_BRANCH: git('rev-parse --abbrev-ref HEAD'),
  },
  webpack: (config) => {
    config.ignoreWarnings = [
      ...(config.ignoreWarnings || []),
      { module: /node_modules[\\/]ag-grid-community/ }
    ];
    return config;
  }
}
module.exports = nextConfig
