'use client';

import SchoolRounded from '@/components/icons/SchoolRounded';
import PencilIcon from '@/components/icons/PencilIcon';
import UserPlusIcon from '@/components/icons/UserPlusIcon';
import CalendarDaysIcon from '@/components/icons/CalendarDaysIcon';
import Link from 'next/link';

export default function Home() {
    return (
        <main className="flex min-h-screen flex-col items-center justify-center p-4 text-center">
            <div className="flex flex-col items-center">
                <div className="text-foreground flex items-center gap-3 text-[23.8px] font-bold">
                    <SchoolRounded />
                    <span>SAMORZĄD</span>
                </div>
                <h1 className="text-4xl font-bold">Witaj w aplikacji Samorządu Uczniowskiego!</h1>
                <p className="text-txtcolor-300 mt-4 max-w-md">
                    Zaloguj się lub zarejestruj, aby uzyskać dostęp do wszystkich funkcji.
                </p>
            </div>

            <div className="mt-8 flex justify-center gap-8">
                <Link
                    href="/login"
                    className="bg-secondarybg hover:ring-secondary focus:ring-secondary flex flex-col items-center justify-center gap-2 rounded-lg p-6 transition-all duration-300 ease-in-out hover:ring-2 focus:ring-2 focus:outline-none"
                >
                    <PencilIcon />
                    <span>Logowanie</span>
                </Link>
                <Link
                    href="/register"
                    className="bg-secondarybg hover:ring-secondary focus:ring-secondary flex flex-col items-center justify-center gap-2 rounded-lg p-6 transition-all duration-300 ease-in-out hover:ring-2 focus:ring-2 focus:outline-none"
                >
                    <UserPlusIcon />
                    <span>Rejestracja</span>
                </Link>
            </div>
            <div className="mt-4">
                <Link
                    href="/upcoming"
                    className="bg-secondarybg hover:ring-secondary focus:ring-secondary flex flex-col items-center justify-center gap-2 rounded-lg p-6 transition-all duration-300 ease-in-out hover:ring-2 focus:ring-2 focus:outline-none"
                >
                    <CalendarDaysIcon />
                    <span>Kalendarz wydarzeń</span>
                </Link>
            </div>
        </main>
    );
}
