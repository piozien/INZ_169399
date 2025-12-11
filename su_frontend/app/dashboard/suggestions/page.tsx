'use client';

import SuggestionCard from '@/components/suggestions/SuggestionCard';
import EditSuggestionModal from '@/components/suggestions/EditSuggestionModal';
import SuggestionDetailsModal from '@/components/suggestions/SuggestionDetailsModal';
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
    History,
} from 'lucide-react';
import { usePublicSuggestions } from '@/hooks/suggestions/usePublicSuggestions';

export default function PublicSuggestionsPage() {
    const {
        title,
        setTitle,
        description,
        setDescription,
        isAnonymous,
        setIsAnonymous,
        tagsInput,
        setTagsInput,
        isSubmitting,
        handleSubmit,
        TITLE_MAX_LENGTH,
        DESC_MAX_LENGTH,
        isLoading,
        userSuggestions,
        displayedSuggestions,
        isHistoryOpen,
        setIsHistoryOpen,
        filter,
        setFilter,
        searchQuery,
        setSearchQuery,
        editingSuggestion,
        setEditingSuggestion,
        selectedSuggestion,
        setSelectedSuggestion,
        handleEditSubmit,
        deleteSuggestion,
    } = usePublicSuggestions();

    if (isLoading)
        return (
            <div className="flex h-[50vh] items-center justify-center">
                <Loader2 className="text-primary h-8 w-8 animate-spin" />
            </div>
        );

    return (
        <div className="animate-in fade-in mx-auto max-w-4xl space-y-10 p-6 duration-500 md:p-8">
            <div className="space-y-2 text-center">
                <h1 className="text-foreground flex items-center justify-center gap-3 text-3xl font-black">
                    <Inbox className="text-secondary h-8 w-8" /> Skrzynka Sugestii
                </h1>
                <p className="text-txtcolor-300 mx-auto max-w-lg">
                    Masz pomysł na zmianę w szkole? Zgłoś go tutaj! Decyzję o realizacji podejmie
                    Zarząd Samorządu.
                </p>
            </div>

            <div className="bg-secondarybg/30 border-border rounded-2xl border p-6 shadow-xl md:p-8">
                <div className="border-border mb-6 flex items-center gap-2 border-b pb-4">
                    <Lightbulb className="text-warning h-5 w-5" />
                    <h2 className="text-foreground text-xl font-bold">Nowe zgłoszenie</h2>
                </div>

                <form onSubmit={handleSubmit} className="space-y-5">
                    <div className="space-y-1">
                        <div className="flex items-center justify-between">
                            <label className="text-txtcolor-300 pl-1 text-xs font-bold tracking-wider uppercase">
                                Tytuł
                            </label>
                            <span
                                className={`text-[10px] ${title.length > TITLE_MAX_LENGTH ? 'text-error' : 'text-txtcolor-300'}`}
                            >
                                {title.length}/{TITLE_MAX_LENGTH}
                            </span>
                        </div>
                        <input
                            type="text"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            maxLength={TITLE_MAX_LENGTH}
                            placeholder="np. Automat z napojami na 2 piętrze"
                            className="bg-inputbg border-border text-foreground focus:ring-primary placeholder:text-txtcolor-300/50 w-full rounded-xl border px-4 py-3 transition-all focus:ring-2 focus:outline-none"
                            required
                        />
                    </div>

                    <div className="space-y-1">
                        <div className="flex items-center justify-between">
                            <label className="text-txtcolor-300 pl-1 text-xs font-bold tracking-wider uppercase">
                                Opis
                            </label>
                            <span
                                className={`text-[10px] ${description.length > DESC_MAX_LENGTH ? 'text-error' : 'text-txtcolor-300'}`}
                            >
                                {description.length}/{DESC_MAX_LENGTH}
                            </span>
                        </div>
                        <textarea
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            maxLength={DESC_MAX_LENGTH}
                            placeholder="Opisz dokładnie swój pomysł... Zgłoś skargę lub usterkę"
                            className="bg-inputbg border-border text-foreground focus:ring-primary placeholder:text-txtcolor-300/50 h-32 w-full resize-none rounded-xl border px-4 py-3 transition-all focus:ring-2 focus:outline-none"
                            required
                        />
                    </div>

                    <div className="grid grid-cols-1 gap-5 md:grid-cols-2">
                        <div className="space-y-1">
                            <label className="text-txtcolor-300 pl-1 text-xs font-bold tracking-wider uppercase">
                                Tagi (po przecinku)
                            </label>
                            <input
                                type="text"
                                value={tagsInput}
                                onChange={(e) => setTagsInput(e.target.value)}
                                placeholder="np. szkoła, skarga, sprzęt"
                                className="bg-inputbg border-border text-foreground focus:ring-primary placeholder:text-txtcolor-300/50 w-full rounded-xl border px-4 py-3 transition-all focus:ring-2 focus:outline-none"
                            />
                        </div>

                        <div className="flex items-end">
                            <div
                                onClick={() => setIsAnonymous(!isAnonymous)}
                                className={`flex w-full cursor-pointer items-center gap-3 rounded-xl border px-4 py-3 transition-all ${isAnonymous ? 'bg-secondary/10 border-secondary' : 'bg-inputbg border-border hover:border-txtcolor-300'}`}
                            >
                                <div
                                    className={`h-6 w-10 flex-shrink-0 rounded-full p-1 transition-colors ${isAnonymous ? 'bg-secondary' : 'bg-darkgray'}`}
                                >
                                    <div
                                        className={`h-4 w-4 transform rounded-full bg-white shadow-md transition-transform ${isAnonymous ? 'translate-x-4' : ''}`}
                                    />
                                </div>
                                <div className="flex items-center gap-2 overflow-hidden">
                                    <Lock
                                        className={`h-4 w-4 flex-shrink-0 ${isAnonymous ? 'text-secondary' : 'text-txtcolor-300'}`}
                                    />
                                    <span
                                        className={`truncate text-sm font-medium ${isAnonymous ? 'text-foreground' : 'text-txtcolor-300'}`}
                                    >
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
                            className="bg-primary text-darkgray hover:bg-secondary shadow-primary/10 hover:shadow-secondary/20 flex w-full items-center justify-center gap-2 rounded-xl py-3.5 text-lg font-black shadow-lg transition-all hover:scale-[1.01] active:scale-[0.99]"
                        >
                            {isSubmitting ? (
                                <Loader2 className="h-5 w-5 animate-spin" />
                            ) : (
                                <>
                                    <Send className="h-5 w-5" /> Wyślij Sugestie
                                </>
                            )}
                        </button>
                    </div>
                </form>
            </div>

            <div className="border-border border-t pt-6">
                <button
                    onClick={() => setIsHistoryOpen(!isHistoryOpen)}
                    className="bg-secondarybg hover:bg-secondarybg/80 group flex w-full items-center justify-between rounded-xl p-4 transition-colors"
                >
                    <div className="flex items-center gap-3">
                        <History className="text-txtcolor-300 group-hover:text-foreground h-5 w-5" />
                        <span className="text-foreground text-lg font-bold">
                            Twoje Sugestie/Zgłoszenia
                        </span>
                        <span className="bg-background text-txtcolor-300 border-border rounded-full border px-2.5 py-0.5 text-xs font-bold">
                            {userSuggestions.length}
                        </span>
                    </div>
                    {isHistoryOpen ? (
                        <ChevronUp className="text-txtcolor-300 h-5 w-5" />
                    ) : (
                        <ChevronDown className="text-txtcolor-300 h-5 w-5" />
                    )}
                </button>

                {isHistoryOpen && (
                    <div className="animate-in slide-in-from-top-2 fade-in mt-6 space-y-6 duration-300">
                        <div className="flex flex-col gap-3 md:flex-row">
                            <div className="relative flex-1">
                                <Search className="text-txtcolor-300 absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
                                <input
                                    type="text"
                                    placeholder="Szukaj w swoich zgłoszeniach..."
                                    value={searchQuery}
                                    onChange={(e) => setSearchQuery(e.target.value)}
                                    className="bg-inputbg border-border text-foreground focus:ring-secondary w-full rounded-lg border py-2 pr-4 pl-10 text-sm focus:ring-2 focus:outline-none"
                                />
                            </div>
                            <div className="relative w-full md:w-48">
                                <Filter className="text-txtcolor-300 absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
                                <select
                                    value={filter}
                                    onChange={(e) => setFilter(e.target.value as any)}
                                    className="bg-inputbg border-border text-foreground focus:ring-secondary w-full cursor-pointer appearance-none rounded-lg border py-2 pr-8 pl-10 text-sm focus:ring-2 focus:outline-none"
                                >
                                    <option value="ALL">Wszystkie</option>
                                    <option value="PENDING">Oczekujące</option>
                                    <option value="APPROVED">Zatwierdzone</option>
                                    <option value="REJECTED">Odrzucone</option>
                                </select>
                                <ChevronDown className="text-txtcolor-300 pointer-events-none absolute top-1/2 right-3 h-4 w-4 -translate-y-1/2" />
                            </div>
                        </div>

                        <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                            {displayedSuggestions.length > 0 ? (
                                displayedSuggestions.map((suggestion) => (
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
                                        onDelete={(id) => deleteSuggestion(id)}
                                    />
                                ))
                            ) : (
                                <div className="text-txtcolor-300 bg-secondarybg/20 border-border col-span-full flex flex-col items-center justify-center rounded-xl border border-dashed py-12">
                                    <Inbox className="mb-2 h-10 w-10 opacity-30" />
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
