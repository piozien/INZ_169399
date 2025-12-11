'use client';

import { Suspense } from 'react';
import Link from 'next/link';
import SchoolRounded from '@/components/icons/SchoolRounded';
import FormField from '@/components/FormField';
import { useResetPassword } from '@/hooks/auth/useResetPassword';

function ResetPasswordForm() {
    const {
        password,
        setPassword,
        confirmPassword,
        setConfirmPassword,
        passwordError,
        handleSubmit,
        isSubmitting,
        isValidating,
        isTokenValid,
        success,
        error,
        countdown,
    } = useResetPassword();

    const renderContent = () => {
        if (isValidating) {
            return (
                <p className="text-txtcolor-300 animate-pulse">
                    Weryfikowanie tokenu bezpieczeństwa...
                </p>
            );
        }

        if (!isTokenValid) {
            return (
                <div className="animate-in fade-in mt-8 w-10/12 text-center">
                    <p className="text-error mb-4 font-medium">{error}</p>
                    <div className="text-txtcolor-300 text-sm">
                        Przekierowanie do strony odzyskiwania hasła za {countdown}s...
                    </div>
                    <Link
                        href="/forgot-password"
                        className="bg-secondarybg border-secondary text-secondary hover:bg-secondary mt-6 inline-block rounded-full border px-6 py-2 text-sm font-bold transition-all hover:text-white"
                    >
                        Przejdź teraz
                    </Link>
                </div>
            );
        }

        if (success) {
            return (
                <div className="animate-in fade-in mt-8 w-10/12 text-center">
                    <p className="text-success mb-2 text-lg font-bold">
                        Hasło zostało pomyślnie zmienione!
                    </p>
                    <p className="text-txtcolor-300 mt-2 text-sm">
                        Zostaniesz przekierowany na stronę logowania za {countdown}s...
                    </p>
                    <Link
                        href="/login"
                        className="bg-primary text-darkgray mt-6 inline-block rounded-full px-6 py-2 text-sm font-bold transition-all hover:opacity-90"
                    >
                        Zaloguj się teraz
                    </Link>
                </div>
            );
        }

        return (
            <form
                onSubmit={handleSubmit}
                className="animate-in slide-in-from-bottom-2 mt-8 flex w-10/12 flex-col gap-5"
            >
                <FormField
                    id="password"
                    label="NOWE HASŁO:"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="Wprowadź nowe hasło"
                    disabled={isSubmitting}
                />
                <FormField
                    id="confirmPassword"
                    label="POTWIERDŹ NOWE HASŁO:"
                    type="password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="Powtórz nowe hasło"
                    disabled={isSubmitting}
                />

                {passwordError && (
                    <p className="text-error -mt-2 ml-2 text-xs font-bold">{passwordError}</p>
                )}
                {error && <p className="text-error text-center text-sm">{error}</p>}

                <button
                    type="submit"
                    disabled={isSubmitting || !!passwordError || !password || !confirmPassword}
                    className="bg-primary text-darkgray hover:bg-secondary disabled:bg-inputbg disabled:text-txtcolor-300 mt-4 flex max-h-[38px] w-full cursor-pointer items-center justify-center rounded-[53px] px-3 py-4 font-semibold transition-colors disabled:cursor-not-allowed"
                >
                    {isSubmitting ? 'Zapisywanie...' : 'Zresetuj hasło'}
                </button>
            </form>
        );
    };

    return (
        <main className="flex min-h-screen items-center justify-center p-3">
            <div className="bg-secondarybg border-border flex w-full max-w-[404px] flex-col items-center justify-center rounded-[11.83px] border px-3 py-10 text-center shadow-2xl">
                <div className="flex flex-col items-center">
                    <SchoolRounded className="text-secondary mb-2 h-16 w-16" />
                    <h1 className="text-foreground mt-4 text-[23.42px] font-semibold">
                        Ustaw nowe hasło
                    </h1>
                </div>
                {renderContent()}
            </div>
        </main>
    );
}

export default function ResetPasswordPage() {
    return (
        <Suspense
            fallback={
                <div className="text-primary flex min-h-screen items-center justify-center">
                    Ładowanie...
                </div>
            }
        >
            <ResetPasswordForm />
        </Suspense>
    );
}
