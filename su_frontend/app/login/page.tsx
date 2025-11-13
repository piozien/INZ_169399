"use client";

import { useState, FormEvent, Suspense, useEffect } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { hashPassword } from "@/lib/security";

function LoginForm() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();
  const searchParams = useSearchParams();
  const queryClient = useQueryClient();

  useEffect(() => {
    if (searchParams.get("registered") === "true") {
      setSuccess("Rejestracja zakończona pomyślnie! Możesz się teraz zalogować.");
    }
  }, [searchParams]);

  const handleEmailLogin = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      const passwordDigest = await hashPassword(password);
      const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
      const response = await fetch(`${apiUrl}/api/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        credentials: "include",
        body: JSON.stringify({ email, password: passwordDigest }),
      });

      if (!response.ok) {
        let message = "Nieprawidłowy email lub hasło";
        try {
          const errorData = await response.json();
          message = errorData.message ?? message;
        } catch {
          // ignore
        }
        throw new Error(message);
      }

      await queryClient.invalidateQueries({ queryKey: ["currentUser"] });
      router.push("/dashboard");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Wystąpił błąd podczas logowania");
    } finally {
      setIsLoading(false);
    }
  };

  const handleMicrosoftLogin = () => {
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
    const oauth2Url = `${apiUrl}/oauth2/authorization/microsoft`;
    window.location.href = oauth2Url;
  };

  return (
    <main>
      <h1>Logowanie</h1>
      <p>Wybierz metodę logowania.</p>

      <form onSubmit={handleEmailLogin}>
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
            disabled={isLoading}
          />
        </div>
        {error && <p style={{ color: "red" }}>{error}</p>}
        {success && <p style={{ color: "green" }}>{success}</p>}
        <button type="submit" disabled={isLoading}>
          {isLoading ? "Logowanie..." : "Zaloguj się"}
        </button>
      </form>

      <div>
        <p>lub</p>
        <button type="button" onClick={handleMicrosoftLogin} disabled={isLoading}>
          Zaloguj się przez Microsoft
        </button>
      </div>

      <p>
        Nie masz konta? <Link href="/register">Zarejestruj się</Link>
      </p>
      <p>
        <Link href="/">Wróć do strony głównej</Link>
      </p>
    </main>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={<main><h1>Ładowanie...</h1></main>}>
      <LoginForm />
    </Suspense>
  );
}
