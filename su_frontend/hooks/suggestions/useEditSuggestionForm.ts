import { useState, useEffect } from 'react';
import { toast } from 'sonner';
import { useAuth } from '@/lib/contexts/AuthContext';
import { SuggestionDto, CreateSuggestionPayload } from '@/types/suggestions.types';

const TITLE_MAX_LENGTH = 100;
const DESC_MAX_LENGTH = 1000;

export const useEditSuggestionForm = (
    suggestion: SuggestionDto,
    isOpen: boolean,
    onClose: () => void,
    onSubmit: (id: string, data: Partial<CreateSuggestionPayload>) => Promise<void>
) => {
    const { user } = useAuth();

    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [isAnonymous, setIsAnonymous] = useState(false);
    const [tagsInput, setTagsInput] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (isOpen && suggestion) {
            setTitle(suggestion.title);
            setDescription(suggestion.description);
            setIsAnonymous(suggestion.anonymous);
            setTagsInput((suggestion.tags || []).join(', '));
        }
    }, [isOpen, suggestion]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!title || !description) return;
        if (!user?.id) {
            toast.error('Błąd: Nie rozpoznano użytkownika.');
            return;
        }
        if (title.length > TITLE_MAX_LENGTH) {
            toast.error(`Tytuł jest za długi (max ${TITLE_MAX_LENGTH} znaków).`);
            return;
        }
        if (description.length > DESC_MAX_LENGTH) {
            toast.error(`Opis jest za długi (max ${DESC_MAX_LENGTH} znaków).`);
            return;
        }

        setIsSubmitting(true);
        try {
            const tags = tagsInput
                .split(',')
                .map((t) => t.trim())
                .filter((t) => t.length > 0);

            await onSubmit(suggestion.id, {
                title,
                description,
                anonymous: isAnonymous,
                tags,
                userId: user.id,
            });
            onClose();
        } catch (error) {
            const msg = error instanceof Error ? error.message : 'Błąd podczas edycji sugestii.';
            toast.error('Nie udało się zapisać zmian', { description: msg });
        } finally {
            setIsSubmitting(false);
        }
    };

    return {
        title, setTitle,
        description, setDescription,
        isAnonymous, setIsAnonymous,
        tagsInput, setTagsInput,
        isSubmitting,
        handleSubmit,
        TITLE_MAX_LENGTH,
        DESC_MAX_LENGTH,
    };
};