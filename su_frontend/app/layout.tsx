import type {Metadata} from 'next';
import {Inter} from 'next/font/google';
import './globals.css';
import Providers from './providers';
import {ThemeProvider} from '@/lib/contexts/ThemeContext';
import {Toaster} from "sonner";
import ThemeScript from '@/components/ThemeScript';
import ConditionalSidebar from '@/components/sidebar/ConditionalSidebar';
import CookieConsent from '@/components/CookieConsent';

const inter = Inter({subsets: ['latin']});

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
            <ThemeScript/>
        </head>
        <body className={`${inter.className} bg-background text-foreground flex min-h-screen`}>
        <Providers>
            <ThemeProvider>
                <ConditionalSidebar/>
                <main className="w-full flex-1">{children}</main>
                <CookieConsent/>
            </ThemeProvider>
        </Providers>
        <Toaster
            position="top-center"
            toastOptions={{
                unstyled: true,
                classNames: {
                    toast: 'my-toast',
                    title: 'my-toast-title',
                    description: 'my-toast-desc',
                    actionButton: 'my-toast-btn',
                    cancelButton: 'my-toast-cancel',
                    icon: 'my-toast-icon',
                },
            }}
        />
        </body>
        </html>
    );
}
