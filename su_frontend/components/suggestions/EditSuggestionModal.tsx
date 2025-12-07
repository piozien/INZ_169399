'use client';

import { useState, useEffect } from "react";
import { X, Save, Lightbulb, Lock, Loader2 } from "lucide-react";
import { SuggestionDto, CreateSuggestionPayload } from "@/types/suggestions.types";
import { useAuth } from "@/lib/contexts/AuthContext";

interface Props {
    isOpen: boolean;
    onClose: () => void;
    suggestion: SuggestionDto;
    onSubmit: (id: string, data: Partial<CreateSuggestionPayload>) => Promise<void>;
}

export default function EditSuggestionModal({ isOpen, onClose, suggestion, onSubmit }: Props) {
    const { user } = useAuth();
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [isAnonymous, setIsAnonymous] = useState(false);
    const [tagsInput, setTagsInput] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);

    const TITLE_MAX_LENGTH = 100;
    const DESC_MAX_LENGTH = 1000;

    useEffect(() => {
        if (isOpen && suggestion) {
            setTitle(suggestion.title);
            setDescription(suggestion.description);
            setIsAnonymous(suggestion.anonymous);
            setTagsInput((suggestion.tags || []).join(", "));
        }
    }, [isOpen, suggestion]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!title || !description) return;

        if (!user?.id) {
            alert("Błąd: Nie rozpoznano użytkownika.");
            return;
        }

        if(title.length > TITLE_MAX_LENGTH) return alert(`Tytuł jest za długi (max ${TITLE_MAX_LENGTH} znaków).`);
        if(description.length > DESC_MAX_LENGTH) return alert(`Opis jest za długi (max ${DESC_MAX_LENGTH} znaków).`);

        setIsSubmitting(true);
        try {
            const tags = tagsInput.split(",").map(t => t.trim()).filter(t => t.length > 0);

            await onSubmit(suggestion.id, {
                title,
                description,
                anonymous: isAnonymous,
                tags,
                userId: user.id
            });
            onClose();
        } catch (error) {
            const msg = error instanceof Error ? error.message : "Błąd podczas edycji sugestii.";
            alert(`Nie udało się zapisać zmian: ${msg}`);
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4 animate-in fade-in">
            <div className="bg-background border border-secondarybg w-full max-w-lg rounded-2xl shadow-2xl overflow-hidden animate-in zoom-in-95">

                <div className="bg-surface p-4 border-b border-secondarybg flex justify-between items-center">
                    <h2 className="text-lg font-bold text-foreground flex items-center gap-2">
                        <Lightbulb className="w-5 h-5 text-secondary" /> Edytuj Sugestię
                    </h2>
                    <button onClick={onClose} className="text-txtcolor-300 hover:text-foreground">
                        <X className="w-5 h-5" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-6 space-y-5">
                    <div className="space-y-1">
                        <div className="flex justify-between items-center">
                            <label className="text-xs font-bold text-txtcolor-300 uppercase tracking-wider">Tytuł</label>
                            <span className={`text-[10px] ${title.length > TITLE_MAX_LENGTH ? 'text-error' : 'text-txtcolor-300'}`}>
                                {title.length}/{TITLE_MAX_LENGTH}
                            </span>
                        </div>
                        <input
                            type="text"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            maxLength={TITLE_MAX_LENGTH}
                            className="w-full bg-inputbg border border-secondarybg rounded-lg px-4 py-3 text-foreground focus:outline-none focus:border-secondary transition-colors"
                            required
                        />
                    </div>

                    <div className="space-y-1">
                        <div className="flex justify-between items-center">
                            <label className="text-xs font-bold text-txtcolor-300 uppercase tracking-wider">Opis</label>
                            <span className={`text-[10px] ${description.length > DESC_MAX_LENGTH ? 'text-error' : 'text-txtcolor-300'}`}>
                                {description.length}/{DESC_MAX_LENGTH}
                            </span>
                        </div>
                        <textarea
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            maxLength={DESC_MAX_LENGTH}
                            className="w-full h-32 bg-inputbg border border-secondarybg rounded-lg px-4 py-3 text-foreground focus:outline-none focus:border-secondary transition-colors resize-none"
                            required
                        />
                    </div>

                    <div className="space-y-1">
                        <label className="text-xs font-bold text-txtcolor-300 uppercase tracking-wider">Tagi (po przecinku)</label>
                        <input
                            type="text"
                            value={tagsInput}
                            onChange={(e) => setTagsInput(e.target.value)}
                            className="w-full bg-inputbg border border-secondarybg rounded-lg px-4 py-3 text-foreground focus:outline-none focus:border-secondary transition-colors"
                        />
                    </div>

                    <div className="flex items-center gap-3 bg-surface p-3 rounded-lg border border-secondarybg/50 cursor-pointer" onClick={() => setIsAnonymous(!isAnonymous)}>
                        <div className={`w-10 h-6 rounded-full p-1 transition-colors ${isAnonymous ? 'bg-secondary' : 'bg-darkgray'}`}>
                            <div className={`bg-white w-4 h-4 rounded-full shadow-md transform transition-transform ${isAnonymous ? 'translate-x-4' : ''}`} />
                        </div>
                        <div className="flex items-center gap-2">
                            <Lock className={`w-4 h-4 ${isAnonymous ? 'text-secondary' : 'text-txtcolor-300'}`} />
                            <span className={`text-sm font-medium ${isAnonymous ? 'text-foreground' : 'text-txtcolor-300'}`}>
                                Anonimowo
                            </span>
                        </div>
                    </div>

                    <button
                        type="submit"
                        disabled={isSubmitting}
                        className="w-full bg-primary text-darkgray hover:bg-secondary font-bold py-3 rounded-xl transition-all flex items-center justify-center gap-2"
                    >
                        {isSubmitting ? <Loader2 className="w-4 h-4 animate-spin"/> : <><Save className="w-4 h-4" /> Zapisz Zmiany</>}
                    </button>
                </form>
            </div>
        </div>
    );
}