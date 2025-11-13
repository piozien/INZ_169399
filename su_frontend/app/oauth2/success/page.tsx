"use client";

import { Suspense, useEffect } from "react";
import { useSearchParams, useRouter } from "next/navigation";

function OAuth2RedirectHandlerComponent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  useEffect(() => {
    const accessToken = searchParams.get("accessToken");
    const refreshToken = searchParams.get("refreshToken");
    const error = searchParams.get("error");

    if (error) {
      router.replace(`/oauth2/error?message=${encodeURIComponent(error)}`);
      return;
    }

    if (accessToken && refreshToken) {
      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("refreshToken", refreshToken);
      router.replace("/dashboard");
      return;
    }

    router.replace("/oauth2/error?message=Brak+tokenów+autoryzacyjnych");
  }, [router, searchParams]);

  return null;
}

export default function OAuth2RedirectHandlerPage() {
  return (
    <section>
      <h1>Finalizowanie logowania…</h1>
      <p>Proszę czekać, trwa finalizowanie procesu uwierzytelniania.</p>
      <Suspense fallback={<p>Przetwarzanie danych logowania…</p>}>
        <OAuth2RedirectHandlerComponent />
      </Suspense>
    </section>
  );
}
