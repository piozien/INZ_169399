import { useState, useEffect, FormEvent } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useMutation } from '@tanstack/react-query';
import { validatePasswordResetToken, confirmPasswordReset } from '@/lib/api/auth';
import { toast } from 'sonner';

export const useResetPassword = () => {
    const router = useRouter();
    const searchParams = useSearchParams();
    const token = searchParams.get('token');

    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [passwordError, setPasswordError] = useState<string | null>(null);

    const [pageError, setPageError] = useState<string | null>(null);
    const [isValidating, setIsValidating] = useState(true);
    const [isTokenValid, setIsTokenValid] = useState(false);

    const [success, setSuccess] = useState(false);
    const [countdown, setCountdown] = useState(5);

    useEffect(() => {
        if (!token) {
            setPageError('Brak tokenu resetowania w adresie URL.');
            setIsValidating(false);
            return;
        }

        const validateToken = async () => {
            try {
                await validatePasswordResetToken(token);
                setIsTokenValid(true);
            } catch (e) {
                setPageError(
                    'Token jest nieprawidłowy lub wygasł. Zostaniesz przekierowany, aby wygenerować nowy.'
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
        const shouldRedirect = success || (!isValidating && !isTokenValid && pageError);

        if (shouldRedirect) {
            const timer = setInterval(() => {
                setCountdown((prev) => prev - 1);
            }, 1000);

            if (countdown === 0) {
                clearInterval(timer);
                router.push(success ? '/login' : '/forgot-password');
            }
            return () => clearInterval(timer);
        }
    }, [success, isValidating, isTokenValid, pageError, countdown, router]);

    const mutation = useMutation({
        mutationFn: confirmPasswordReset,
        onSuccess: () => {
            setSuccess(true);
            setCountdown(5);
            toast.success('Hasło zostało zmienione!', {
                description: 'Zostaniesz przekierowany do strony logowania.',
                duration: 5000,
            });
        },
        onError: (err: any) => {
            toast.error('Nie udało się zmienić hasła', {
                description: err.message || 'Wystąpił nieznany błąd.',
            });
        },
    });

    const handleSubmit = (e: FormEvent) => {
        e.preventDefault();
        if (!token) return;
        if (password !== confirmPassword) {
            toast.error('Hasła muszą być identyczne');
            return;
        }

        mutation.mutate({ token, newPassword: password });
    };

    return {
        password,
        setPassword,
        confirmPassword,
        setConfirmPassword,
        passwordError,
        handleSubmit,
        isSubmitting: mutation.isPending,

        isValidating,
        isTokenValid,
        success,
        error: pageError,
        countdown,
    };
};