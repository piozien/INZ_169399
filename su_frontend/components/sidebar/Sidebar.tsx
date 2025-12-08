'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import SchoolRounded from '@/components/icons/SchoolRounded';
import HomeIcon from '@/components/icons/sidebar/HomeIcon';
import ProfileIcon from '@/components/icons/sidebar/ProfileIcon';
import SettingsIcon from '@/components/icons/sidebar/SettingsIcon';
import CalendarDaysIcon from '@/components/icons/CalendarDaysIcon';
import FinanceIcon from '@/components/icons/sidebar/FinanceIcon';
import ListIcon from '@/components/icons/sidebar/ListIcon';
import { LogOut, Sun, Moon, Landmark, X, Inbox } from 'lucide-react';
import { useTheme } from '@/lib/contexts/ThemeContext';
import { useAuth } from '@/lib/contexts/AuthContext';
import React, { useEffect } from "react";

type NavLink = {
    href: string;
    label: string;
    icon: React.ElementType;
    subLinks?: { href: string; label: string; icon: React.ElementType }[];
};

interface SidebarProps {
    isOpen: boolean;
    onClose: () => void;
}

const Sidebar = ({ isOpen, onClose }: SidebarProps) => {
    const pathname = usePathname();
    const { theme, toggleTheme } = useTheme();
    const { user, isLoading, logout } = useAuth();

    useEffect(() => {
        onClose();
    }, [pathname]);

    const councilIdMatch = pathname.match(/\/dashboard\/council\/([a-f0-9-]+)/);
    const activeCouncilId = councilIdMatch ? councilIdMatch[1] : null;

    const mainLinks: NavLink[] = [
        { href: '/dashboard', label: 'Strona Główna', icon: HomeIcon },
        {
            href: '/dashboard/council',
            label: 'Samorząd',
            icon: Landmark,
            subLinks: activeCouncilId
                ? [
                    { href: `/dashboard/council/${activeCouncilId}/members`, label: 'Członkowie', icon: ListIcon },
                    { href: `/dashboard/council/${activeCouncilId}/events`, label: 'Wydarzenia (SU)', icon: CalendarDaysIcon },
                    { href: `/dashboard/council/${activeCouncilId}/suggestions`, label: 'Sugestie (SU)', icon: Inbox },
                    { href: `/dashboard/council/${activeCouncilId}/finances`, label: 'Finanse', icon: FinanceIcon },
                ]
                : undefined,
        },
        { href: '/dashboard/events', label: 'Wydarzenia', icon: CalendarDaysIcon },
        { href: '/dashboard/suggestions', label: 'Sugestie', icon: Inbox },
    ];

    const userLinks: NavLink[] = [
        { href: '/dashboard/settings', label: 'Ustawienia', icon: SettingsIcon },
        { href: '/dashboard/profile', label: 'Twoje Konto', icon: ProfileIcon },
    ];

    if (isLoading) {
        return (
            <aside className="hidden md:block w-64 flex-shrink-0 bg-secondarybg border-r border-border h-screen sticky top-0">
                <div className="h-full animate-pulse bg-secondarybg"></div>
            </aside>
        );
    }

    if (!user) return null;

    const NavGroup = ({ links, title }: { links: NavLink[]; title?: string }) => (
        <div className="space-y-2">
            {title && (
                <h3 className="px-3 text-xs font-semibold uppercase text-txtcolor-300">
                    {title}
                </h3>
            )}
            <nav className="flex flex-col space-y-1">
                {links.map((link) => {
                    const isActive = pathname === link.href || (link.subLinks && pathname.startsWith(link.href));
                    const Icon = link.icon;

                    return (
                        <div key={link.href}>
                            <Link
                                href={link.href}
                                className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                                    isActive
                                        ? 'bg-primary text-background'
                                        : 'text-foreground hover:bg-secondarybg'
                                }`}
                            >
                                <Icon className="h-5 w-5" />
                                <span>{link.label}</span>
                            </Link>

                            {link.subLinks && (
                                <div className="mt-1 flex flex-col space-y-1 pl-4 border-l-2 border-secondary/20 ml-5">
                                    {link.subLinks.map((subLink) => {
                                        const isSubActive = pathname === subLink.href;
                                        const SubIcon = subLink.icon;
                                        return (
                                            <Link
                                                key={subLink.href}
                                                href={subLink.href}
                                                className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                                                    isSubActive
                                                        ? 'text-secondary bg-secondarybg font-semibold'
                                                        : 'text-txtcolor-300 hover:text-foreground hover:bg-secondarybg/50'
                                                }`}
                                            >
                                                <SubIcon className="h-4 w-4" />
                                                <span>{subLink.label}</span>
                                            </Link>
                                        );
                                    })}
                                </div>
                            )}
                        </div>
                    );
                })}
            </nav>
        </div>
    );

    return (
        <>
            {isOpen && (
                <div
                    className="fixed inset-0 z-40 bg-background/50 backdrop-blur-sm md:hidden animate-in fade-in duration-200"
                    onClick={onClose}
                />
            )}

            <aside
                className={`
                fixed inset-y-0 left-0 z-50 flex h-screen w-64 flex-col border-r border-border bg-background transition-transform duration-300 ease-in-out
                ${isOpen ? 'translate-x-0' : '-translate-x-full'} 
                
                md:sticky md:top-0 md:h-screen md:translate-x-0 md:border-r md:border-border md:z-auto
                `}
            >
                <div className="flex items-center justify-between p-4 mb-2 shrink-0">
                    <div className="flex items-center gap-3">
                        <SchoolRounded className="h-8 w-8 text-secondary" />
                        <span className="text-lg font-bold">SAMORZĄD</span>
                    </div>
                    <button onClick={onClose} className="md:hidden text-txtcolor-300 hover:text-foreground">
                        <X className="h-6 w-6" />
                    </button>
                </div>

                <div className="px-4 py-2 text-xs text-txtcolor-300 shrink-0">
                    Zalogowany: <span className="font-medium text-foreground">{user.fullName}</span>
                </div>

                <div className="mt-2 flex flex-1 flex-col justify-between overflow-y-auto [scrollbar-width:none] [-ms-overflow-style:none] [&::-webkit-scrollbar]:hidden">
                    <div className="space-y-6 px-4">
                        <NavGroup links={mainLinks} />
                    </div>
                    <div className="pt-4 mt-4 border-t border-border shrink-0 p-4">
                        <NavGroup links={userLinks} />

                        <button
                            onClick={toggleTheme}
                            className="mt-2 flex w-full items-center justify-between rounded-lg px-3 py-2 text-sm font-medium text-foreground hover:bg-secondarybg transition-colors"
                        >
                            <span className="flex items-center gap-3">
                                {theme === 'dark' ? (
                                    <Sun className="h-5 w-5" />
                                ) : (
                                    <Moon className="h-5 w-5" />
                                )}
                                <span>{theme === 'dark' ? 'Jasny motyw' : 'Ciemny motyw'}</span>
                            </span>
                        </button>

                        <button
                            onClick={() => logout()}
                            className="mt-1 flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-error hover:bg-error/50 transition-colors"
                        >
                            <LogOut className="h-5 w-5" />
                            <span>Wyloguj</span>
                        </button>
                    </div>
                </div>
            </aside>
        </>
    );
};

export default Sidebar;