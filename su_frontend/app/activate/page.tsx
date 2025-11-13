"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";

function ActivateComponent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [status, setStatus] = useState<"loading" | "success" | "error">("loading");
  const [message, setMessage] = useState<string>("");

  useEffect(() => {
    const token = searchParams.get("token");
    if (!token) {
      setStatus("error");
      setMessage("Brak tokenu aktywacyjnego.");
      return;
    }

    const activateAccount = async () => {
      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
        const response = await fetch(`${apiUrl}/api/auth/activate?token=${encodeURIComponent(token)}`, {
          method: "POST",
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}));
          throw new Error(errorData.message || "Aktywacja konta nie powiodła się.");
        }

        setStatus("success");
        setMessage("Konto zostało pomyślnie aktywowane! Możesz się teraz zalogować.");
        setTimeout(() => {
          router.push("/login");
        }, 3000);
      } catch (err) {
        setStatus("error");
        setMessage(err instanceof Error ? err.message : "Wystąpił błąd podczas aktywacji konta.");
      }
    };

    activateAccount();
  }, [searchParams, router]);

  if (status === "loading") {
    return (
      <main>
        <h1>Aktywacja konta</h1>
        <p>Trwa aktywacja Twojego konta. Proszę czekać...</p>
      </main>
    );
  }

  if (status === "success") {
    return (
      <main>
        <h1>Konto aktywowane</h1>
        <p style={{ color: "green" }}>{message}</p>
        <p>Zostaniesz przekierowany na stronę logowania za chwilę.</p>
        <p>
          <Link href="/login">Przejdź do logowania teraz</Link>
        </p>
      </main>
    );
  }

  return (
    <main>
      <h1>Błąd aktywacji</h1>
      <p style={{ color: "red" }}>{message}</p>
      <p>
        <Link href="/login">Przejdź do logowania</Link>
      </p>
      <p>
        <Link href="/">Wróć do strony głównej</Link>
      </p>
    </main>
  );
}

export default function ActivatePage() {
  return (
    <Suspense fallback={<main><h1>Ładowanie...</h1></main>}>
      <ActivateComponent />
    </Suspense>
  );
}

