'use client';

import { useState, FormEvent, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';

import SchoolRounded from '@/components/icons/SchoolRounded';
import MicrosoftIcon from '@/components/icons/MicrosoftIcon';
import FormField from '@/components/FormField';

import { useAuth } from '@/lib/contexts/AuthContext';
import { ApiError } from '@/types/error.types';
import { getMicrosoftAuthUrl } from '@/lib/api/auth';

function LoginForm() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const { login } = useAuth();

    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const [isPending, setIsPending] = useState(false);

    const registered = searchParams.get('registered') === 'true';
    const successMessage = registered
        ? 'Rejestracja zakończona pomyślnie! Na podany adres email wysłano link aktywacyjny.'
        : null;

    const handleEmailLogin = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        setIsPending(true);

        try {
            await login({ email, password });
            router.push('/dashboard');
        } catch (err) {
            if (err instanceof ApiError || err instanceof Error) {
                setError(err.message);
            } else {
                setError('Wystąpił nieznany błąd podczas logowania');
            }
        } finally {
            setIsPending(false);
        }
    };

    const handleMicrosoftLogin = () => {
        window.location.href = getMicrosoftAuthUrl();
    };

    return (
        <main className="flex min-h-screen items-center justify-center p-3">
            <div className="bg-secondarybg flex w-full max-w-[404px] flex-col items-center justify-center rounded-[11.83px] px-3 py-10 text-center">
                <div className="flex flex-col items-center">
                    <SchoolRounded />
                    <h1 className="mt-4 text-[23.42px] font-semibold">Zaloguj się</h1>
                    <p className="text-txtcolor-300 mt-3 max-w-[350px] text-sm">
                        Użyj swojego adresu email i hasła, aby zalogować się do portalu samorządu.
                    </p>
                </div>

                <form onSubmit={handleEmailLogin} className="mt-8 flex w-10/12 flex-col gap-5">
                    <FormField
                        id="email"
                        label="ADRES E-MAIL:"
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="jan@kowalski.pl"
                        disabled={isPending}
                    />

                    <FormField
                        id="password"
                        label="Hasło:"
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="Twoje hasło"
                        disabled={isPending}
                    />

                    {error && <p className="text-error text-sm">{error}</p>}
                    {successMessage && <p className="text-success text-sm">{successMessage}</p>}

                    <button
                        type="submit"
                        disabled={isPending}
                        className="bg-primary text-darkgray hover:bg-secondary mt-4 flex max-h-[38px] w-full cursor-pointer items-center justify-center rounded-[53px] px-3 py-4 font-semibold transition-colors disabled:opacity-50"
                    >
                        {isPending ? 'Logowanie...' : 'Zaloguj się'}
                    </button>

                    <div className="text-center text-sm">
                        <Link
                            href="/forgot-password"
                            className="text-secondary font-medium hover:underline"
                        >
                            Zapomniałeś hasła?
                        </Link>
                    </div>
                </form>

                <div className="mt-8 flex w-10/12 flex-col items-center gap-5">
                    <div className="flex w-full items-center gap-4">
                        <div className="border-txtcolor-300 flex-1 border-b" />
                        <p className="text-text-muted text-sm">lub</p>
                        <div className="border-txtcolor-300 flex-1 border-b" />
                    </div>

                    <button
                        type="button"
                        onClick={handleMicrosoftLogin}
                        disabled={isPending}
                        className="bg-microsoftbg text-darkgray mt-4 flex max-h-[38px] w-full cursor-pointer items-center justify-center gap-3 rounded-[53px] px-3 py-4 font-semibold transition-opacity hover:opacity-90"
                    >
                        <MicrosoftIcon />
                        <span>Zaloguj się przez Microsoft</span>
                    </button>
                </div>

                <div className="mt-auto pt-4 text-sm">
                    <p>
                        Nie masz konta?{' '}
                        <Link
                            href="/register"
                            className="text-secondary font-medium hover:underline"
                        >
                            Zarejestruj się
                        </Link>
                    </p>
                    <p className="mt-2">
                        <Link href="/" className="text-secondary font-medium hover:underline">
                            Wróć do strony głównej
                        </Link>
                    </p>
                </div>
            </div>
        </main>
    );
}

export default function LoginPage() {
    return (
        <Suspense
            fallback={
                <div className="flex min-h-screen items-center justify-center">Ładowanie...</div>
            }
        >
            <LoginForm />
        </Suspense>
    );
}
