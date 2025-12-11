import { useState, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    fetchSuggestions,
    createSuggestion,
    deleteSuggestion,
    updateSuggestion,
} from '@/lib/api/suggestions';
import { useAuth } from '@/lib/contexts/AuthContext';
import { SuggestionDto, CreateSuggestionPayload } from '@/types/suggestions.types';

const TITLE_MAX_LENGTH = 100;
const DESC_MAX_LENGTH = 1000;

export const usePublicSuggestions = () => {
    const { user } = useAuth();
    const queryClient = useQueryClient();

    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [isAnonymous, setIsAnonymous] = useState(false);
    const [tagsInput, setTagsInput] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const [isHistoryOpen, setIsHistoryOpen] = useState(false);
    const [filter, setFilter] = useState<'ALL' | 'APPROVED' | 'PENDING' | 'REJECTED'>('ALL');
    const [searchQuery, setSearchQuery] = useState('');
    const [editingSuggestion, setEditingSuggestion] = useState<SuggestionDto | null>(null);
    const [selectedSuggestion, setSelectedSuggestion] = useState<SuggestionDto | null>(null);

    const { data: suggestions, isLoading } = useQuery({
        queryKey: ['suggestions'],
        queryFn: fetchSuggestions,
        enabled: !!user,
    });

    const { userSuggestions, displayedSuggestions } = useMemo(() => {
        const mySuggestions = suggestions?.filter((s) => s.userId === user?.id) || [];

        const filtered = mySuggestions
            .filter((s) => {
                const matchesStatus = filter === 'ALL' || s.status === filter;
                const textContent = `${s.title || ''} ${s.description || ''}`.toLowerCase();
                const matchesSearch = textContent.includes(searchQuery.toLowerCase());
                return matchesStatus && matchesSearch;
            })
            .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

        return { userSuggestions: mySuggestions, displayedSuggestions: filtered };
    }, [suggestions, user, filter, searchQuery]);

    const createMutation = useMutation({
        mutationFn: createSuggestion,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['suggestions'] });
            setTitle('');
            setDescription('');
            setTagsInput('');
            setIsAnonymous(false);
            alert('Twoja sugestia została wysłana!');
            setIsHistoryOpen(true);
        },
        onError: (error) =>
            alert(`Błąd: ${error instanceof Error ? error.message : 'Wystąpił błąd.'}`),
    });

    const updateMutation = useMutation({
        mutationFn: (variables: { id: string; data: Partial<CreateSuggestionPayload> }) =>
            updateSuggestion(variables.id, variables.data),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['suggestions'] }),
        onError: (error) =>
            alert(
                `Nie udało się zaktualizować: ${error instanceof Error ? error.message : 'Błąd'}`
            ),
    });

    const deleteMutation = useMutation({
        mutationFn: deleteSuggestion,
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['suggestions'] }),
        onError: (error) =>
            alert(`Nie udało się usunąć: ${error instanceof Error ? error.message : 'Błąd'}`),
    });

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!user?.id) return alert('Musisz być zalogowany.');
        if (title.length > TITLE_MAX_LENGTH) return alert('Tytuł za długi.');
        if (description.length > DESC_MAX_LENGTH) return alert('Opis za długi.');

        setIsSubmitting(true);
        try {
            const tags = tagsInput
                .split(',')
                .map((t) => t.trim())
                .filter((t) => t.length > 0);
            await createMutation.mutateAsync({
                title,
                description,
                anonymous: isAnonymous,
                tags,
                userId: user.id,
            });
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleEditSubmit = async (id: string, data: Partial<CreateSuggestionPayload>) => {
        if (!user?.id) return;
        const payload = { ...data, userId: user.id };
        await updateMutation.mutateAsync({ id, data: payload });
        setEditingSuggestion(null);
    };

    return {
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
        deleteSuggestion: deleteMutation.mutate,
    };
};
