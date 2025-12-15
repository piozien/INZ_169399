import { useState, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import {
    fetchCouncilSuggestions,
    approveSuggestion,
    rejectSuggestion,
    deleteSuggestion,
} from '@/lib/api/suggestions';
import { fetchCouncilById } from '@/lib/api/council';
import { SuggestionDto } from '@/types/suggestions.types';

export const useCouncilSuggestions = (councilId: string) => {
    const queryClient = useQueryClient();

    const [filter, setFilter] = useState<'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED'>('PENDING');
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedSuggestion, setSelectedSuggestion] = useState<SuggestionDto | null>(null);

    const { data: councilSuggestions, isLoading: suggestionsLoading } = useQuery({
        queryKey: ['councilSuggestions', councilId],
        queryFn: () => fetchCouncilSuggestions(councilId || ''),
        enabled: !!councilId,
    });

    const { data: council, isLoading: councilLoading } = useQuery({
        queryKey: ['council', councilId],
        queryFn: () => fetchCouncilById(councilId || ''),
        enabled: !!councilId,
    });

    const permissions = useMemo(() => {
        if (!council?.myPermissions)
            return { canApprove: false, canReject: false, canDelete: false };
        const perms = council.myPermissions;
        const hasAll = perms.includes('ALL_ACCESS');
        return {
            canApprove: hasAll || perms.includes('SUGGESTION_APPROVE'),
            canReject: hasAll || perms.includes('SUGGESTION_DELETE'),
            canDelete: hasAll || perms.includes('SUGGESTION_DELETE'),
        };
    }, [council]);

    const approveMutation = useMutation({
        mutationFn: approveSuggestion,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['councilSuggestions', councilId] });
            setSelectedSuggestion(null);
            toast.success('Sugestia zatwierdzona');
        },
        onError: (err: any) =>
            toast.error('Błąd zatwierdzania', { description: err.message || 'Nieznany błąd' }),
    });

    const rejectMutation = useMutation({
        mutationFn: (vars: { id: string; reason: string }) =>
            rejectSuggestion(vars.id, vars.reason),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['councilSuggestions', councilId] });
            setSelectedSuggestion(null);
            toast.success('Sugestia odrzucona');
        },
        onError: (err: any) =>
            toast.error('Błąd odrzucania', { description: err.message || 'Nieznany błąd' }),
    });

    const deleteMutation = useMutation({
        mutationFn: deleteSuggestion,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['councilSuggestions', councilId] });
            toast.success('Sugestia usunięta');
        },
        onError: (err: any) =>
            toast.error('Błąd usuwania', { description: err.message || 'Nieznany błąd' }),
    });

    const displayedSuggestions = useMemo(() => {
        if (!councilSuggestions) return [];

        return councilSuggestions
            .filter((s) => {
                const matchesStatus = filter === 'ALL' || s.status === filter;
                const textContent = `${s.title || ''} ${s.description || ''}`.toLowerCase();
                const matchesSearch = textContent.includes(searchQuery.toLowerCase());
                return matchesStatus && matchesSearch;
            })
            .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
    }, [councilSuggestions, filter, searchQuery]);

    return {
        suggestions: displayedSuggestions,
        isLoading: suggestionsLoading || councilLoading,
        permissions,

        filter, setFilter,
        searchQuery, setSearchQuery,

        selectedSuggestion, setSelectedSuggestion,

        approve: approveMutation.mutate,
        reject: (id: string, reason: string) => rejectMutation.mutate({ id, reason }),
        remove: deleteMutation.mutate,
    };
};