'use client';

import { useEffect, useState, Suspense } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
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
        <div className="w-full text-center max-w-[404px] rounded-[12px] px-6 py-10 flex flex-col justify-center items-center bg-secondarybg border border-border shadow-xl">
            <div className="mb-8 flex flex-col items-center gap-2">
                <div className="scale-125 text-primary">
                    <SchoolRounded />
                </div>
                <span className="text-xl font-bold tracking-widest text-foreground uppercase mt-2">
          Samorząd Uczniowski
        </span>
            </div>

            {status === 'loading' && (
                <div className="flex flex-col items-center gap-4 animate-in fade-in">
                    <Loader2 className="w-10 h-10 text-primary animate-spin" />
                    <h1 className="text-xl font-semibold text-foreground">Weryfikacja...</h1>
                    <p className="text-sm text-txtcolor-300">Trwa aktywacja Twojego konta.</p>
                </div>
            )}

            {status === 'success' && (
                <div className="flex flex-col items-center gap-4 animate-in zoom-in-95 duration-300">
                    <CheckCircle className="w-12 h-12 text-success" />
                    <h1 className="text-2xl font-bold text-foreground">Konto aktywne!</h1>
                    <p className="text-sm text-txtcolor-300 max-w-[300px]">
                        Twój adres e-mail został potwierdzony. Możesz teraz zalogować się do systemu.
                    </p>
                    <Link
                        href="/login"
                        className="mt-4 w-full py-3 px-4 rounded-full bg-primary text-darkgray font-bold hover:bg-secondary transition-all shadow-lg hover:shadow-primary/20"
                    >
                        Przejdź do logowania
                    </Link>
                </div>
            )}

            {status === 'error' && (
                <div className="flex flex-col items-center gap-4 animate-in shake duration-300">
                    <XCircle className="w-12 h-12 text-error" />
                    <h1 className="text-xl font-bold text-foreground">Błąd aktywacji</h1>
                    <div className="flex flex-col gap-3 w-full mt-4">
                        <Link
                            href="/login"
                            className="w-full py-3 px-4 rounded-full bg-inputbg text-foreground border border-border hover:border-primary transition-all font-medium text-sm"
                        >
                            Wróć do logowania
                        </Link>
                        <Link href="/" className="text-xl text-foregorund hover:text-primary transition-colors">
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
        <main className="min-h-screen flex items-center justify-center p-4 bg-background">
            <Suspense fallback={<div className="text-primary">Ładowanie...</div>}>
                <ActivateContent />
            </Suspense>
        </main>
    );
}