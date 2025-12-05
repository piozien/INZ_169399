'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import SchoolRounded from '@/components/icons/SchoolRounded';
import CalendarDaysIcon from '@/components/icons/CalendarDaysIcon';
import { LogIn, UserPlus, Sun, Home, X } from 'lucide-react';
import { useTheme } from '@/lib/contexts/ThemeContext';

interface SidebarProps {
    isOpen: boolean;
    onClose: () => void;
}

export default function PublicSidebar({ isOpen, onClose }: SidebarProps) {
    const pathname = usePathname();
    const { toggleTheme } = useTheme();

    const publicLinks = [
        { href: '/', label: 'Strona Główna', icon: Home },
        { href: '/upcoming', label: 'Wydarzenia', icon: CalendarDaysIcon },
    ];

    const NavItem = ({ href, label, icon: Icon }: { href: string; label: string; icon: any }) => {
        const isActive = pathname === href;
        return (
            <Link
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
    };

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
                fixed inset-y-0 left-0 z-50 flex h-screen w-64 flex-col border-r border-border bg-background p-4 transition-transform duration-300 ease-in-out
                ${isOpen ? 'translate-x-0' : '-translate-x-full'} 
                
                md:sticky md:top-0 md:h-screen md:translate-x-0 md:border-r md:border-border md:z-auto
                `}
            >
                <div className="flex items-center justify-between p-3 mb-2 shrink-0">
                    <div className="flex items-center gap-3">
                        <SchoolRounded className="h-8 w-8 text-secondary" />
                        <span className="text-lg font-bold">SAMORZĄD</span>
                    </div>
                    <button onClick={onClose} className="md:hidden text-txtcolor-300 hover:text-foreground">
                        <X className="h-6 w-6" />
                    </button>
                </div>

                <div className="px-3 py-2 text-xs text-txtcolor-300 shrink-0">
                    Witaj!
                </div>

                <div className="mt-6 flex flex-1 flex-col justify-between">

                    <div className="space-y-1">
                        {publicLinks.map((link) => (
                            <NavItem key={link.href} {...link} />
                        ))}
                    </div>

                    <div className="space-y-2 pt-4 border-t border-border -mx-4 px-4 pb-2">

                        <button
                            onClick={toggleTheme}
                            className="flex w-full items-center justify-between rounded-lg px-3 py-2 text-sm font-medium text-foreground hover:bg-secondarybg transition-colors"
                        >
                            <span className="flex items-center gap-3">
                                <Sun className="h-5 w-5" />
                                Zmień motyw
                            </span>
                        </button>

                        <Link
                            href="/login"
                            className="flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-foreground hover:bg-secondarybg transition-colors"
                        >
                            <LogIn className="h-5 w-5" />
                            <span>Zaloguj się</span>
                        </Link>

                        <Link
                            href="/register"
                            className="flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-foreground hover:bg-secondarybg transition-colors"
                        >
                            <UserPlus className="h-5 w-5" />
                            <span>Dołącz</span>
                        </Link>
                    </div>
                </div>
            </aside>
        </>
    );
}