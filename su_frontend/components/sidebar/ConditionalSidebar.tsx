'use client';

import { usePathname } from 'next/navigation';
import { useState } from 'react';
import Sidebar from './Sidebar';
import PublicSidebar from './PublicSidebar';
import { Menu } from 'lucide-react';

const ConditionalSidebar = () => {
    const pathname = usePathname();
    const [isMobileOpen, setIsMobileOpen] = useState(false);

    const MobileMenuButton = () => (
        <button
            onClick={() => setIsMobileOpen(true)}
            className="fixed top-4 left-4 z-40 p-2 bg-secondarybg border border-border rounded-lg text-foreground md:hidden hover:bg-inputbg transition-colors shadow-lg"
            aria-label="Otwórz menu"
        >
            <Menu className="h-6 w-6" />
        </button>
    );

    if (pathname.startsWith('/dashboard')) {
        return (
            <>
                <MobileMenuButton />
                <Sidebar
                    isOpen={isMobileOpen}
                    onClose={() => setIsMobileOpen(false)}
                />
            </>
        );
    }

    const publicSidebarRoutes = ['/upcoming'];

    if (publicSidebarRoutes.includes(pathname)) {
        return (
            <>
                <MobileMenuButton />
                <PublicSidebar
                    isOpen={isMobileOpen}
                    onClose={() => setIsMobileOpen(false)}
                />
            </>
        );
    }

    return null;
};

export default ConditionalSidebar;