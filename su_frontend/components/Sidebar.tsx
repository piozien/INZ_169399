'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import SchoolRounded from '@/components/icons/SchoolRounded';
import HomeIcon from '@/components/icons/sidebar/HomeIcon';
import ProfileIcon from '@/components/icons/sidebar/ProfileIcon';
import SettingsIcon from '@/components/icons/sidebar/SettingsIcon';
import CalendarDaysIcon from '@/components/icons/CalendarDaysIcon';
import { LogOut, Sun, Landmark } from 'lucide-react';
import { useTheme } from '@/lib/contexts/ThemeContext';
import { useAuth } from '@/lib/contexts/AuthContext';

const Sidebar = () => {
  const pathname = usePathname();
  const { toggleTheme } = useTheme();

  const { user, isLoading, logout } = useAuth();

  const mainLinks = [
    { href: '/dashboard', label: 'Strona Główna', icon: HomeIcon },
    { href: '/dashboard/events', label: 'Wydarzenia', icon: CalendarDaysIcon },
    { href: '/dashboard/council', label: 'Samorząd', icon: Landmark },
  ];

  const userLinks = [
    { href: '/dashboard/settings', label: 'Ustawienia', icon: SettingsIcon },
    { href: '/dashboard/profile', label: 'Twoje Konto', icon: ProfileIcon },
  ];

  if (isLoading) {
    return (
      <aside className="w-64 flex-shrink-0 bg-secondarybg p-4">
        <div className="h-full animate-pulse rounded-md bg-secondarybg"></div>
      </aside>
    );
  }

  if (!user) return null;

  const NavGroup = ({
    title,
    links,
  }: {
    title?: string;
    links: { href: string; label: string; icon: React.ElementType }[];
  }) => (
    <div className="space-y-2">
      {title && (
        <h3 className="px-3 text-xs font-semibold uppercase text-txtcolor-300">
          {title}
        </h3>
      )}
      <nav className="flex flex-col space-y-1">
        {links.map(({ href, label, icon: Icon }) => {
          const isActive = pathname === href;
          return (
            <Link
              key={href}
              href={href}
              className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                isActive
                  ? 'bg-primary text-background'
                  : 'text-foreground hover:bg-secondarybg'
              }`}
            >
              <Icon className="h-5 w-5" />
              <span>{label}</span>
            </Link>
          );
        })}
      </nav>
    </div>
  );

  return (
    <aside className="flex h-screen w-64 flex-col border-r border-border bg-background p-4">
      <div className="flex items-center gap-3 p-3">
        <SchoolRounded className="h-8 w-8 text-secondary" />
        <span className="text-lg font-bold">SAMORZĄD</span>
      </div>

      <div className="px-3 py-2 text-xs text-lg">
        Zalogowany: {user.fullName}
      </div>

      <div className="mt-2 flex flex-1 flex-col justify-between">
        <div className="space-y-6">
          <NavGroup links={mainLinks} />
        </div>
        <div>
          <NavGroup links={userLinks} />
          <button
            onClick={toggleTheme}
            className="mt-4 flex w-full items-center justify-between rounded-lg px-3 py-2 text-sm font-medium text-foreground hover:bg-secondarybg"
          >
            <span>Zmień motyw</span>
            <Sun className="h-5 w-5" />
          </button>
          <button
            onClick={() => logout()}
            className=" flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-foreground transition-colors hover:bg-secondarybg"
          >
            <LogOut className="h-5 w-5" />
            <span>Wyloguj</span>
          </button>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
