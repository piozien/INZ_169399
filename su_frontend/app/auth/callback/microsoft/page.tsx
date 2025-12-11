'use client';

import { useEffect, useRef } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/contexts/AuthContext';
import { Loader2 } from 'lucide-react';

export default function MicrosoftCallbackPage() {
    const router = useRouter();
    const { loginWithMicrosoft } = useAuth();
    const processed = useRef(false);

    useEffect(() => {
        if (processed.current) return;

        const hash = window.location.hash;
        const params = new URLSearchParams(hash.substring(1));
        const accessToken = params.get('access_token');

        if (accessToken) {
            processed.current = true;

            loginWithMicrosoft({ token: accessToken })
                .then(() => {
                    router.push('/dashboard');
                })
                .catch((err) => {
                    console.error('MS Login Error:', err);
                    router.push('/login?error=microsoft_failed');
                });
        } else {
            const error = params.get('error');
            if (error) {
                console.error('Azure error:', error);
                router.push('/login?error=microsoft_denied');
            }
        }
    }, [loginWithMicrosoft, router]);

    return (
        <div className="bg-background text-foreground flex min-h-screen flex-col items-center justify-center">
            <Loader2 className="text-primary mb-4 h-10 w-10 animate-spin" />
            <p className="text-txtcolor-300">Logowanie przez Microsoft...</p>
        </div>
    );
}
