import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { fetchUserCouncils, joinCouncilByCode } from '@/lib/api/council';
import { CouncilResponseDto } from '@/types/council.types';
import { ApiError } from '@/types/error.types';

export const useCouncilList = () => {
    const queryClient = useQueryClient();
    const [joinError, setJoinError] = useState<string | null>(null);

    const {
        data: councils,
        isLoading,
        error,
    } = useQuery<CouncilResponseDto[]>({
        queryKey: ['userCouncils'],
        queryFn: fetchUserCouncils,
        retry: false,
    });

    const activeCouncils = councils?.filter((c) => c.active) || [];
    const archiveCouncils =
        councils
            ?.filter((c) => !c.active)
            .sort((a, b) => new Date(b.endDate).getTime() - new Date(a.endDate).getTime()) || [];

    const hasNoCouncils = !councils || councils.length === 0;

    const joinMutation = useMutation({
        mutationFn: joinCouncilByCode,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['userCouncils'] });
            setJoinError(null);
            toast.success('Dołączono do samorządu!');
        },
        onError: (err: any) => {
            const message = err instanceof ApiError ? err.message : 'Wystąpił błąd podczas dołączania.';
            setJoinError(message);
            toast.error('Błąd dołączania', { description: message });
        },
    });

    const joinCouncil = async (code: string) => {
        setJoinError(null);
        if (code.trim()) {
            await joinMutation.mutateAsync(code.trim());
        }
    };

    return {
        councils,
        activeCouncils,
        archiveCouncils,
        hasNoCouncils,
        isLoading,
        error,
        joinCouncil,
        isJoining: joinMutation.isPending,
        joinError,
        clearJoinError: () => setJoinError(null),
    };
};