'use client';

import Link from 'next/link';
import SchoolRounded from '@/components/icons/SchoolRounded';
import PencilIcon from '@/components/icons/PencilIcon';
import UserPlusIcon from '@/components/icons/UserPlusIcon';
import CalendarDaysIcon from '@/components/icons/CalendarDaysIcon';

export default function HomePage() {
  return (
    <main className="min-h-screen flex flex-col items-center justify-center text-center p-4">
      <div className="flex flex-col items-center">
        <div className="flex items-center gap-3 text-[23.8px] font-bold text-foreground">
          <SchoolRounded />
          <span>SAMORZĄD</span>
        </div>
        <h1 className="text-5xl font-bold mt-4">Witaj!</h1>
        <p className="mt-4 max-w-md text-neutral-300">
          Aby rozpocząć swoją przygodę z aplikacją, musisz się zalogować. Co
          robimy?
        </p>
      </div>

      <div className="mt-12 w-full max-w-md">
        <div className="grid grid-cols-2 gap-4">
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
            href="/events"
            className="bg-secondarybg p-6 rounded-lg flex flex-col items-center justify-center gap-2 transition-all duration-300 ease-in-out hover:ring-2 hover:ring-secondary focus:outline-none focus:ring-2 focus:ring-secondary"
          >
            <CalendarDaysIcon />
            <span>Kalendarz wydarzeń</span>
          </Link>
        </div>
      </div>
    </main>
  );
}
