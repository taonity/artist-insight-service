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
  env: {
    BUILD_TIME: new Date().toISOString(),
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
