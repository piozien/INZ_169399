import { LoginRequestDTO } from "@/types/auth.types";
import { User } from "@/types/user.types";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export async function login(payload: LoginRequestDTO): Promise<User> {
  const response = await fetch(`${API_URL}/api/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    credentials: "include",
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Nieprawidłowy email lub hasło");
  }

  const data = await response.json();
  return data.user;
}

export async function logout(): Promise<void> {
  await fetch(`${API_URL}/api/auth/logout`, {
    method: 'POST',
    credentials: 'include',
  });
}

export async function requestPasswordReset(payload: {
  email: string;
}): Promise<void> {
  const response = await fetch(`${API_URL}/api/auth/password-reset/request`, {
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
    `${API_URL}/api/auth/password-reset/validate/${token}`,
  );

  if (!response.ok) {
    throw new Error('Nieprawidłowy lub nieważny token.');
  }
}

export async function confirmPasswordReset(payload: {
  token: string;
  newPassword: string;
}): Promise<void> {
  const response = await fetch(`${API_URL}/api/auth/password-reset/confirm`, {
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
