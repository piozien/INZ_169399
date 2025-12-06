import { UserRequestDto } from "@/types/auth.types";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export async function registerUser(payload: UserRequestDto): Promise<void> {
    const response = await fetch(`${API_URL}/auth/register`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
            ...payload,
            authProvider: 'LOCAL',
        }),
    });

    if (!response.ok) {
        let errorMessage = 'Rejestracja nie powiodła się. Sprawdź dane i spróbuj ponownie.';
        try {
            const errorData = await response.json();
            errorMessage = errorData.message || errorMessage;
        } catch {
            // ignore
        }
        throw new Error(errorMessage);
    }
}

export async function activateAccount(token: string): Promise<void> {
    const response = await fetch(`${API_URL}/auth/activate?token=${token}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Link aktywacyjny jest nieprawidłowy lub wygasł.');
    }
}

export async function requestPasswordReset(payload: {
  email: string;
}): Promise<void> {
  const response = await fetch(`${API_URL}/auth/password-reset/request`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    console.error('Password reset request failed, but we are hiding the error.');
  }
}

export async function validatePasswordResetToken(token: string): Promise<void> {
  const response = await fetch(
    `${API_URL}/auth/password-reset/validate/${token}`,
  );

  if (!response.ok) {
    throw new Error('Nieprawidłowy lub nieważny token.');
  }
}

export async function confirmPasswordReset(payload: {
  token: string;
  newPassword: string;
}): Promise<void> {
  const response = await fetch(`${API_URL}/auth/password-reset/confirm`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Nie udało się zresetować hasła.');
  }
}
