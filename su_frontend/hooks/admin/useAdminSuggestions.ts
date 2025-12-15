import { useState, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchAllSuggestionsAdmin, deleteSuggestionAdmin } from '@/lib/api/admin';
import {toast} from "sonner";

export const useAdminSuggestions = () => {
    const queryClient = useQueryClient();

    const [searchQuery, setSearchQuery] = useState('');
    const [statusFilter, setStatusFilter] = useState<'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED'>(
        'ALL'
    );

    const { data: suggestions, isLoading } = useQuery({
        queryKey: ['adminAllSuggestions'],
        queryFn: fetchAllSuggestionsAdmin,
    });

    const filteredSuggestions = useMemo(() => {
        if (!suggestions) return [];

        return suggestions
            .filter((s) => {
                const matchesSearch =
                    s.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                    s.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
                    (s.fullName || '').toLowerCase().includes(searchQuery.toLowerCase());

                const matchesStatus = statusFilter === 'ALL' || s.status === statusFilter;

                return matchesSearch && matchesStatus;
            })
            .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    }, [suggestions, searchQuery, statusFilter]);

    const deleteMutation = useMutation({
        mutationFn: deleteSuggestionAdmin,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['adminAllSuggestions'] });
        },
        onError: (error: any) => {
            toast.error('Nie udało się usunąć sugestii', {
                description: error.message || 'Nie udało się usunąć sugestii',
            });
        },
    });

    return {
        suggestions: filteredSuggestions,
        isLoading,
        searchQuery,
        setSearchQuery,
        statusFilter,
        setStatusFilter,
        deleteSuggestion: deleteMutation.mutate,
    };
};
