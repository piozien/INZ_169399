'use client';

import { usePathname } from 'next/navigation';
import Sidebar from './Sidebar';
import PublicSidebar from './PublicSidebar';

const NO_SIDEBAR_PATHS = ['/', '/login', '/register', '/forgot-password', '/reset-password',];
const PUBLIC_SIDEBAR_PATHS = ['/upcoming'];

const ConditionalSidebar = () => {
  const pathname = usePathname();

  if (NO_SIDEBAR_PATHS.includes(pathname)) {
    return null;
  }

  if (PUBLIC_SIDEBAR_PATHS.includes(pathname)) {
    return <PublicSidebar />;
  }

  return <Sidebar />;
};

export default ConditionalSidebar;
