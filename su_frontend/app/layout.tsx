import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';
import Providers from './providers';
import { ThemeProvider } from '@/lib/contexts/ThemeContext';
import ThemeScript from '@/components/ThemeScript';
import ConditionalSidebar from '@/components/sidebar/ConditionalSidebar';
import CookieConsent from '@/components/CookieConsent';

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
    title: 'Samorząd Uczniowski',
    description: 'Portal Samorządu Uczniowskiego',
};

export default function RootLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <html lang="pl" suppressHydrationWarning>
            <head>
                <ThemeScript />
            </head>
            <body className={`${inter.className} bg-background text-foreground flex min-h-screen`}>
                <Providers>
                    <ThemeProvider>
                        <ConditionalSidebar />
                        <main className="w-full flex-1">{children}</main>
                        <CookieConsent />
                    </ThemeProvider>
                </Providers>
            </body>
        </html>
    );
}
