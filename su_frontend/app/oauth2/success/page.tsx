"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";

export default function OAuth2SuccessPage() {
  const router = useRouter();
  const queryClient = useQueryClient();

  useEffect(() => {
    // Invalidate the currentUser query to force a refetch
    queryClient.invalidateQueries({ queryKey: ['currentUser'] });
    // Redirect to the dashboard immediately
    router.push('/dashboard');
  }, [router, queryClient]);

  return (
    <div className="min-h-screen flex items-center justify-center">
      <p>Przekierowywanie...</p>
    </div>
  );
}
