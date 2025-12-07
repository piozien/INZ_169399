'use client';

import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { fetchSuggestions, createSuggestion, deleteSuggestion, updateSuggestion } from "@/lib/api/suggestions";
import { useAuth } from "@/lib/contexts/AuthContext";
import SuggestionCard from "@/components/suggestions/SuggestionCard";
import EditSuggestionModal from "@/components/suggestions/EditSuggestionModal";
import SuggestionDetailsModal from "@/components/suggestions/SuggestionDetailsModal";
import {
    Loader2,
    Send,
    Lightbulb,
    Lock,
    Inbox,
    ChevronDown,
    ChevronUp,
    Filter,
    Search,
    History
} from "lucide-react";
import { SuggestionDto, CreateSuggestionPayload } from "@/types/suggestions.types";

export default function PublicSuggestionsPage() {
    const { user } = useAuth();
    const queryClient = useQueryClient();

    const TITLE_MAX_LENGTH = 100;
    const DESC_MAX_LENGTH = 1000;

    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [isAnonymous, setIsAnonymous] = useState(false);
    const [tagsInput, setTagsInput] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    const [isHistoryOpen, setIsHistoryOpen] = useState(false);
    const [filter, setFilter] = useState<'ALL' | 'APPROVED' | 'PENDING' | 'REJECTED'>('ALL');
    const [searchQuery, setSearchQuery] = useState("");

    const [editingSuggestion, setEditingSuggestion] = useState<SuggestionDto | null>(null);
    const [selectedSuggestion, setSelectedSuggestion] = useState<SuggestionDto | null>(null);

    const { data: suggestions, isLoading } = useQuery({
        queryKey: ['suggestions'],
        queryFn: fetchSuggestions,
        enabled: !!user,
    });

    const createMutation = useMutation({
        mutationFn: createSuggestion,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['suggestions'] });
            setTitle("");
            setDescription("");
            setTagsInput("");
            setIsAnonymous(false);
            alert("Twoja sugestia została wysłana!");
            setIsHistoryOpen(true);
        },
        onError: (error) => {
            const msg = error instanceof Error ? error.message : "Wystąpił błąd podczas wysyłania sugestii.";
            alert(`Błąd: ${msg}`);
        }
    });

    const updateMutation = useMutation({
        mutationFn: (variables: { id: string, data: Partial<CreateSuggestionPayload> }) =>
            updateSuggestion(variables.id, variables.data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['suggestions'] });
        },
        onError: (error) => {
            const msg = error instanceof Error ? error.message : "Błąd edycji.";
            alert(`Nie udało się zaktualizować: ${msg}`);
        }
    });

    const deleteMutation = useMutation({
        mutationFn: deleteSuggestion,
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['suggestions'] }),
        onError: (error) => {
            const msg = error instanceof Error ? error.message : "Błąd usuwania.";
            alert(`Nie udało się usunąć: ${msg}`);
        }
    });

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!user?.id) return alert("Musisz być zalogowany, aby wysłać sugestię.");

        if(title.length > TITLE_MAX_LENGTH) return alert(`Tytuł jest za długi (max ${TITLE_MAX_LENGTH} znaków).`);
        if(description.length > DESC_MAX_LENGTH) return alert(`Opis jest za długi (max ${DESC_MAX_LENGTH} znaków).`);

        setIsSubmitting(true);
        try {
            const tags = tagsInput.split(",").map(t => t.trim()).filter(t => t.length > 0);
            await createMutation.mutateAsync({
                title,
                description,
                anonymous: isAnonymous,
                tags,
                userId: user.id
            });
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleEditSubmit = async (id: string, data: Partial<CreateSuggestionPayload>) => {
        if (!user?.id) return;

        if(data.title && data.title.length > TITLE_MAX_LENGTH) return alert("Tytuł za długi.");
        if(data.description && data.description.length > DESC_MAX_LENGTH) return alert("Opis za długi.");

        const payload = { ...data, userId: user.id };
        await updateMutation.mutateAsync({ id, data: payload });
        setEditingSuggestion(null);
    };

    const userSuggestions = suggestions?.filter(s => s.userId === user?.id) || [];

    const displayedSuggestions = userSuggestions.filter(s => {
        const matchesStatus = filter === 'ALL' || s.status === filter;
        const textContent = `${s.title || ''} ${s.description || ''}`.toLowerCase();
        const matchesSearch = textContent.includes(searchQuery.toLowerCase());
        return matchesStatus && matchesSearch;
    });

    displayedSuggestions.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

    if (isLoading) return <div className="flex h-[50vh] items-center justify-center"><Loader2 className="w-8 h-8 text-primary animate-spin" /></div>;

    return (
        <div className="p-6 md:p-8 max-w-4xl mx-auto space-y-10">

            <div className="text-center space-y-2">
                <h1 className="text-3xl font-black text-foreground flex items-center justify-center gap-3">
                    <Inbox className="w-8 h-8 text-secondary" /> Skrzynka Sugestii
                </h1>
                <p className="text-txtcolor-300 max-w-lg mx-auto">
                    Masz pomysł na zmianę w szkole? Zgłoś go tutaj! Decyzję o realizacji podejmie Zarząd Samorządu.
                </p>
            </div>

            <div className="bg-secondarybg/30 border border-border rounded-2xl p-6 md:p-8 shadow-xl">
                <div className="flex items-center gap-2 mb-6 border-b border-border pb-4">
                    <Lightbulb className="w-5 h-5 text-warning" />
                    <h2 className="text-xl font-bold text-foreground">Nowe zgłoszenie</h2>
                </div>

                <form onSubmit={handleSubmit} className="space-y-5">
                    <div className="space-y-1">
                        <div className="flex justify-between items-center">
                            <label className="text-xs font-bold text-txtcolor-300 uppercase tracking-wider pl-1">Tytuł</label>
                            <span className={`text-[10px] ${title.length > TITLE_MAX_LENGTH ? 'text-error' : 'text-txtcolor-300'}`}>
                                {title.length}/{TITLE_MAX_LENGTH}
                            </span>
                        </div>
                        <input
                            type="text"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            maxLength={TITLE_MAX_LENGTH}
                            placeholder="np. Automat z napojami na 2 piętrze"
                            className="w-full bg-inputbg border border-border rounded-xl px-4 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all placeholder:text-txtcolor-300/50"
                            required
                        />
                    </div>

                    <div className="space-y-1">
                        <div className="flex justify-between items-center">
                            <label className="text-xs font-bold text-txtcolor-300 uppercase tracking-wider pl-1">Opis</label>
                            <span className={`text-[10px] ${description.length > DESC_MAX_LENGTH ? 'text-error' : 'text-txtcolor-300'}`}>
                                {description.length}/{DESC_MAX_LENGTH}
                            </span>
                        </div>
                        <textarea
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            maxLength={DESC_MAX_LENGTH}
                            placeholder="Opisz dokładnie swój pomysł...
Zgłoś skargę lub usterkę"
                            className="w-full h-32 bg-inputbg border border-border rounded-xl px-4 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all resize-none placeholder:text-txtcolor-300/50"
                            required
                        />
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                        <div className="space-y-1">
                            <label className="text-xs font-bold text-txtcolor-300 uppercase tracking-wider pl-1">Tagi (po przecinku)</label>
                            <input
                                type="text"
                                value={tagsInput}
                                onChange={(e) => setTagsInput(e.target.value)}
                                placeholder="np. szkoła, skarga, sprzęt"
                                className="w-full bg-inputbg border border-border rounded-xl px-4 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all placeholder:text-txtcolor-300/50"
                            />
                        </div>

                        <div className="flex items-end">
                            <div
                                onClick={() => setIsAnonymous(!isAnonymous)}
                                className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl border cursor-pointer transition-all ${isAnonymous ? 'bg-secondary/10 border-secondary' : 'bg-inputbg border-border hover:border-txtcolor-300'}`}
                            >
                                <div className={`w-10 h-6 rounded-full p-1 transition-colors flex-shrink-0 ${isAnonymous ? 'bg-secondary' : 'bg-darkgray'}`}>
                                    <div className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${isAnonymous ? 'translate-x-4' : ''}`} />
                                </div>
                                <div className="flex items-center gap-2 overflow-hidden">
                                    <Lock className={`w-4 h-4 flex-shrink-0 ${isAnonymous ? 'text-secondary' : 'text-txtcolor-300'}`} />
                                    <span className={`text-sm font-medium truncate ${isAnonymous ? 'text-foreground' : 'text-txtcolor-300'}`}>
                                        Wyślij anonimowo
                                    </span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div className="pt-2">
                        <button
                            type="submit"
                            disabled={isSubmitting}
                            className="w-full bg-primary text-darkgray hover:bg-secondary font-black text-lg py-3.5 rounded-xl transition-all flex items-center justify-center gap-2 shadow-lg shadow-primary/10 hover:shadow-secondary/20 hover:scale-[1.01] active:scale-[0.99]"
                        >
                            {isSubmitting ? <Loader2 className="w-5 h-5 animate-spin"/> : <><Send className="w-5 h-5" /> Wyślij Sugestie</>}
                        </button>
                    </div>
                </form>
            </div>

            <div className="border-t border-border pt-6">
                <button
                    onClick={() => setIsHistoryOpen(!isHistoryOpen)}
                    className="w-full flex items-center justify-between p-4 bg-secondarybg hover:bg-secondarybg/80 rounded-xl transition-colors group"
                >
                    <div className="flex items-center gap-3">
                        <History className="w-5 h-5 text-txtcolor-300 group-hover:text-foreground" />
                        <span className="font-bold text-lg text-foreground">Twoje Sugestie/Zgłoszenia</span>
                        <span className="bg-background px-2.5 py-0.5 rounded-full text-xs font-bold text-txtcolor-300 border border-border">
                            {userSuggestions.length}
                        </span>
                    </div>
                    {isHistoryOpen ? <ChevronUp className="w-5 h-5 text-txtcolor-300" /> : <ChevronDown className="w-5 h-5 text-txtcolor-300" />}
                </button>

                {isHistoryOpen && (
                    <div className="mt-6 space-y-6 animate-in slide-in-from-top-2 fade-in duration-300">

                        <div className="flex flex-col md:flex-row gap-3">
                            <div className="relative flex-1">
                                <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-txtcolor-300 w-4 h-4" />
                                <input
                                    type="text"
                                    placeholder="Szukaj w swoich zgłoszeniach..."
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    className="w-full pl-10 pr-4 py-2 rounded-lg bg-inputbg border border-border text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-secondary"
                                />
                            </div>
                            <div className="relative w-full md:w-48">
                                <Filter className="absolute left-3 top-1/2 -translate-y-1/2 text-txtcolor-300 w-4 h-4" />
                                <select
                                    value={filter}
                                    onChange={(e) => setFilter(e.target.value as any)}
                                    className="w-full pl-10 pr-8 py-2 rounded-lg bg-inputbg border border-border text-foreground text-sm focus:outline-none focus:ring-2 focus:ring-secondary appearance-none cursor-pointer"
                                >
                                    <option value="ALL">Wszystkie</option>
                                    <option value="PENDING">Oczekujące</option>
                                    <option value="APPROVED">Zatwierdzone</option>
                                    <option value="REJECTED">Odrzucone</option>
                                </select>
                                <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 text-txtcolor-300 w-4 h-4 pointer-events-none" />
                            </div>
                        </div>

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                            {displayedSuggestions.length > 0 ? (
                                displayedSuggestions.map(suggestion => (
                                    <SuggestionCard
                                        key={suggestion.id}
                                        suggestion={suggestion}
                                        canApprove={false}
                                        canReject={false}
                                        canEdit={true}
                                        canDelete={true}
                                        onClick={() => setSelectedSuggestion(suggestion)}
                                        onEdit={() => setEditingSuggestion(suggestion)}
                                        onApprove={() => {}}
                                        onReject={() => {}}
                                        onDelete={(id) => deleteMutation.mutate(id)}
                                    />
                                ))
                            ) : (
                                <div className="col-span-full py-12 flex flex-col items-center justify-center text-txtcolor-300 bg-secondarybg/20 rounded-xl border border-dashed border-border">
                                    <Inbox className="w-10 h-10 mb-2 opacity-30" />
                                    <p className="text-sm">Brak zgłoszeń spełniających kryteria.</p>
                                </div>
                            )}
                        </div>
                    </div>
                )}
            </div>

            {editingSuggestion && (
                <EditSuggestionModal
                    isOpen={!!editingSuggestion}
                    onClose={() => setEditingSuggestion(null)}
                    suggestion={editingSuggestion}
                    onSubmit={handleEditSubmit}
                />
            )}

            {selectedSuggestion && (
                <SuggestionDetailsModal
                    isOpen={!!selectedSuggestion}
                    onClose={() => setSelectedSuggestion(null)}
                    suggestion={selectedSuggestion}
                />
            )}
        </div>
    );
}