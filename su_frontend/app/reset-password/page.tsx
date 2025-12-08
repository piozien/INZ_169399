'use client';

import { useState, FormEvent, useEffect, Suspense } from 'react';
import { useMutation } from '@tanstack/react-query';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';

import SchoolRounded from '@/components/icons/SchoolRounded';
import FormField from '@/components/FormField';
import { validatePasswordResetToken, confirmPasswordReset } from '@/lib/api/auth';

function ResetPasswordForm() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const token = searchParams.get('token');

    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const [error, setError] = useState<string | null>(null);
    const [passwordError, setPasswordError] = useState<string | null>(null);
    const [success, setSuccess] = useState(false);

    const [isValidating, setIsValidating] = useState(true);
    const [isTokenValid, setIsTokenValid] = useState(false);

    const [countdown, setCountdown] = useState(5);

    useEffect(() => {
        if (!token) {
            setError('Brak tokenu resetowania w adresie URL.');
            setIsValidating(false);
            return;
        }

        const validateToken = async () => {
            try {
                await validatePasswordResetToken(token);
                setIsTokenValid(true);
            } catch (e) {
                setError(
                    'Token jest nieprawidłowy lub wygasł. Zostaniesz przekierowany, aby wygenerować nowy.',
                );
                setIsTokenValid(false);
            } finally {
                setIsValidating(false);
            }
        };

        validateToken();
    }, [token]);

    useEffect(() => {
        if (password && confirmPassword && password !== confirmPassword) {
            setPasswordError('Hasła nie są identyczne!');
        } else {
            setPasswordError(null);
        }
    }, [password, confirmPassword]);

    useEffect(() => {
        const shouldRedirect = success || (!isValidating && !isTokenValid && error);

        if (shouldRedirect) {
            const timer = setInterval(() => {
                setCountdown((prevCountdown) => prevCountdown - 1);
            }, 1000);

            if (countdown === 0) {
                clearInterval(timer);
                if (success) {
                    router.push('/login');
                } else {
                    router.push('/forgot-password');
                }
            }

            return () => clearInterval(timer);
        }
    }, [success, isValidating, isTokenValid, error, countdown, router]);

    const mutation = useMutation({
        mutationFn: confirmPasswordReset,
        onSuccess: () => {
            setSuccess(true);
            setError(null);
            setCountdown(5);
        },
        onError: (err) => {
            setError(err instanceof Error ? err.message : 'Wystąpił nieznany błąd.');
        },
    });

    const handleResetPassword = (e: FormEvent) => {
        e.preventDefault();
        if (!token) return;
        setError(null);
        mutation.mutate({ token, newPassword: password });
    };

    const renderContent = () => {
        if (isValidating) {
            return <p className="text-txtcolor-300 animate-pulse">Weryfikowanie tokenu bezpieczeństwa...</p>;
        }
        if (!isTokenValid) {
            return (
                <div className="w-10/12 mt-8 text-center animate-in fade-in">
                    <p className="text-error font-medium mb-4">{error}</p>
                    <div className="text-sm text-txtcolor-300">
                        Przekierowanie do strony odzyskiwania hasła za {countdown}s...
                    </div>
                    <Link
                        href="/forgot-password"
                        className="mt-6 inline-block bg-secondarybg border border-secondary text-secondary px-6 py-2 rounded-full text-sm font-bold hover:bg-secondary hover:text-white transition-all"
                    >
                        Przejdź teraz
                    </Link>
                </div>
            );
        }

        if (success) {
            return (
                <div className="w-10/12 mt-8 text-center animate-in fade-in">
                    <p className="text-success font-bold text-lg mb-2">
                        Hasło zostało pomyślnie zmienione!
                    </p>
                    <p className="mt-2 text-sm text-txtcolor-300">
                        Zostaniesz przekierowany na stronę logowania za {countdown}s...
                    </p>
                    <Link
                        href="/login"
                        className="mt-6 inline-block bg-primary text-darkgray px-6 py-2 rounded-full text-sm font-bold hover:opacity-90 transition-all"
                    >
                        Zaloguj się teraz
                    </Link>
                </div>
            );
        }
        return (
            <form
                onSubmit={handleResetPassword}
                className="w-10/12 mt-8 flex flex-col gap-5 animate-in slide-in-from-bottom-2"
            >
                <FormField
                    id="password"
                    label="NOWE HASŁO:"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Wprowadź nowe hasło"
                    disabled={mutation.isPending}
                />
                <FormField
                    id="confirmPassword"
                    label="POTWIERDŹ NOWE HASŁO:"
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="Powtórz nowe hasło"
                    disabled={mutation.isPending}
                />

                {passwordError && (
                    <p className="text-error text-xs font-bold -mt-2 ml-2">{passwordError}</p>
                )}
                {error && isTokenValid && <p className="text-error text-sm text-center">{error}</p>}

                <button
                    type="submit"
                    disabled={
                        mutation.isPending ||
                        !!passwordError ||
                        !password ||
                        !confirmPassword
                    }
                    className="w-full max-h-[38px] py-4 px-3 rounded-[53px] mt-4 bg-primary text-darkgray font-semibold hover:bg-secondary cursor-pointer transition-colors flex items-center justify-center disabled:bg-inputbg disabled:text-txtcolor-300 disabled:cursor-not-allowed"
                >
                    {mutation.isPending ? 'Zapisywanie...' : 'Zresetuj hasło'}
                </button>
            </form>
        );
    };

    return (
        <main className="min-h-screen flex items-center justify-center p-3">
            <div className="w-full text-center max-w-[404px] rounded-[11.83px] px-3 py-10 flex flex-col justify-center items-center bg-secondarybg border border-border shadow-2xl">
                <div className="flex items-center flex-col">
                    <SchoolRounded className="w-16 h-16 text-primary mb-2" />
                    <h1 className="text-[23.42px] font-semibold mt-4 text-foreground">Ustaw nowe hasło</h1>
                </div>
                {renderContent()}
            </div>
        </main>
    );
}

export default function ResetPasswordPage() {
    return (
        <Suspense fallback={<div className="min-h-screen flex items-center justify-center text-primary">Ładowanie...</div>}>
            <ResetPasswordForm/>
        </Suspense>
    )
}