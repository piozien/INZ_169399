import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { fetchDashboardSummary } from '@/lib/api/dashboard';
import { joinCouncilByCode } from '@/lib/api/council';
import { useAuth } from '@/lib/contexts/AuthContext';

export const useDashboard = () => {
    const { user } = useAuth();
    const queryClient = useQueryClient();

    const [joinCode, setJoinCode] = useState('');

    const { data: summary, isLoading } = useQuery({
        queryKey: ['dashboardSummary'],
        queryFn: fetchDashboardSummary,
        staleTime: 0,
        refetchOnMount: true,
    });

    const joinMutation = useMutation({
        mutationFn: joinCouncilByCode,
        onSuccess: () => {
            toast.success('Sukces!', { description: 'Dołączono do samorządu.' });
            queryClient.invalidateQueries({ queryKey: ['dashboardSummary'] });
            setJoinCode('');
        },
        onError: (err) =>
            toast.error('Błąd dołączania', {
                description: err instanceof Error ? err.message : 'Nieznany błąd',
            }),
    });

    const handleJoin = (e: React.FormEvent) => {
        e.preventDefault();
        if (joinCode.trim()) joinMutation.mutate(joinCode.trim());
    };

    const firstName = user?.fullName?.split(' ')[0] || 'Uczniu';
    const isMember = !!(summary?.councilMember && summary.activeCouncilId);

    return {
        user,
        summary,
        isLoading,
        joinCode, setJoinCode,
        handleJoin,
        isJoining: joinMutation.isPending,
        firstName,
        isMember,
    };
};