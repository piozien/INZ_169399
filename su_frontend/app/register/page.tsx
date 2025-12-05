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
        <main className="min-h-screen flex items-center justify-center p-3">
            <div className="w-full text-center max-w-[404px] rounded-[11.83px] px-3 py-7 flex flex-col justify-center items-center bg-secondarybg">
                <div className="flex items-center flex-col">
                    <SchoolRounded />
                    <h1 className="text-[23.42px] font-semibold mt-4">Zarejestruj się</h1>
                    <p className="mt-3 text-sm max-w-[350px] text-txtcolor-300">
                        Utwórz nowe konto w portalu samorządu szkolnego.
                    </p>
                </div>

                <form
                    onSubmit={handleRegister}
                    className="w-10/12 mt-8 flex flex-col gap-4"
                >
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
                        className="w-full max-h-[38px] py-4 px-3 rounded-[53px] mt-4 bg-primary text-darkgray font-semibold hover:bg-secondary cursor-pointer transition-colors flex items-center justify-center"
                    >
                        {registrationMutation.isPending
                            ? 'Rejestrowanie...'
                            : 'Zarejestruj się'}
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