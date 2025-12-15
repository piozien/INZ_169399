'use client';

import { useRouter } from 'next/navigation';
import { Landmark, Trash2, Calendar, Users, Loader2 } from 'lucide-react';
import { useAdminCouncils } from '@/hooks/admin/useAdminCouncils';
import { format } from 'date-fns';

export default function CouncilsManager() {
    const router = useRouter();
    const { councils, isLoading, deleteCouncil } = useAdminCouncils();

    if (isLoading)
        return (
            <div className="flex justify-center p-10">
                <Loader2 className="text-primary h-8 w-8 animate-spin" />
            </div>
        );

    return (
        <div className="space-y-6">
            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3">
                {councils?.map((council: any) => (
                    <div
                        key={council.id}
                        onClick={() => router.push(`/dashboard/council/${council.id}`)}
                        className="bg-background border-border hover:border-primary/50 group relative flex cursor-pointer flex-col justify-between overflow-hidden rounded-xl border p-5 shadow-sm transition-all hover:shadow-md"
                    >
                        <div className="bg-primary absolute top-0 left-0 h-full w-1 opacity-0 transition-opacity group-hover:opacity-100" />

                        <div>
                            <div className="mb-2 flex items-start justify-between">
                                <div className="bg-secondarybg text-secondary group-hover:text-primary rounded-lg p-2 transition-colors">
                                    <Landmark className="h-6 w-6" />
                                </div>
                                {council.active && (
                                    <span className="bg-success/10 text-success border-success/20 rounded border px-2 py-1 text-[10px] font-bold uppercase">
                                        Aktywny
                                    </span>
                                )}
                            </div>
                            <h3 className="text-foreground mb-1 line-clamp-1 text-lg font-bold">
                                {council.name}
                            </h3>
                            <div className="text-txtcolor-300 flex items-center gap-2 text-sm">
                                <Calendar className="h-3.5 w-3.5" />
                                {format(new Date(council.startDate), 'yyyy')} -{' '}
                                {council.endDate
                                    ? format(new Date(council.endDate), 'yyyy')
                                    : '...'}
                            </div>
                        </div>

                        <div
                            className="border-border mt-6 flex items-center justify-between border-t pt-4"
                            onClick={(e) => e.stopPropagation()}
                        >
                            <div className="text-txtcolor-300 flex items-center gap-1 text-xs font-bold uppercase">
                                <Users className="h-3.5 w-3.5" /> Kod:{' '}
                                <span className="text-foreground font-mono">
                                    {council.joinCode}
                                </span>
                            </div>
                            <button
                                onClick={() => {
                                    if (
                                        confirm(
                                            'UWAGA: Usunięcie samorządu usunie też jego członków, historię finansów i wydarzenia. Czy kontynuować?'
                                        )
                                    )
                                        deleteCouncil(council.id);
                                }}
                                className="hover:bg-error/10 text-txtcolor-300 hover:text-error z-10 rounded-lg p-2 transition-colors"
                                title="Usuń samorząd trwale"
                            >
                                <Trash2 className="h-4 w-4" />
                            </button>
                        </div>
                    </div>
                ))}
            </div>

            {councils?.length === 0 && (
                <div className="text-txtcolor-300 flex flex-col items-center justify-center p-12 opacity-60">
                    <Landmark className="mb-3 h-12 w-12 stroke-1" />
                    <p className="italic">Brak utworzonych samorządów.</p>
                </div>
            )}
        </div>
    );
}
