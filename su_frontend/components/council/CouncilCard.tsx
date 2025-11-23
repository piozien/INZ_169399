'use client';

import { useRouter } from 'next/navigation';
import { CouncilResponseDto } from '@/types/council.types';
import { Calendar, Users, Key, GraduationCap } from 'lucide-react';
import { format } from 'date-fns';
import { pl } from 'date-fns/locale';

interface CouncilCardProps {
  council: CouncilResponseDto;
  isActive?: boolean;
}

export default function CouncilCard({ council, isActive = false }: CouncilCardProps) {
  const router = useRouter();

  const formatDate = (dateString: string) => {
    return format(new Date(dateString), 'd MMMM yyyy', { locale: pl });
  };

  const handleClick = () => {
    router.push(`/dashboard/council/${council.id}`);
  };

  return (
    <div
      onClick={handleClick}
      className="bg-secondarybg rounded-lg p-6 flex flex-col h-full shadow-lg hover:ring-2 hover:ring-secondary transition-all duration-300 ease-in-out cursor-pointer"
    >
      <div className="flex items-start justify-between mb-2">
        <h2 className="text-2xl font-bold text-primary">{council.name}</h2>
        
        {isActive && (
          <span className="inline-block bg-success-bg text-success px-3 py-1 rounded-full text-xs font-semibold">
            Aktywny
          </span>
        )}
        
        {!isActive && (
          <span className="inline-block bg-neutral-status-bg text-neutral-status px-3 py-1 rounded-full text-xs font-semibold">
            Nieaktywny
          </span>
        )}
      </div>

      <div className="mt-auto pt-4 border-t border-border space-y-3">
        
        <div className="flex items-center gap-2">
          <Calendar size={18} className="text-secondary" />
          <span className="text-sm text-txtcolor-300">
            {formatDate(council.startDate)} - {formatDate(council.endDate)}
          </span>
        </div>

        <div className="flex items-center gap-2">
          <GraduationCap size={18} className="text-secondary" />
          <span className="text-sm text-txtcolor-300">
            Rok akademicki: {council.academicYear}
          </span>
        </div>

        <div className="flex items-center gap-2">
          <Users size={18} className="text-secondary" />
          <span className="text-sm text-txtcolor-300">
            {council.members?.length || 0} {council.members?.length === 1 ? 'członek' : 'członków'}
          </span>
        </div>

        <div className="flex items-center gap-2">
          <Key size={18} className="text-secondary" />
          <span className="text-sm text-txtcolor-300 font-mono">
            {council.joinCode}
          </span>
        </div>
      </div>
    </div>
  );
}