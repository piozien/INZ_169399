'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useQueryClient } from '@tanstack/react-query';

export default function OAuth2SuccessPage() {
  const router = useRouter();
  const queryClient = useQueryClient();

  useEffect(() => {
    queryClient.invalidateQueries({ queryKey: ['currentUser'] });
    router.push('/dashboard');
  }, [router, queryClient]);

  return (
    <div className="min-h-screen flex items-center justify-center">
      <p>Przekierowywanie...</p>
    </div>
  );
}
