"use client";

import { useState, FormEvent, Suspense, useEffect } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { hashPassword } from "@/lib/security";
import { GraduationCap } from "lucide-react";

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
      setSuccess(
        "Rejestracja zakończona pomyślnie! Możesz się teraz zalogować."
      );
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

      router.push("/dashboard");
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Wystąpił błąd podczas logowania"
      );
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
      <div>
        <GraduationCap size={60} />
        <h1>Zaloguj się</h1>
        <p>Użyj swojego adresu email i hasła, aby zalogować się do portalu samorządu szkolnego.</p>

        <form onSubmit={handleEmailLogin}>
          <div>
            <label htmlFor="email">ADRES E-MAIL:</label>
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
          {error && <p>{error}</p>}
          {success && <p>{success}</p>}
          <button type="submit" disabled={isLoading}>
            {isLoading ? "Logowanie..." : "Zaloguj się"}
          </button>
        </form>

        <div>
          <p>lub</p>
          <button
            type="button"
            onClick={handleMicrosoftLogin}
            disabled={isLoading}
          >
            Zaloguj się przez Microsoft
          </button>
        </div>

        <p>
          Nie masz konta? <Link href="/register">Zarejestruj się</Link>
        </p>
        <p>
          <Link href="/">Wróć do strony głównej</Link>
        </p>
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
