import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
    output: "standalone",

    async rewrites() {
        return [
            {
                source: '/api/:path*',
                destination: 'https://subackend-89f03bcfe431.herokuapp.com/api/:path*',
            },
        ];
    },
};

export default nextConfig;
