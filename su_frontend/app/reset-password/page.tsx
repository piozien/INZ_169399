
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
          'Token jest nieprawidłowy lub wygasł. Proszę, spróbuj ponownie.',
        );
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
    if (success) {
      const timer = setInterval(() => {
        setCountdown((prevCountdown) => prevCountdown - 1);
      }, 1000);

      if (countdown === 0) {
        clearInterval(timer);
        router.push('/login');
      }

      return () => clearInterval(timer);
    }
  }, [success, countdown, router]);

  const mutation = useMutation({
    mutationFn: confirmPasswordReset,
    onSuccess: () => {
      setSuccess(true);
      setError(null);
    },
    onError: (err) => {
      setError(err instanceof Error ? err.message : 'Wystąpił nieznany błąd.');
    },
  });

  const handleResetPassword = (e: FormEvent) => {
    e.preventDefault();
    if (!token) {
      setError('Brak tokenu, nie można zresetować hasła.');
      return;
    }
    setError(null);
    mutation.mutate({ token, newPassword: password });
  };

  const renderContent = () => {
    if (isValidating) {
      return <p>Weryfikowanie tokenu...</p>;
    }

    if (!isTokenValid || (error && !mutation.isPending)) {
      return (
        <div className="w-10/12 mt-8 text-center">
          <p className="text-red-500">{error}</p>
          <Link
            href="/forgot-password"
            className="mt-4 inline-block text-secondary font-medium"
          >
            Zacznij od nowa
          </Link>
        </div>
      );
    }

    if (success) {
      return (
        <div className="w-10/12 mt-8 text-center">
          <p className="text-green-500">
            Hasło zostało pomyślnie zresetowane!
          </p>
          <p className="mt-2 text-sm text-txtcolor-300">
            Zostaniesz przekierowany na stronę logowania za {countdown}s...
          </p>
        </div>
      );
    }

    return (
      <form
        onSubmit={handleResetPassword}
        className="w-10/12 mt-8 flex flex-col gap-5"
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
          <p className="text-red-500 text-sm -mt-2">{passwordError}</p>
        )}
        {error && <p className="text-red-500 text-sm">{error}</p>}
        <button
          type="submit"
          disabled={
            mutation.isPending ||
            !!passwordError ||
            !password ||
            !confirmPassword
          }
          className="w-full max-h-[38px] py-4 px-3 rounded-[53px] mt-4 bg-primary text-darkgray font-semibold hover:bg-secondary cursor-pointer transition-colors flex items-center justify-center disabled:bg-neutral-500 disabled:cursor-not-allowed"
        >
          {mutation.isPending ? 'Zapisywanie...' : 'Zresetuj hasło'}
        </button>
      </form>
    );
  };

  return (
    <main className="min-h-screen flex items-center justify-center p-3">
      <div className="w-full text-center max-w-[404px] rounded-[11.83px] px-3 py-10 flex flex-col justify-center items-center bg-secondarybg">
        <div className="flex items-center flex-col">
          <SchoolRounded />
          <h1 className="text-[23.42px] font-semibold mt-4">Ustaw nowe hasło</h1>
        </div>
        {renderContent()}
      </div>
    </main>
  );
}


export default function ResetPasswordPage() {
    return (
        <Suspense fallback={<div>Ładowanie...</div>}>
            <ResetPasswordForm/>
        </Suspense>
    )
}
