"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

const publicNavigation = [
  { href: "/", label: "Strona główna" },
  { href: "/events", label: "Wydarzenia" },
  { href: "/suggestions", label: "Propozycje" },
  { href: "/login", label: "Logowanie" },
  { href: "/register", label: "Rejestracja" },
];

const protectedNavigation = [
  { href: "/", label: "Strona główna" },
  { href: "/events", label: "Wydarzenia" },
  { href: "/suggestions", label: "Propozycje" },
  { href: "/finances", label: "Finanse" },
  { href: "/dashboard", label: "Panel" },
];

export default function Navbar() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [mounted, setMounted] = useState(false);
  const router = useRouter();

  useEffect(() => {
    setMounted(true);
    const token = localStorage.getItem("accessToken");
    setIsAuthenticated(!!token);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    setIsAuthenticated(false);
    router.push("/login");
  };

  const navigation = mounted && isAuthenticated ? protectedNavigation : publicNavigation;

  return (
    <header>
      <nav aria-label="Główne">
        <ul>
          {navigation.map((item) => (
            <li key={item.href}>
              <Link href={item.href}>{item.label}</Link>
            </li>
          ))}
          {mounted && isAuthenticated && (
            <li>
              <button type="button" onClick={handleLogout}>
                Wyloguj
              </button>
            </li>
          )}
        </ul>
      </nav>
    </header>
  );
}

