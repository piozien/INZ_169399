"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

interface UserInfo {
  name: string;
  email: string;
}

export default function DashboardPage() {
  const [user, setUser] = useState<UserInfo | null>(null);
  const router = useRouter();

  useEffect(() => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      router.replace("/login");
      return;
    }

    try {
      const payload = JSON.parse(atob(token.split(".")[1])) as UserInfo;
      setUser({ name: payload.name, email: payload.email });
    } catch (error) {
      console.error("Failed to decode token", error);
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      router.replace("/login");
    }
  }, [router]);

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    router.replace("/login");
  };

  if (!user) {
    return (
      <main>
        <h1>Ładowanie panelu…</h1>
      </main>
    );
  }

  return (
    <main>
      <h1>Witaj, {user.name}</h1>
      <p>Jesteś zalogowany jako {user.email}.</p>
      <button type="button" onClick={handleLogout}>
        Wyloguj
      </button>
    </main>
  );
}
