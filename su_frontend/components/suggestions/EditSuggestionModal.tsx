'use client';

import { X, Save, Lightbulb, Lock, Loader2 } from 'lucide-react';
import { SuggestionDto, CreateSuggestionPayload } from '@/types/suggestions.types';
import { useEditSuggestionForm } from '@/hooks/suggestions/useEditSuggestionForm';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    suggestion: SuggestionDto;
    onSubmit: (id: string, data: Partial<CreateSuggestionPayload>) => Promise<void>;
}

export default function EditSuggestionModal({ isOpen, onClose, suggestion, onSubmit }: Props) {
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
    } = useEditSuggestionForm(suggestion, isOpen, onClose, onSubmit);

    if (!isOpen) return null;

    return (
        <div className="bg-background/80 animate-in fade-in fixed inset-0 z-50 flex items-center justify-center p-4 backdrop-blur-sm">
            <div className="bg-background border-secondarybg animate-in zoom-in-95 w-full max-w-lg overflow-hidden rounded-2xl border shadow-2xl">
                <div className="bg-surface border-secondarybg flex items-center justify-between border-b p-4">
                    <h2 className="text-foreground flex items-center gap-2 text-lg font-bold">
                        <Lightbulb className="text-secondary h-5 w-5" /> Edytuj Sugestię
                    </h2>
                    <button onClick={onClose} className="text-txtcolor-300 hover:text-foreground">
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-5 p-6">
                    <div className="space-y-1">
                        <div className="flex items-center justify-between">
                            <label className="text-txtcolor-300 text-xs font-bold tracking-wider uppercase">
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
                            className="bg-inputbg border-secondarybg text-foreground focus:border-secondary w-full rounded-lg border px-4 py-3 transition-colors focus:outline-none"
                            required
                        />
                    </div>

                    <div className="space-y-1">
                        <div className="flex items-center justify-between">
                            <label className="text-txtcolor-300 text-xs font-bold tracking-wider uppercase">
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
                            className="bg-inputbg border-secondarybg text-foreground focus:border-secondary h-32 w-full resize-none rounded-lg border px-4 py-3 transition-colors focus:outline-none"
                            required
                        />
                    </div>

                    <div className="space-y-1">
                        <label className="text-txtcolor-300 text-xs font-bold tracking-wider uppercase">
                            Tagi (po przecinku)
                        </label>
                        <input
                            type="text"
                            value={tagsInput}
                            onChange={(e) => setTagsInput(e.target.value)}
                            className="bg-inputbg border-secondarybg text-foreground focus:border-secondary w-full rounded-lg border px-4 py-3 transition-colors focus:outline-none"
                        />
                    </div>

                    <div
                        className="bg-surface border-secondarybg/50 flex cursor-pointer items-center gap-3 rounded-lg border p-3"
                        onClick={() => setIsAnonymous(!isAnonymous)}
                    >
                        <div
                            className={`h-6 w-10 rounded-full p-1 transition-colors ${isAnonymous ? 'bg-secondary' : 'bg-darkgray'}`}
                        >
                            <div
                                className={`h-4 w-4 transform rounded-full bg-white shadow-md transition-transform ${isAnonymous ? 'translate-x-4' : ''}`}
                            />
                        </div>
                        <div className="flex items-center gap-2">
                            <Lock
                                className={`h-4 w-4 ${isAnonymous ? 'text-secondary' : 'text-txtcolor-300'}`}
                            />
                            <span
                                className={`text-sm font-medium ${isAnonymous ? 'text-foreground' : 'text-txtcolor-300'}`}
                            >
                                Anonimowo
                            </span>
                        </div>
                    </div>

                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="bg-primary text-darkgray hover:bg-secondary flex w-full items-center justify-center gap-2 rounded-xl py-3 font-bold transition-all"
                    >
                        {isSubmitting ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                            <>
                                <Save className="h-4 w-4" /> Zapisz Zmiany
                            </>
                        )}
                    </button>
                </form>
            </div>
        </div>
    );
}
