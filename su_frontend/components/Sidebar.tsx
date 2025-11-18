'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import SchoolRounded from '@/components/icons/SchoolRounded';

import FinanceIcon from '@/components/icons/sidebar/FinanceIcon';
import HomeIcon from '@/components/icons/sidebar/HomeIcon';
import ListIcon from '@/components/icons/sidebar/ListIcon';
import ProfileIcon from '@/components/icons/sidebar/ProfileIcon';
import SettingsIcon from '@/components/icons/sidebar/SettingsIcon';

import CalendarDaysIcon from '@/components/icons/CalendarDaysIcon';
import { useCurrentUser } from '@/lib/hooks/useCurrentUser';
import { LogOut, Sun } from 'lucide-react';
import { useTheme } from '@/lib/contexts/ThemeContext';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { logout } from '@/lib/api/auth';

const Sidebar = () => {
  const pathname = usePathname();
  const { user, isLoading } = useCurrentUser();
  const { toggleTheme } = useTheme();
  const router = useRouter();
  const queryClient = useQueryClient();

  const logoutMutation = useMutation({
    mutationFn: logout,
    onSuccess: () => {
      queryClient.clear();
      router.push('/login');
    },
  });

  const navLinks = [
    { href: '/dashboard', label: 'Strona Główna', icon: HomeIcon },
    { href: '/dashboard/events', label: 'Wydarzenia', icon: CalendarDaysIcon },
  ];

  const classLinks = user?.studentClass
    ? [
        {
          href: `/dashboard/class/${user.studentClass.id}/members`,
          label: 'Członkowie',
          icon: ListIcon,
        },
        {
          href: `/dashboard/class/${user.studentClass.id}/finance`,
          label: 'Finanse',
          icon: FinanceIcon,
        },
      ]
    : [];

  const councilLinks = user?.council
    ? [
        {
          href: `/dashboard/council/${user.council.id}/members`,
          label: 'Członkowie',
          icon: ListIcon,
        },
        {
          href: `/dashboard/council/${user.council.id}/events`,
          label: 'Wydarzenia',
          icon: CalendarDaysIcon,
        },
        {
          href: `/dashboard/council/${user.council.id}/finance`,
          label: 'Finanse',
          icon: FinanceIcon,
        },
      ]
    : [];

  if (isLoading) {
    return (
      <aside className="w-64 flex-shrink-0 bg-secondarybg p-4">
        <div className="h-full animate-pulse rounded-md bg-neutral-700"></div>
      </aside>
    );
  }

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
                  : 'text-foreground hover:bg-neutral-500/20'
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
      <div className="mt-6 flex flex-1 flex-col justify-between">
        <div className="space-y-6">
          <NavGroup links={navLinks} />
          {user?.studentClass && (
            <NavGroup
              title={`KLASA ${user.studentClass.name}`}
              links={classLinks}
            />
          )}
          {user?.council && (
            <NavGroup title={`SAMORZĄD ${user.council.name}`} links={councilLinks} />
          )}
        </div>
        <div>
          <NavGroup
            links={[
              {
                href: '/dashboard/settings',
                label: 'Ustawienia',
                icon: SettingsIcon,
              },
              {
                href: '/dashboard/profile',
                label: 'Twoje Konto',
                icon: ProfileIcon,
              },
            ]}
          />
          <button
            onClick={toggleTheme}
            className="mt-4 flex w-full items-center justify-between rounded-lg px-3 py-2 text-sm font-medium text-foreground hover:bg-neutral-500/20"
          >
            <span>Zmień motyw</span>
            <Sun className="h-5 w-5" />
          </button>
          <button
            onClick={() => logoutMutation.mutate()}
            disabled={logoutMutation.isPending}
            className="mt-2 flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-red-500 hover:bg-red-500/20"
          >
            <LogOut className="h-5 w-5" />
            <span>{logoutMutation.isPending ? 'Wylogowywanie...' : 'Wyloguj'}</span>
          </button>
        </div>
      </div>
    </aside>
  );
};

export default Sidebar;
