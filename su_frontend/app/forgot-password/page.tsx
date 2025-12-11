'use client';

import { useState, FormEvent } from 'react';
import { useMutation } from '@tanstack/react-query';
import Link from 'next/link';

import SchoolRounded from '@/components/icons/SchoolRounded';
import FormField from '@/components/FormField';
import { requestPasswordReset } from '@/lib/api/auth';

export default function ForgotPasswordPage() {
    const [email, setEmail] = useState('');
    const [successMessage, setSuccessMessage] = useState<string | null>(null);

    const mutation = useMutation({
        mutationFn: requestPasswordReset,
        onSuccess: () => {
            setSuccessMessage(
                'Jeśli konto powiązane z tym adresem e-mail istnieje, wysłaliśmy na nie link do resetowania hasła.'
            );
        },
        onError: () => {
            setSuccessMessage(
                'Jeśli konto powiązane z tym adresem e-mail istnieje, wysłaliśmy na nie link do resetowania hasła.'
            );
        },
    });

    const handleRequestReset = (e: FormEvent) => {
        e.preventDefault();
        setSuccessMessage(null);
        mutation.mutate({ email });
    };

    return (
        <main className="flex min-h-screen items-center justify-center p-3">
            <div className="bg-secondarybg flex w-full max-w-[404px] flex-col items-center justify-center rounded-[11.83px] px-3 py-10 text-center">
                <div className="flex flex-col items-center">
                    <SchoolRounded />
                    <h1 className="mt-4 text-[23.42px] font-semibold">Resetowanie hasła</h1>
                    <p className="text-txtcolor-300 mt-3 max-w-[350px] text-sm">
                        Podaj adres e-mail powiązany z Twoim kontem, a wyślemy Ci link do
                        zresetowania hasła.
                    </p>
                </div>

                {successMessage ? (
                    <div className="mt-8 w-10/12 text-center">
                        <p className="text-success">{successMessage}</p>
                        <Link
                            href="/login"
                            className="text-secondary mt-4 inline-block font-medium"
                        >
                            Wróć do logowania
                        </Link>
                    </div>
                ) : (
                    <form
                        onSubmit={handleRequestReset}
                        className="mt-8 flex w-10/12 flex-col gap-5"
                    >
                        <FormField
                            id="email"
                            label="ADRES E-MAIL:"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="jan@kowalski.pl"
                            disabled={mutation.isPending}
                        />

                        <button
                            type="submit"
                            disabled={mutation.isPending}
                            className="bg-primary text-darkgray hover:bg-secondary mt-4 flex max-h-[38px] w-full cursor-pointer items-center justify-center rounded-[53px] px-3 py-4 font-semibold transition-colors"
                        >
                            {mutation.isPending ? 'Wysyłanie...' : 'Wyślij link do resetowania'}
                        </button>
                    </form>
                )}
                <div className="mt-auto pt-4 text-sm">
                    <p>
                        Pamiętasz hasło?{' '}
                        <Link href="/login" className="text-secondary font-medium">
                            Zaloguj się
                        </Link>
                    </p>
                </div>
            </div>
        </main>
    );
}
