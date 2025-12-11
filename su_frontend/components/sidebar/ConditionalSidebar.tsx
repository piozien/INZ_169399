'use client';

import { usePathname } from 'next/navigation';
import { useState } from 'react';
import Sidebar from './Sidebar';
import PublicSidebar from './PublicSidebar';
import { Menu } from 'lucide-react';

const MobileMenuButton = ({ onClick }: { onClick: () => void }) => (
    <button
        onClick={onClick}
        className="bg-secondarybg border-border text-foreground hover:bg-inputbg fixed top-4 left-4 z-40 rounded-lg border p-2 shadow-lg transition-colors md:hidden"
        aria-label="Otwórz menu"
    >
        <Menu className="h-6 w-6" />
    </button>
);

const ConditionalSidebar = () => {
    const pathname = usePathname();
    const [isMobileOpen, setIsMobileOpen] = useState(false);

    const isDashboard = pathname?.startsWith('/dashboard');
    const isPublic = ['/upcoming'].includes(pathname);

    if (isDashboard) {
        return (
            <>
                <MobileMenuButton onClick={() => setIsMobileOpen(true)} />
                <Sidebar isOpen={isMobileOpen} onClose={() => setIsMobileOpen(false)} />
            </>
        );
    }

    if (isPublic) {
        return (
            <>
                <MobileMenuButton onClick={() => setIsMobileOpen(true)} />
                <PublicSidebar isOpen={isMobileOpen} onClose={() => setIsMobileOpen(false)} />
            </>
        );
    }

    return null;
};

export default ConditionalSidebar;
