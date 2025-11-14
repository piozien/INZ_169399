"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";

export default function OAuth2RedirectHandlerPage() {
  const router = useRouter();
  const queryClient = useQueryClient();

  useEffect(() => {

    queryClient.invalidateQueries({ queryKey: ["currentUser"] });
    router.replace("/dashboard");
  }, [router, queryClient]);

  return (
    <section>
      <h1>Finalizowanie logowania…</h1>
      <p>Proszę czekać, trwa finalizowanie procesu uwierzytelniania.</p>
    </section>
  );
}
