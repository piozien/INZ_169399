import SchoolRounded from '@/components/icons/SchoolRounded';
import { CalendarDays } from 'lucide-react';

interface Props {
    name: string;
    academicYear: string;
    isActive: boolean;
}

export default function CouncilHeader({ name, academicYear, isActive }: Props) {
    return (
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 border-b border-border pb-6">
            <div className="flex items-center gap-4">
                <div className="p-4 bg-secondarybg rounded-2xl border border-border">
                    <SchoolRounded className="h-10 w-10 text-secondary" />
                </div>
                <div>
                    <div className="flex items-center gap-3">
                        <h1 className="text-3xl font-bold text-foreground">{name}</h1>
                        <span
                            className={`px-3 py-1 rounded-full text-xs font-medium border ${
                                isActive
                                    ? 'bg-green-500/10 text-green-500 border-green-500/20'
                                    : 'bg-red-500/10 text-red-500 border-red-500/20'
                            }`}
                        >
              {isActive ? 'Aktywny' : 'Archiwalny'}
            </span>
                    </div>
                    <p className="text-txtcolor-300 mt-1 flex items-center gap-2">
                        <CalendarDays className="h-4 w-4" />
                        Rok szkolny: <span className="text-foreground font-medium">{academicYear}</span>
                    </p>
                </div>
            </div>
        </div>
    );
}