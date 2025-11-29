
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
        'Jeśli konto powiązane z tym adresem e-mail istnieje, wysłaliśmy na nie link do resetowania hasła.',
      );
    },
    onError: () => {
      setSuccessMessage(
        'Jeśli konto powiązane z tym adresem e-mail istnieje, wysłaliśmy na nie link do resetowania hasła.',
      );
    },
  });

  const handleRequestReset = (e: FormEvent) => {
    e.preventDefault();
    setSuccessMessage(null);
    mutation.mutate({ email });
  };

  return (
    <main className="min-h-screen flex items-center justify-center p-3">
      <div className="w-full text-center max-w-[404px] rounded-[11.83px] px-3 py-10 flex flex-col justify-center items-center bg-secondarybg">
        <div className="flex items-center flex-col">
          <SchoolRounded />
          <h1 className="text-[23.42px] font-semibold mt-4">
            Resetowanie hasła
          </h1>
          <p className="mt-3 text-sm max-w-[350px] text-txtcolor-300">
            Podaj adres e-mail powiązany z Twoim kontem, a wyślemy Ci link do
            zresetowania hasła.
          </p>
        </div>

        {successMessage ? (
          <div className="w-10/12 mt-8 text-center">
            <p className="text-success">{successMessage}</p>
            <Link
              href="/login"
              className="mt-4 inline-block text-secondary font-medium"
            >
              Wróć do logowania
            </Link>
          </div>
        ) : (
          <form
            onSubmit={handleRequestReset}
            className="w-10/12 mt-8 flex flex-col gap-5"
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
              className="w-full max-h-[38px] py-4 px-3 rounded-[53px] mt-4 bg-primary text-darkgray font-semibold hover:bg-secondary cursor-pointer transition-colors flex items-center justify-center"
            >
              {mutation.isPending
                ? 'Wysyłanie...'
                : 'Wyślij link do resetowania'}
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
