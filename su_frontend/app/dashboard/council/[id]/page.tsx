'use client';

import { use } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchCouncilById } from '@/lib/api/council';
import { CouncilResponseDto } from '@/types/council.types';

export default function CouncilDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const {
    data: council,
    isLoading,
    error,
  } = useQuery<CouncilResponseDto>({
    queryKey: ['council', id],
    queryFn: () => fetchCouncilById(id),
  });

  if (isLoading) {
    return <div>Ładowanie...</div>;
  }

  if (error || !council) {
    return <div>Nie znaleziono samorządu</div>;
  }

  return (
    <div>
      <h1>{council.name}</h1>
      <p>Rok akademicki: {council.academicYear}</p>
      <p>
        Okres: {new Date(council.startDate).toLocaleDateString('pl-PL')} -{' '}
        {new Date(council.endDate).toLocaleDateString('pl-PL')}
      </p>
      <p>Status: {council.isActive ? 'Aktywny' : 'Nieaktywny'}</p>
      <p>Kod dołączenia: {council.joinCode}</p>
      <p>Liczba członków: {council.members?.length || 0}</p>
    </div>
  );
}

