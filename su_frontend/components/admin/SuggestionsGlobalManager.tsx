'use client';

import { useState } from 'react';
import {
    Trash2,
    Loader2,
    User,
    Clock,
    CheckCircle,
    Ban,
    MessageSquare,
    Search,
    Filter,
    Maximize2,
} from 'lucide-react';
import { useAdminSuggestions } from '@/hooks/admin/useAdminSuggestions';
import { format } from 'date-fns';
import { pl } from 'date-fns/locale';
import { toast } from 'sonner';
import SuggestionDetailsModal from '@/components/suggestions/SuggestionDetailsModal';

export default function SuggestionsGlobalManager() {
    const {
        suggestions,
        isLoading,
        deleteSuggestion,
        searchQuery,
        setSearchQuery,
        statusFilter,
        setStatusFilter,
    } = useAdminSuggestions();

    const [selectedSuggestion, setSelectedSuggestion] = useState<any>(null);

    if (isLoading)
        return (
            <div className="flex justify-center p-10">
                <Loader2 className="text-primary h-8 w-8 animate-spin" />
            </div>
        );

    const getStatusBadge = (status: string) => {
        if (status === 'APPROVED')
            return (
                <span className="text-success bg-success/10 border-success/20 flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-[10px] font-bold uppercase">
                    <CheckCircle className="h-3 w-3" /> Zatwierdzona
                </span>
            );
        if (status === 'REJECTED')
            return (
                <span className="text-error bg-error/10 border-error/20 flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-[10px] font-bold uppercase">
                    <Ban className="h-3 w-3" /> Odrzucona
                </span>
            );
        return (
            <span className="text-warning bg-warning/10 border-warning/20 flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-[10px] font-bold uppercase">
                <Clock className="h-3 w-3" /> Oczekująca
            </span>
        );
    };

    const handleDeleteClick = (e: React.MouseEvent, suggestionId: string) => {
        e.stopPropagation();
        toast('Czy na pewno chcesz usunąć tę sugestię?', {
            description: 'Operacja jest nieodwracalna.',
            action: {
                label: 'Usuń',
                onClick: () => deleteSuggestion(suggestionId),
            },
            cancel: {
                label: 'Anuluj',
                onClick: () => {},
            },
        });
    };

    return (
        <div className="space-y-6">
            <div className="bg-secondarybg/30 border-border grid grid-cols-1 gap-4 rounded-xl border p-4 md:grid-cols-3">
                <div className="relative md:col-span-2">
                    <Search className="text-txtcolor-300 absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
                    <input
                        type="text"
                        placeholder="Szukaj w treści, tytule lub autorze..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="bg-inputbg border-border focus:ring-primary w-full rounded-lg border py-2 pr-4 pl-10 text-sm transition-all focus:ring-2 focus:outline-none"
                    />
                </div>
                <div className="relative">
                    <Filter className="text-txtcolor-300 absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value as any)}
                        className="bg-inputbg border-border w-full cursor-pointer rounded-lg border py-2 pr-4 pl-10 text-sm"
                    >
                        <option value="ALL">Wszystkie Statusy</option>
                        <option value="PENDING">Oczekujące</option>
                        <option value="APPROVED">Zatwierdzone</option>
                        <option value="REJECTED">Odrzucone</option>
                    </select>
                </div>
            </div>

            <div className="bg-background border-border overflow-hidden rounded-xl border shadow-sm">
                <div className="overflow-x-auto">
                    <table className="w-full text-left text-sm">
                        <thead className="bg-secondarybg text-txtcolor-300 border-border border-b text-xs font-bold uppercase">
                        <tr>
                            <th className="px-6 py-4">Tytuł / Treść</th>
                            <th className="px-6 py-4">Autor / Data</th>
                            <th className="px-6 py-4">Status</th>
                            <th className="px-6 py-4 text-right">Akcje</th>
                        </tr>
                        </thead>
                        <tbody className="divide-border divide-y">
                        {suggestions?.map((s: any) => (
                            <tr
                                key={s.id}
                                onClick={() => setSelectedSuggestion(s)}
                                className="hover:bg-secondarybg/30 group cursor-pointer transition-colors"
                            >
                                <td className="max-w-[300px] px-6 py-4 md:max-w-[400px]">
                                    <div className="text-foreground mb-1 flex items-center gap-2 truncate font-bold">
                                        {s.title}
                                        <Maximize2 className="text-txtcolor-300 h-3 w-3 opacity-0 transition-opacity group-hover:opacity-100" />
                                    </div>
                                    <div className="text-txtcolor-300 line-clamp-2 text-xs leading-relaxed">
                                        {s.description}
                                    </div>
                                </td>
                                <td className="px-6 py-4">
                                    <div className="flex flex-col gap-1">
                                        <div className="text-foreground flex items-center gap-2 text-xs font-medium">
                                            <User className="text-txtcolor-300 h-3 w-3" />
                                            {s.anonymous ? (
                                                <span className="italic opacity-70">
                                                        Anonim
                                                    </span>
                                            ) : (
                                                s.fullName
                                            )}
                                        </div>
                                        <div className="text-txtcolor-300 font-mono text-[10px]">
                                            {format(
                                                new Date(s.createdAt),
                                                'dd MMM yyyy, HH:mm',
                                                { locale: pl }
                                            )}
                                        </div>
                                    </div>
                                </td>
                                <td className="px-6 py-4">{getStatusBadge(s.status)}</td>
                                <td className="px-6 py-4 text-right">
                                    <div
                                        className="flex justify-end"
                                        onClick={(e) => e.stopPropagation()}
                                    >
                                        <button
                                            onClick={(e) => handleDeleteClick(e, s.id)}
                                            className="hover:bg-error/10 text-txtcolor-300 hover:text-error rounded-lg p-2 transition-colors"
                                            title="Usuń sugestię"
                                        >
                                            <Trash2 className="h-4 w-4" />
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
                {suggestions?.length === 0 && (
                    <div className="text-txtcolor-300 flex flex-col items-center justify-center p-12 opacity-60">
                        <MessageSquare className="mb-3 h-12 w-12 stroke-1" />
                        <p className="italic">Brak zgłoszeń spełniających kryteria.</p>
                    </div>
                )}
            </div>

            {selectedSuggestion && (
                <SuggestionDetailsModal
                    isOpen={!!selectedSuggestion}
                    onClose={() => setSelectedSuggestion(null)}
                    suggestion={selectedSuggestion}
                    canApprove={false}
                    canReject={false}
                />
            )}
        </div>
    );
}