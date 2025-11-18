'use client';

import { useState, FormEvent, Suspense, useEffect } from 'react';
import { useQueryClient, useMutation } from '@tanstack/react-query';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';

import SchoolRounded from '@/components/icons/SchoolRounded';
import MicrosoftIcon from '@/components/icons/MicrosoftIcon';
import FormField from '@/components/FormField';
import { LoginRequestDTO } from '@/types/auth.types';
import { login } from '@/lib/api/auth';

function LoginForm() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();
  const searchParams = useSearchParams();
  const queryClient = useQueryClient();

  const registered = searchParams.get('registered') === 'true';
  const successMessage = registered
    ? 'Rejestracja zakończona pomyślnie! Na podany adres email wysłano link aktywacyjny.'
    : null;

  const loginMutation = useMutation({
    mutationFn: login,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['currentUser'] });
      router.push('/dashboard');
    },
    onError: (err) => {
      setError(
        err instanceof Error ? err.message : 'Wystąpił błąd podczas logowania',
      );
    },
  });

  const handleEmailLogin = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    const payload: LoginRequestDTO = {
      email: email,
      password: password,
    };

    loginMutation.mutate(payload);
  };

  const handleMicrosoftLogin = () => {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
    const oauth2Url = `${apiUrl}/oauth2/authorization/microsoft`;
    window.location.href = oauth2Url;
  };

  return (
    <main className="min-h-screen flex items-center justify-center p-3">
      <div className="w-full text-center max-w-[404px] rounded-[11.83px] px-3 py-10 flex flex-col justify-center items-center bg-secondarybg">
        <div className="flex items-center flex-col">
          <SchoolRounded />
          <h1 className="text-[23.42px] font-semibold mt-4">Zaloguj się</h1>
          <p className="mt-3 text-sm max-w-[350px] text-txtcolor-300">
            Użyj swojego adresu email i hasła, aby zalogować się do portalu
            samorządu szkolnego.
          </p>
        </div>

        <form
          onSubmit={handleEmailLogin}
          className="w-10/12 mt-8 flex flex-col gap-5"
        >
          <FormField
            id="email"
            label="ADRES E-MAIL:"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="jan@kowalski.pl"
            disabled={loginMutation.isPending}
          />

          <FormField
            id="password"
            label="Hasło:"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Twoje hasło"
            disabled={loginMutation.isPending}
          />

          {error && <p className="text-red-500 text-sm">{error}</p>}
          {successMessage && (
            <p className="text-green-500 text-sm">{successMessage}</p>
          )}

          <button
            type="submit"
            disabled={loginMutation.isPending}
            className="w-full max-h-[38px] py-4 px-3 rounded-[53px] mt-4 bg-primary text-darkgray font-semibold hover:bg-secondary cursor-pointer transition-colors flex items-center justify-center"
          >
            {loginMutation.isPending ? 'Logowanie...' : 'Zaloguj się'}
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

        <div className="w-10/12 mt-8 flex flex-col gap-5 items-center">
          <div className="w-full flex items-center gap-4">
            <div className="flex-1 border-b border-txtcolor-300" />
            <p className="text-sm text-text-muted">lub</p>
            <div className="flex-1 border-b border-txtcolor-300" />
          </div>

          <button
            type="button"
            onClick={handleMicrosoftLogin}
            disabled={loginMutation.isPending}
            className="w-full max-h-[38px] py-4 px-3 rounded-[53px] mt-4 bg-microsoftbg cursor-pointer text-darkgray font-semibold flex items-center justify-center gap-3"
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
            <Link
              href="/"
              className="text-secondary font-medium hover:underline"
            >
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
        <main>
          <h1>Ładowanie...</h1>
        </main>
      }
    >
      <LoginForm />
    </Suspense>
  );
}
