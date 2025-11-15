"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { useCurrentUser } from "@/lib/hooks/useCurrentUser";
import { logout } from "@/lib/api/auth";
import { useTheme } from "@/lib/contexts/ThemeContext";
import { Sun, Moon } from "lucide-react";

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
  const [mounted, setMounted] = useState(false);
  const router = useRouter();
  const queryClient = useQueryClient();
  const { data: user, isLoading } = useCurrentUser();
  const { theme, toggleTheme, mounted: themeMounted } = useTheme();

  useEffect(() => {
    setMounted(true);
  }, []);

  const handleLogout = async () => {
    await logout();
    queryClient.invalidateQueries({ queryKey: ["currentUser"] });
    router.push("/login");
  };

  const isAuthenticated = mounted && !isLoading && user != null;
  const navigation = isAuthenticated ? protectedNavigation : publicNavigation;

  return (
    <header>
      <nav aria-label="Główne">
        <ul>
          {navigation.map((item) => (
            <li key={item.href}>
              <Link href={item.href}>{item.label}</Link>
            </li>
          ))}
          {themeMounted && (
            <li>
              <button
                type="button"
                onClick={toggleTheme}
                className="p-2 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-700 transition-colors"
                aria-label="Przełącz motyw"
              >
                {theme === "dark" ? (
                  <Sun className="w-5 h-5" />
                ) : (
                  <Moon className="w-5 h-5" />
                )}
              </button>
            </li>
          )}
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

