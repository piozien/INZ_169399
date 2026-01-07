import type { NextConfig } from 'next';

const nextConfig: NextConfig = {
    output: "standalone",

    async rewrites() {
        const isDev = process.env.NODE_ENV !== 'production';
        return [
            {
                source: '/api/:path*',
                destination: isDev 
                    ? 'http://backend:8080/api/:path*' 
                    : 'https://subackend-89f03bcfe431.herokuapp.com/api/:path*',
            },
        ];
    },
};

export default nextConfig;
