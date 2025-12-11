'use client';

import { useParams, useRouter } from 'next/navigation';
import SuggestionCard from '@/components/suggestions/SuggestionCard';
import SuggestionDetailsModal from '@/components/suggestions/SuggestionDetailsModal';
import {
    ShieldCheck,
    AlertCircle,
    Search,
    Filter,
    ChevronDown,
    Loader2,
    ArrowLeft,
} from 'lucide-react';
import { useCouncilSuggestions } from '@/hooks/council/suggestions/useCouncilSuggestions';

export default function CouncilSuggestionsPage() {
    const params = useParams();
    const router = useRouter();
    const rawId = params?.id;
    const councilId = Array.isArray(rawId) ? rawId[0] : rawId || '';

    const {
        suggestions,
        isLoading,
        permissions: { canApprove, canReject, canDelete },
        filter,
        setFilter,
        searchQuery,
        setSearchQuery,
        selectedSuggestion,
        setSelectedSuggestion,
        approve,
        reject,
        remove,
    } = useCouncilSuggestions(councilId);

    if (isLoading)
        return (
            <div className="flex h-[50vh] items-center justify-center">
                <Loader2 className="text-primary h-8 w-8 animate-spin" />
            </div>
        );

    return (
        <div className="space-y-8 p-6">
            <div className="flex flex-col items-start justify-between gap-4 md:flex-row md:items-center">
                <div className="flex items-center gap-3">
                    <button
                        onClick={() => router.push(`/dashboard/council/${councilId}`)}
                        className="text-txtcolor-300 hover:text-foreground hover:bg-secondarybg -ml-2 rounded-xl p-2 transition-colors"
                        title="Powrót do samorządu"
                    >
                        <ArrowLeft className="h-6 w-6" />
                    </button>
                    <div>
                        <h1 className="text-foreground flex items-center gap-2 text-2xl font-bold">
                            <ShieldCheck className="text-primary" /> Zarządzanie Sugestiami
                        </h1>
                        <p className="text-txtcolor-300 mt-1 text-sm">
                            Decyduj o pomysłach zgłoszonych przez uczniów.
                        </p>
                    </div>
                </div>
            </div>

            <div className="bg-secondarybg border-secondarybg flex flex-col gap-4 rounded-xl border p-4 md:flex-row">
                <div className="relative flex-1">
                    <Search className="text-txtcolor-300 absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
                    <input
                        type="text"
                        placeholder="Szukaj w sugestiach..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="bg-inputbg border-secondarybg text-foreground focus:ring-primary placeholder:text-txtcolor-300 w-full rounded-lg border py-2 pr-4 pl-10 focus:ring-2 focus:outline-none"
                    />
                </div>

                <div className="relative w-full md:w-48">
                    <Filter className="text-txtcolor-300 absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
                    <select
                        value={filter}
                        onChange={(e) => setFilter(e.target.value as any)}
                        className="bg-inputbg border-secondarybg text-foreground focus:ring-primary w-full cursor-pointer appearance-none rounded-lg border py-2 pr-8 pl-10 focus:ring-2 focus:outline-none"
                    >
                        <option value="ALL">Wszystkie</option>
                        <option value="PENDING">Do decyzji</option>
                        <option value="APPROVED">Zatwierdzone</option>
                        <option value="REJECTED">Odrzucone</option>
                    </select>
                    <ChevronDown className="text-txtcolor-300 pointer-events-none absolute top-1/2 right-3 h-4 w-4 -translate-y-1/2" />
                </div>
            </div>

            <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
                {suggestions.length > 0 ? (
                    suggestions.map((suggestion) => (
                        <SuggestionCard
                            key={suggestion.id}
                            suggestion={suggestion}
                            canApprove={false}
                            canReject={false}
                            canDelete={canDelete}
                            onClick={() => setSelectedSuggestion(suggestion)}
                            onApprove={() => {}}
                            onReject={() => {}}
                            onDelete={(id) => remove(id)}
                        />
                    ))
                ) : (
                    <div className="text-txtcolor-300 bg-secondarybg/30 border-secondarybg col-span-full flex flex-col items-center justify-center rounded-xl border-2 border-dashed py-12">
                        <AlertCircle className="mb-3 h-10 w-10 opacity-20" />
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
                    onApprove={(id) => approve(id)}
                    onReject={(id, reason) => reject(id, reason)}
                />
            )}
        </div>
    );
}
