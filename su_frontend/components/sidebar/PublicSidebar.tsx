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

const NavItem = ({
    href,
    label,
    icon: Icon,
    currentPath,
}: {
    href: string;
    label: string;
    icon: any;
    currentPath: string;
}) => {
    const isActive = currentPath === href;
    return (
        <Link
            href={href}
            className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                isActive ? 'bg-primary text-background' : 'text-foreground hover:bg-secondarybg'
            }`}
        >
            <Icon className="h-5 w-5" />
            <span>{label}</span>
        </Link>
    );
};

export default function PublicSidebar({ isOpen, onClose }: SidebarProps) {
    const pathname = usePathname();
    const { toggleTheme } = useTheme();

    const publicLinks = [
        { href: '/', label: 'Strona Główna', icon: Home },
        { href: '/upcoming', label: 'Wydarzenia', icon: CalendarDaysIcon },
    ];

    return (
        <>
            {isOpen && (
                <div
                    className="bg-background/50 animate-in fade-in fixed inset-0 z-40 backdrop-blur-sm duration-200 md:hidden"
                    onClick={onClose}
                />
            )}

            <aside
                className={`border-border bg-background fixed inset-y-0 left-0 z-50 flex h-screen w-64 flex-col border-r p-4 transition-transform duration-300 ease-in-out ${isOpen ? 'translate-x-0' : '-translate-x-full'} md:border-border md:sticky md:top-0 md:z-auto md:h-screen md:translate-x-0 md:border-r`}
            >
                <div className="mb-2 flex shrink-0 items-center justify-between p-3">
                    <div className="flex items-center gap-3">
                        <SchoolRounded className="text-secondary h-8 w-8" />
                        <span className="text-lg font-bold">SAMORZĄD</span>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-txtcolor-300 hover:text-foreground md:hidden"
                    >
                        <X className="h-6 w-6" />
                    </button>
                </div>

                <div className="text-txtcolor-300 shrink-0 px-3 py-2 text-xs">Witaj!</div>

                <div className="mt-6 flex flex-1 flex-col justify-between">
                    <div className="space-y-1">
                        {publicLinks.map((link) => (
                            <NavItem key={link.href} {...link} currentPath={pathname} />
                        ))}
                    </div>

                    <div className="border-border -mx-4 space-y-2 border-t px-4 pt-4 pb-2">
                        <button
                            onClick={toggleTheme}
                            className="text-foreground hover:bg-secondarybg flex w-full items-center justify-between rounded-lg px-3 py-2 text-sm font-medium transition-colors"
                        >
                            <span className="flex items-center gap-3">
                                <Sun className="h-5 w-5" />
                                Zmień motyw
                            </span>
                        </button>

                        <Link
                            href="/login"
                            className="text-foreground hover:bg-secondarybg flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors"
                        >
                            <LogIn className="h-5 w-5" />
                            <span>Zaloguj się</span>
                        </Link>

                        <Link
                            href="/register"
                            className="text-foreground hover:bg-secondarybg flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors"
                        >
                            <UserPlus className="h-5 w-5" />
                            <span>Zarejestruj się</span>
                        </Link>
                    </div>
                </div>
            </aside>
        </>
    );
}
