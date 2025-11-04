"use client";

import { useEffect, Suspense } from "react";
import { useSearchParams, useRouter } from "next/navigation";

function OAuth2RedirectHandlerComponent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  useEffect(() => {
    const accessToken = searchParams.get("accessToken");
    const refreshToken = searchParams.get("refreshToken");
    const error = searchParams.get("error");

    if (error) {
      console.error("OAuth2 login error:", error);
      router.push("/login?error=" + encodeURIComponent(error));
      return;
    }

    if (accessToken && refreshToken) {
      console.log("Saving tokens...");
      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("refreshToken", refreshToken);
      router.push("/dashboard");
    } else {
      console.warn("No tokens in the URL after OAuth2 redirection");
      router.push("/login");
    }
  }, [searchParams, router]);

  return null;
}

export default function OAuth2RedirectHandlerPage() {
  return (
    <main>
      <h1>Logowanie...</h1>
      <p>Proszę czekać, trwa finalizowanie procesu logowania.</p>
      <Suspense fallback={<div>Przetwarzanie danych...</div>}>
        <OAuth2RedirectHandlerComponent />
      </Suspense>
    </main>
  );
}
