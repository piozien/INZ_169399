'use client';

import { useState, FormEvent, Suspense, useEffect } from 'react';
import { useMutation } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

import SchoolRounded from '@/components/icons/SchoolRounded';
import FormField from '@/components/FormField';
import { UserRequestDto } from '@/types/auth.types';
import { registerUser } from '@/lib/api/auth';

function RegistrationForm() {
    const [fullName, setFullName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState<string | null>(null);
    const router = useRouter();

    useEffect(() => {
        if (confirmPassword && password !== confirmPassword) {
            setError('Hasła nie są identyczne');
        } else if (error === 'Hasła nie są identyczne') {
            setError(null);
        }
    }, [password, confirmPassword]);

    const registrationMutation = useMutation<void, Error, UserRequestDto>({
        mutationFn: registerUser,
        onSuccess: () => {
            router.push('/login?registered=true');
        },
        onError: (err) => {
            setError(err.message);
        },
    });

    const handleRegister = (e: FormEvent) => {
        e.preventDefault();
        setError(null);

        if (password !== confirmPassword) {
            setError('Hasła nie są identyczne');
            return;
        }

        if (password.length < 8) {
            setError('Hasło musi mieć co najmniej 8 znaków');
            return;
        }

        registrationMutation.mutate({ fullName, email, password });
    };

    return (
        <main className="flex min-h-screen items-center justify-center p-3">
            <div className="bg-secondarybg flex w-full max-w-[404px] flex-col items-center justify-center rounded-[11.83px] px-3 py-7 text-center">
                <div className="flex flex-col items-center">
                    <SchoolRounded />
                    <h1 className="mt-4 text-[23.42px] font-semibold">Zarejestruj się</h1>
                    <p className="text-txtcolor-300 mt-3 max-w-[350px] text-sm">
                        Utwórz nowe konto w portalu samorządu szkolnego.
                    </p>
                </div>

                <form onSubmit={handleRegister} className="mt-8 flex w-10/12 flex-col gap-4">
                    <FormField
                        id="fullName"
                        label="IMIĘ I NAZWISKO:"
                        type="text"
                        value={fullName}
                        onChange={(e) => setFullName(e.target.value)}
                        placeholder="Jan Kowalski"
                        disabled={registrationMutation.isPending}
                    />

                    <FormField
                        id="email"
                        label="ADRES E-MAIL:"
                        type="email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="jan@kowalski.pl"
                        disabled={registrationMutation.isPending}
                    />

                    <FormField
                        id="password"
                        label="HASŁO:"
                        type="password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="Minimum 8 znaków"
                        disabled={registrationMutation.isPending}
                    />

                    <FormField
                        id="confirmPassword"
                        label="POTWIERDŹ HASŁO:"
                        type="password"
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        placeholder="Powtórz hasło"
                        disabled={registrationMutation.isPending}
                    />

                    {error && <p className="text-error text-sm">{error}</p>}

                    <button
                        type="submit"
                        disabled={registrationMutation.isPending}
                        className="bg-primary text-darkgray hover:bg-secondary mt-4 flex max-h-[38px] w-full cursor-pointer items-center justify-center rounded-[53px] px-3 py-4 font-semibold transition-colors"
                    >
                        {registrationMutation.isPending ? 'Rejestrowanie...' : 'Zarejestruj się'}
                    </button>
                </form>

                <div className="mt-auto pt-4 text-sm">
                    <p>
                        Masz już konto?{' '}
                        <Link href="/login" className="text-secondary font-medium hover:underline">
                            Zaloguj się
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

export default function RegisterPage() {
    return (
        <Suspense
            fallback={
                <main>
                    <h1>Ładowanie...</h1>
                </main>
            }
        >
            <RegistrationForm />
        </Suspense>
    );
}
