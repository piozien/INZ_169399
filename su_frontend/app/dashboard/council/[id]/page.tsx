'use client';

import { use } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchCouncilById } from '@/lib/api/council';
import { CouncilResponseDto } from '@/types/council.types';
import {
  Loader2,
  Users,
  CalendarDays,
  PartyPopper,
  PiggyBank,
} from 'lucide-react';
import CouncilHeader from '@/components/council/dashboard/CouncilHeader';
import JoinCodeCard from '@/components/council/dashboard/JoinCodeCard';
import StatCard from '@/components/council/dashboard/StatCard';
import QuickActionCard from '@/components/council/dashboard/QuickActionCard';

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
    retry: 1,
  });

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-[50vh]">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (error || !council) {
    return (
      <div className="flex flex-col items-center justify-center h-[50vh] text-txtcolor-300">
        <h2 className="text-2xl font-bold mb-2">Nie znaleziono samorządu</h2>
        <p>Upewnij się, że masz odpowiednie uprawnienia.</p>
      </div>
    );
  }

  return (
    <div className="p-6 space-y-8 max-w-7xl mx-auto">
      <CouncilHeader
        name={council.name}
        academicYear={council.academicYear}
        isActive={council.isActive}
      />
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <JoinCodeCard joinCode={council.joinCode} />

        <StatCard
          icon={Users}
          iconColor="text-info"
          title="Członkowie"
          value={council.members?.length || 0}
          unit="osób"
          linkHref={`/dashboard/council/${id}/members`}
          linkLabel="Zarządzaj członkami"
        />

        <StatCard
          icon={CalendarDays}
          iconColor="text-warning"
          title="Kadencja"
          customContent={
            <div className="space-y-3 mt-4">
              <div className="flex justify-between items-center">
                <span className="text-txtcolor-300 text-sm">Start:</span>
                <span className="font-medium">
                  {new Date(council.startDate).toLocaleDateString('pl-PL')}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-txtcolor-300 text-sm">Koniec:</span>
                <span className="font-medium">
                  {new Date(council.endDate).toLocaleDateString('pl-PL')}
                </span>
              </div>
            </div>
          }
        />
      </div>
      <div>
        <h2 className="text-xl font-semibold mb-4 text-foreground">
          Szybkie akcje
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <QuickActionCard
            href={`/dashboard/council/${id}/events`}
            icon={PartyPopper}
            title="Wydarzenia"
            description="Planuj apele, dyskoteki i zbiórki."
            bgColorClass="bg-accent/10"
            iconColorClass="text-accent"
          />

          <QuickActionCard
            href={`/dashboard/council/${id}/finances`}
            icon={PiggyBank}
            title="Budżet i Finanse"
            description="Zarządzaj wydatkami i zbiórkami."
            bgColorClass="bg-success/10"
            iconColorClass="text-success"
          />
        </div>
      </div>
    </div>
  );
}
