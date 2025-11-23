import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import './globals.css';
import QueryClientWrapper from '@/components/QueryClientProvider';
import { ThemeProvider } from '@/lib/contexts/ThemeContext';
import ThemeScript from '@/components/ThemeScript';
import ConditionalSidebar from '@/components/ConditionalSidebar';

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
      <body
        className={`${inter.className} flex bg-background text-foreground`}
      >
        <QueryClientWrapper>
          <ThemeProvider>
            <ConditionalSidebar />
            <main className="flex-1">{children}</main>
          </ThemeProvider>
        </QueryClientWrapper>
      </body>
    </html>
  );
}
