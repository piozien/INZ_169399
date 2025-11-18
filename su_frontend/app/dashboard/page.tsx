"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { useCurrentUser } from "@/lib/hooks/useCurrentUser";
import { logout } from "@/lib/api/auth";

export default function DashboardPage() {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { data: user, isLoading, error } = useCurrentUser();

  useEffect(() => {
    if (error) {
      router.replace("/login");
    }
  }, [error, router]);

  if (error) {
    return null;
  }

  if (isLoading || !user) {
    return (
      <main className="p-8 bg-background">
        <h1>Ładowanie panelu…</h1>
      </main>
    );
  }

  const handleLogout = async () => {
    await logout();
    queryClient.invalidateQueries({ queryKey: ["currentUser"] });
    router.replace("/login");
  };

  return (
    <main className="p-8 bg-background">
      <h1>Witaj, {user.fullName}</h1>
      <p>Jesteś zalogowany jako {user.email}.</p>
      <button type="button" onClick={handleLogout}>
        Wyloguj
      </button>
    </main>
  );
}
