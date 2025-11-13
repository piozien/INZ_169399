"use client";

import { Suspense, useEffect } from "react";
import { useRouter, useSearchParams } from "next/navigation";

function OAuthErrorComponent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const message = searchParams.get("message") ?? "Nie udało się ukończyć procesu logowania.";

  useEffect(() => {
    const redirect = setTimeout(() => {
      router.replace("/login");
    }, 4000);

    return () => clearTimeout(redirect);
  }, [router]);

  return (
    <section>
      <h1>Logowanie nie powiodło się</h1>
      <p>{message}</p>
      <p>Zostaniesz przekierowany na stronę logowania za chwilę.</p>
    </section>
  );
}

export default function OAuthErrorPage() {
  return (
    <Suspense fallback={<section><h1>Przetwarzanie błędu…</h1></section>}>
      <OAuthErrorComponent />
    </Suspense>
  );
}
