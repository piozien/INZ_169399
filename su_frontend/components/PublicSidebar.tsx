'use client';

import Link from 'next/link';
import { useTheme } from '@/lib/contexts/ThemeContext';
import SchoolRounded from '@/components/icons/SchoolRounded';
import HomeIcon from '@/components/icons/sidebar/HomeIcon';
import { Sun } from 'lucide-react';

const PublicSidebar = () => {
  const { toggleTheme } = useTheme();

  return (
    <aside className="flex h-screen w-64 flex-col border-r border-border bg-background p-4">
      <div className="flex items-center gap-3 p-3">
        <SchoolRounded className="h-8 w-8 text-secondary" />
        <span className="text-lg font-bold">SAMORZĄD</span>
      </div>
      <div className="mt-6 flex flex-1 flex-col justify-between">
        <nav className="flex flex-col space-y-1">
          <Link
            href="/"
            className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors text-foreground hover:secondarybg`}
          >
            <HomeIcon className="h-5 w-5" />
            <span>Strona Główna</span>
          </Link>
        </nav>
        <div>
          <button
            onClick={toggleTheme}
            className="mt-4 flex w-full items-center justify-between rounded-lg px-3 py-2 text-sm font-medium text-foreground hover:secondarybg"
          >
            <span>Zmień motyw</span>
            <Sun className="h-5 w-5" />
          </button>
        </div>
      </div>
    </aside>
  );
};

export default PublicSidebar;
