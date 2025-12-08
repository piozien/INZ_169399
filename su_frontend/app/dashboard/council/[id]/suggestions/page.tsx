'use client';

import {useState} from "react";
import {useParams, useRouter} from "next/navigation";
import {useQuery, useMutation, useQueryClient} from "@tanstack/react-query";
import {fetchCouncilSuggestions, approveSuggestion, rejectSuggestion, deleteSuggestion} from "@/lib/api/suggestions";
import {fetchCouncilById} from "@/lib/api/council";
import SuggestionCard from "@/components/suggestions/SuggestionCard";
import SuggestionDetailsModal from "@/components/suggestions/SuggestionDetailsModal";
import {SuggestionDto} from "@/types/suggestions.types";
import {
    ShieldCheck,
    AlertCircle,
    Search,
    Filter,
    ChevronDown,
    Loader2,
    ArrowLeft
} from "lucide-react";

export default function CouncilSuggestionsPage() {
    const params = useParams();
    const router = useRouter();
    const rawId = params?.id;
    const councilId = Array.isArray(rawId) ? rawId[0] : rawId;

    const queryClient = useQueryClient();

    const [filter, setFilter] = useState<'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED'>('PENDING');
    const [searchQuery, setSearchQuery] = useState("");
    const [selectedSuggestion, setSelectedSuggestion] = useState<SuggestionDto | null>(null);

    const {data: councilSuggestions, isLoading: suggestionsLoading} = useQuery({
        queryKey: ['councilSuggestions', councilId],
        queryFn: () => fetchCouncilSuggestions(councilId || ""),
        enabled: !!councilId
    });

    const {data: council, isLoading: councilLoading} = useQuery({
        queryKey: ['council', councilId],
        queryFn: () => fetchCouncilById(councilId || ""),
        enabled: !!councilId
    });

    const getPermissions = () => {
        if (!council?.myPermissions) return {canApprove: false, canReject: false, canDelete: false};

        const perms = council.myPermissions;
        const hasAll = perms.includes('ALL_ACCESS');

        return {
            canApprove: hasAll || perms.includes('SUGGESTION_APPROVE'),
            canReject: hasAll || perms.includes('SUGGESTION_DELETE'),
            canDelete: hasAll || perms.includes('SUGGESTION_DELETE')
        };
    };

    const {canApprove, canReject, canDelete} = getPermissions();

    const approveMutation = useMutation({
        mutationFn: approveSuggestion,
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['councilSuggestions', councilId]});
            setSelectedSuggestion(null);
        },
        onError: (err) => alert("Błąd zatwierdzania: " + (err instanceof Error ? err.message : "Nieznany"))
    });

    const rejectMutation = useMutation({
        mutationFn: (vars: { id: string; reason: string }) => rejectSuggestion(vars.id, vars.reason),
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['councilSuggestions', councilId]});
            setSelectedSuggestion(null);
        },
        onError: (err) => alert("Błąd odrzucania: " + (err instanceof Error ? err.message : "Nieznany"))
    });

    const deleteMutation = useMutation({
        mutationFn: deleteSuggestion,
        onSuccess: () => queryClient.invalidateQueries({queryKey: ['councilSuggestions', councilId]}),
        onError: (err) => alert("Błąd usuwania: " + (err instanceof Error ? err.message : "Nieznany"))
    });

    const displayedSuggestions = councilSuggestions?.filter(s => {
        const matchesStatus = filter === 'ALL' || s.status === filter;
        const textContent = `${s.title || ''} ${s.description || ''}`.toLowerCase();
        const matchesSearch = textContent.includes(searchQuery.toLowerCase());

        return matchesStatus && matchesSearch;
    }) || [];

    displayedSuggestions.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

    if (suggestionsLoading || councilLoading) return <div className="flex h-[50vh] items-center justify-center"><Loader2
        className="w-8 h-8 text-primary animate-spin"/></div>;

    return (
        <div className="p-6 space-y-8">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div className="flex items-center gap-3">
                    <button
                        onClick={() => router.push(`/dashboard/council/${councilId}`)}
                        className="p-2 -ml-2 rounded-xl text-txtcolor-300 hover:text-foreground hover:bg-secondarybg transition-colors"
                        title="Powrót do samorządu"
                    >
                        <ArrowLeft className="h-6 w-6"/>
                    </button>
                    <div>
                        <h1 className="text-2xl font-bold text-foreground flex items-center gap-2">
                            <ShieldCheck className="text-primary"/> Zarządzanie Sugestiami
                        </h1>
                        <p className="text-txtcolor-300 text-sm mt-1">
                            Decyduj o pomysłach zgłoszonych przez uczniów.
                        </p>
                    </div>
                </div>
            </div>

            <div className="flex flex-col md:flex-row gap-4 bg-secondarybg p-4 rounded-xl border border-secondarybg">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-txtcolor-300 w-4 h-4"/>
                    <input
                        type="text"
                        placeholder="Szukaj w sugestiach..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="w-full pl-10 pr-4 py-2 rounded-lg bg-inputbg border border-secondarybg text-foreground focus:outline-none focus:ring-2 focus:ring-primary placeholder:text-txtcolor-300"
                    />
                </div>

                <div className="relative w-full md:w-48">
                    <Filter className="absolute left-3 top-1/2 -translate-y-1/2 text-txtcolor-300 w-4 h-4"/>
                    <select
                        value={filter}
                        onChange={(e) => setFilter(e.target.value as any)}
                        className="w-full pl-10 pr-8 py-2 rounded-lg bg-inputbg border border-secondarybg text-foreground focus:outline-none focus:ring-2 focus:ring-primary appearance-none cursor-pointer"
                    >
                        <option value="ALL">Wszystkie</option>
                        <option value="PENDING">Do decyzji</option>
                        <option value="APPROVED">Zatwierdzone</option>
                        <option value="REJECTED">Odrzucone</option>
                    </select>
                    <ChevronDown
                        className="absolute right-3 top-1/2 -translate-y-1/2 text-txtcolor-300 w-4 h-4 pointer-events-none"/>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {displayedSuggestions.length > 0 ? (
                    displayedSuggestions.map(suggestion => (
                        <SuggestionCard
                            key={suggestion.id}
                            suggestion={suggestion}
                            canApprove={false}
                            canReject={false}
                            canDelete={canDelete}
                            onClick={() => setSelectedSuggestion(suggestion)}
                            onApprove={() => {
                            }}
                            onReject={() => {
                            }}
                            onDelete={(id) => deleteMutation.mutate(id)}
                        />
                    ))
                ) : (
                    <div
                        className="col-span-full py-12 flex flex-col items-center justify-center text-txtcolor-300 bg-secondarybg/30 rounded-xl border-2 border-dashed border-secondarybg">
                        <AlertCircle className="w-10 h-10 mb-3 opacity-20"/>
                        <p>Brak sugestii spełniających kryteria.</p>
                    </div>
                )}
            </div>

            {selectedSuggestion && (
                <SuggestionDetailsModal
                    isOpen={!!selectedSuggestion}
                    onClose={() => setSelectedSuggestion(null)}
                    suggestion={selectedSuggestion}
                    canApprove={canApprove}
                    canReject={canReject}
                    onApprove={(id) => approveMutation.mutate(id)}
                    onReject={(id, reason) => rejectMutation.mutate({id, reason})}
                />
            )}
        </div>
    );
}