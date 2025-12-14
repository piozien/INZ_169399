import { useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {toast} from "sonner";
import { fetchAllCouncilsAdmin, deleteCouncilAdmin } from '@/lib/api/admin';

export const useAdminCouncils = () => {
    const queryClient = useQueryClient();

    const { data: councils, isLoading } = useQuery({
        queryKey: ['adminCouncils'],
        queryFn: fetchAllCouncilsAdmin,
    });

    const sortedCouncils = useMemo(() => {
        if (!councils) return [];

        return [...councils].sort((a: any, b: any) => {
            const dateA = new Date(a.startDate).getTime();
            const dateB = new Date(b.startDate).getTime();
            return dateB - dateA;
        });
    }, [councils]);

    const deleteMutation = useMutation({
        mutationFn: deleteCouncilAdmin,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['adminCouncils'] });
            toast.success(`Samorząd został usunięty.`);
        },
        onError: (error: any) => {
            toast.error('Nie udało się usunąć samorządu', {
                description: error.message || 'Nie udało się usunąć samorządu',
            });
        },
    });

    return {
        councils: sortedCouncils,
        isLoading,
        deleteCouncil: deleteMutation.mutate,
    };
};
