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
        const params = new URLSearchParams(hash.substring(1)); //#
        const accessToken = params.get('access_token');

        if (accessToken) {
            processed.current = true;

            loginWithMicrosoft({ token: accessToken })
                .then(() => {
                    router.push('/dashboard');
                })
                .catch((err) => {
                    console.error("MS Login Error:", err);
                    router.push('/login?error=microsoft_failed');
                });
        } else {
            const error = params.get('error');
            if (error) {
                console.error("Azure error:", error);
                router.push('/login?error=microsoft_denied');
            }
        }
    }, [loginWithMicrosoft, router]);

    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-background text-foreground">
            <Loader2 className="h-10 w-10 animate-spin text-primary mb-4" />
            <p className="text-txtcolor-300">Logowanie przez Microsoft...</p>
        </div>
    );
}