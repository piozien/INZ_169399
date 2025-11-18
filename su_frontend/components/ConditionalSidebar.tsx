'use client';

import { usePathname } from 'next/navigation';
import Sidebar from './Sidebar';

const HIDDEN_PATHS = ['/login', '/register', '/forgot-password', '/reset-password', '/'];

const ConditionalSidebar = () => {
  const pathname = usePathname();

  if (HIDDEN_PATHS.includes(pathname)) {
    return null;
  }

  return <Sidebar />;
};

export default ConditionalSidebar;
