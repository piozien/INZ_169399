'use client';

import Link from 'next/link';
import { CouncilResponseDto } from '@/types/council.types';
import { Calendar, Users, Key, GraduationCap, ShieldCheck } from 'lucide-react';
import { format } from 'date-fns';
import { pl } from 'date-fns/locale';

interface CouncilCardProps {
    council: CouncilResponseDto;
    isActive?: boolean;
}

const formatDate = (dateString: string) => {
    return format(new Date(dateString), 'd MMMM yyyy', { locale: pl });
};

export default function CouncilCard({ council, isActive = false }: CouncilCardProps) {
    return (
        <Link href={`/dashboard/council/${council.id}`} className="block h-full">
            <div className="bg-secondarybg hover:ring-secondary group flex h-full cursor-pointer flex-col rounded-lg p-6 shadow-lg transition-all duration-300 ease-in-out hover:ring-2">
                <div className="mb-2 flex items-start justify-between">
                    <h2 className="text-primary group-hover:text-secondary mr-4 text-2xl font-bold transition-colors">
                        {council.name}
                    </h2>

                    <div className="flex shrink-0 flex-col items-end gap-2">
                        {council.defaultCouncil && (
                            <span className="bg-secondary/10 text-secondary border-secondary/20 inline-flex items-center gap-1 rounded-full border px-3 py-1 text-xs font-semibold">
                                <ShieldCheck size={12} /> Domyślny
                            </span>
                        )}

                        <span
                            className={`inline-block rounded-full px-3 py-1 text-xs font-semibold ${
                                isActive
                                    ? 'bg-success/10 text-success'
                                    : 'bg-txtcolor-300/10 text-txtcolor-300'
                            }`}
                        >
                            {isActive ? 'Aktywny' : 'Nieaktywny'}
                        </span>
                    </div>
                </div>

                <div className="border-border mt-auto space-y-3 border-t pt-4">
                    <div className="flex items-center gap-2">
                        <Calendar size={18} className="text-secondary" />
                        <span className="text-txtcolor-300 text-sm">
                            {formatDate(council.startDate)} - {formatDate(council.endDate)}
                        </span>
                    </div>

                    <div className="flex items-center gap-2">
                        <GraduationCap size={18} className="text-secondary" />
                        <span className="text-txtcolor-300 text-sm">
                            Rok szkolny: {council.academicYear}
                        </span>
                    </div>

                    <div className="flex items-center gap-2">
                        <Users size={18} className="text-secondary" />
                        <span className="text-txtcolor-300 text-sm">
                            {council.members?.length || 0}{' '}
                            {council.members?.length === 1 ? 'członek' : 'członków'}
                        </span>
                    </div>

                    <div className="flex items-center gap-2">
                        <Key size={18} className="text-secondary" />
                        <span className="text-txtcolor-300 font-mono text-sm tracking-wider">
                            {council.joinCode}
                        </span>
                    </div>
                </div>
            </div>
        </Link>
    );
}
