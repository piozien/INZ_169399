'use client';

import { useEffect, useState, Suspense } from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { activateAccount } from '@/lib/api/auth';
import SchoolRounded from '@/components/icons/SchoolRounded';
import { Loader2, CheckCircle, XCircle } from 'lucide-react';

function ActivateContent() {
    const searchParams = useSearchParams();
    const token = searchParams.get('token');
    const [status, setStatus] = useState<'loading' | 'success' | 'error'>('loading');
    const [message, setMessage] = useState('');

    useEffect(() => {
        if (!token) {
            setStatus('error');
            setMessage('Brak tokenu aktywacyjnego w linku.');
            return;
        }

        activateAccount(token)
            .then(() => {
                setStatus('success');
            })
            .catch((err) => {
                setStatus('error');
                setMessage(err.message || 'Wystąpił błąd podczas aktywacji.');
            });
    }, [token]);

    return (
        <div className="bg-secondarybg border-border flex w-full max-w-[404px] flex-col items-center justify-center rounded-[12px] border px-6 py-10 text-center shadow-xl">
            <div className="mb-8 flex flex-col items-center gap-2">
                <div className="text-primary scale-125">
                    <SchoolRounded />
                </div>
                <span className="text-foreground mt-2 text-xl font-bold tracking-widest uppercase">
                    Samorząd Uczniowski
                </span>
            </div>

            {status === 'loading' && (
                <div className="animate-in fade-in flex flex-col items-center gap-4">
                    <Loader2 className="text-primary h-10 w-10 animate-spin" />
                    <h1 className="text-foreground text-xl font-semibold">Weryfikacja...</h1>
                    <p className="text-txtcolor-300 text-sm">Trwa aktywacja Twojego konta.</p>
                </div>
            )}

            {status === 'success' && (
                <div className="animate-in zoom-in-95 flex flex-col items-center gap-4 duration-300">
                    <CheckCircle className="text-success h-12 w-12" />
                    <h1 className="text-foreground text-2xl font-bold">Konto aktywne!</h1>
                    <p className="text-txtcolor-300 max-w-[300px] text-sm">
                        Twój adres e-mail został potwierdzony. Możesz teraz zalogować się do
                        systemu.
                    </p>
                    <Link
                        href="/login"
                        className="bg-primary text-darkgray hover:bg-secondary hover:shadow-primary/20 mt-4 w-full rounded-full px-4 py-3 font-bold shadow-lg transition-all"
                    >
                        Przejdź do logowania
                    </Link>
                </div>
            )}

            {status === 'error' && (
                <div className="animate-in shake flex flex-col items-center gap-4 duration-300">
                    <XCircle className="text-error h-12 w-12" />
                    <h1 className="text-foreground text-xl font-bold">Błąd aktywacji</h1>
                    <div className="mt-4 flex w-full flex-col gap-3">
                        <Link
                            href="/login"
                            className="bg-inputbg text-foreground border-border hover:border-primary w-full rounded-full border px-4 py-3 text-sm font-medium transition-all"
                        >
                            Wróć do logowania
                        </Link>
                        <Link
                            href="/"
                            className="text-foregorund hover:text-primary text-xl transition-colors"
                        >
                            Strona główna
                        </Link>
                    </div>
                </div>
            )}
        </div>
    );
}

export default function ActivatePage() {
    return (
        <main className="bg-background flex min-h-screen items-center justify-center p-4">
            <Suspense fallback={<div className="text-primary">Ładowanie...</div>}>
                <ActivateContent />
            </Suspense>
        </main>
    );
}
