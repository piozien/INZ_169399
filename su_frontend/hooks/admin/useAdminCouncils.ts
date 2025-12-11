import { useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
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
            alert('Samorząd został usunięty.');
        },
        onError: (error: any) => {
            alert('Nie udało się usunąć samorządu: ' + (error.message || 'Błąd'));
        },
    });

    return {
        councils: sortedCouncils,
        isLoading,
        deleteCouncil: deleteMutation.mutate,
    };
};
