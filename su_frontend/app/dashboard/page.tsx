"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

export default function DashboardPage() {
  const [user, setUser] = useState<{ name: string; email: string } | null>(
    null
  );
  const router = useRouter();

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      router.push("/login");
      return;
    }

    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      setUser({ name: payload.name, email: payload.email });
    } catch (error) {
      console.error("Błąd dekodowania tokenu:", error);
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      router.push("/login");
    }
  }, [router]);

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    router.push("/login");
  };

  if (!user) {
    return (
      <main>
        <h1>Ładowanie...</h1>
      </main>
    );
  }

  return (
    <main>
      <h1>Witaj na Dashboardzie, {user.name}!</h1>
      <p>Jesteś zalogowany jako: {user.email}</p>
      <button onClick={handleLogout}>Wyloguj</button>
    </main>
  );
}
