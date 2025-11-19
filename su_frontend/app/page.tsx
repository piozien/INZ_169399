'use client';

import SchoolRounded from '@/components/icons/SchoolRounded';
import PencilIcon from '@/components/icons/PencilIcon';
import UserPlusIcon from '@/components/icons/UserPlusIcon';
import CalendarDaysIcon from '@/components/icons/CalendarDaysIcon';
import Link from 'next/link';

export default function Home() {
  return (
    <main className="min-h-screen flex flex-col items-center justify-center text-center p-4">
      <div className="flex flex-col items-center">
        <div className="flex items-center gap-3 text-[23.8px] font-bold text-foreground">
          <SchoolRounded />
          <span>SAMORZĄD</span>
        </div>
        <h1 className="text-4xl font-bold">
          Witaj w aplikacji Samorządu Uczniowskiego!
        </h1>
        <p className="mt-4 max-w-md text-txtcolor-300">
          Zaloguj się lub zarejestruj, aby uzyskać dostęp do wszystkich funkcji.
        </p>
      </div>

      <div className="mt-8 flex justify-center gap-8">
        <Link
          href="/login"
          className="bg-secondarybg p-6 rounded-lg flex flex-col items-center justify-center gap-2 transition-all duration-300 ease-in-out hover:ring-2 hover:ring-secondary focus:outline-none focus:ring-2 focus:ring-secondary"
        >
          <PencilIcon />
          <span>Logowanie</span>
        </Link>
        <Link
          href="/register"
          className="bg-secondarybg p-6 rounded-lg flex flex-col items-center justify-center gap-2 transition-all duration-300 ease-in-out hover:ring-2 hover:ring-secondary focus:outline-none focus:ring-2 focus:ring-secondary"
        >
          <UserPlusIcon />
          <span>Rejestracja</span>
        </Link>
      </div>
      <div className="mt-4">
        <Link
          href="/upcoming"
          className="bg-secondarybg p-6 rounded-lg flex flex-col items-center justify-center gap-2 transition-all duration-300 ease-in-out hover:ring-2 hover:ring-secondary focus:outline-none focus:ring-2 focus:ring-secondary"
        >
          <CalendarDaysIcon />
          <span>Kalendarz wydarzeń</span>
        </Link>
      </div>
    </main>
  );
}
