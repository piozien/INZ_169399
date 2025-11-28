'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/contexts/AuthContext';

export default function MicrosoftCallbackPage() {
    const router = useRouter();
    const { loginWithMicrosoft } = useAuth();

    useEffect(() => {
        const hash = window.location.hash;
        const params = new URLSearchParams(hash.replace('#', '?'));
        const accessToken = params.get('access_token');

        if (accessToken) {
            loginWithMicrosoft({ token: accessToken })
                .then(() => {
                    router.push('/dashboard');
                })
                .catch((err) => {
                    console.error('Błąd logowania MS:', err);
                    router.push('/login?error=microsoft_failed');
                });
        } else {
            router.push('/login?error=no_token');
        }
    }, [router, loginWithMicrosoft]);

    return (
        <main className="min-h-screen flex items-center justify-center">
            <h1>Logowanie przez Microsoft...</h1>
        </main>
    );
}
