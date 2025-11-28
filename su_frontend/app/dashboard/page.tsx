'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/contexts/AuthContext';

export default function DashboardPage() {
  const router = useRouter();
  const { user, isLoading, logout } = useAuth();

  useEffect(() => {
    if (!isLoading && !user) {
      router.replace('/login');
    }
  }, [user, isLoading, router]);

  if (isLoading) {
    return (
      <main className="p-8 bg-background flex items-center justify-center h-screen">
        <h1 className="text-xl">Ładowanie panelu...</h1>
      </main>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <main className="p-8 bg-background">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold mb-4">Witaj, {user.fullName}</h1>
          {/* Tymczasowa zawartosc strony*/}
        <div className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm border border-border">
          <p className="text-lg mb-2">Jesteś zalogowany jako:</p>
          <p className="font-mono bg-gray-100 dark:bg-gray-900 p-2 rounded mb-6">
            {user.email}
          </p>

          <div className="flex gap-4">
            <button
              type="button"
              onClick={() => logout()}
              className="bg-red-600 text-white px-6 py-2 rounded hover:bg-red-700 transition-colors"
            >
              Wyloguj się
            </button>
          </div>
        </div>
      </div>
    </main>
  );
}
