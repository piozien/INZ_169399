"use client";

import { useState, FormEvent } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

export default function RegisterPage() {
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();

  const handleRegister = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (password !== confirmPassword) {
      setError("Hasła nie są identyczne");
      return;
    }

    if (password.length < 8) {
      setError("Hasło musi mieć co najmniej 8 znaków");
      return;
    }

    setIsLoading(true);

    try {
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
      const response = await fetch(`${apiUrl}/api/auth/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({
          fullName,
          email,
          password,
          authProvider: "LOCAL",
        }),
      });

      if (!response.ok) {
        let errorMessage = "Rejestracja nie powiodła się. Sprawdź dane i spróbuj ponownie.";
        try {
          const errorData = await response.json();
          errorMessage = errorData.message || errorMessage;
        } catch {
        }
        throw new Error(errorMessage);
      }

      router.push("/login?registered=true");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Wystąpił błąd podczas rejestracji");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <main>
      <h1>Rejestracja</h1>
      <p>Utwórz nowe konto w portalu samorządu szkolnego.</p>

      <form onSubmit={handleRegister}>
        <div>
          <label htmlFor="fullName">Imię i nazwisko:</label>
          <input
            id="fullName"
            type="text"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            required
            minLength={2}
            maxLength={100}
            disabled={isLoading}
          />
        </div>
        <div>
          <label htmlFor="email">Email:</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            disabled={isLoading}
          />
        </div>
        <div>
          <label htmlFor="password">Hasło:</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={8}
            disabled={isLoading}
          />
          <small>Minimum 8 znaków</small>
        </div>
        <div>
          <label htmlFor="confirmPassword">Potwierdź hasło:</label>
          <input
            id="confirmPassword"
            type="password"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
            minLength={8}
            disabled={isLoading}
          />
        </div>
        {error && <p style={{ color: "red" }}>{error}</p>}
        <button type="submit" disabled={isLoading}>
          {isLoading ? "Rejestrowanie..." : "Zarejestruj się"}
        </button>
      </form>

      <p>
        Masz już konto? <Link href="/login">Zaloguj się</Link>
      </p>
      <p>
        <Link href="/">Wróć do strony głównej</Link>
      </p>
    </main>
  );
}

