import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchPermissionMatrix, assignPermission, revokePermission } from '@/lib/api/admin';
import {toast} from "sonner";

export const usePermissions = () => {
    const queryClient = useQueryClient();

    const { data: matrix, isLoading } = useQuery({
        queryKey: ['permissionMatrix'],
        queryFn: fetchPermissionMatrix,
        staleTime: 1000 * 60 * 5,
    });

    const toggleMutation = useMutation({
        mutationFn: async ({
            role,
            perm,
            hasPerm,
        }: {
            role: string;
            perm: string;
            hasPerm: boolean;
        }) => {
            if (hasPerm) {
                await revokePermission(role, perm);
            } else {
                await assignPermission(role, perm);
            }
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['permissionMatrix'] });
        },
        onError: (error: any) => {
            toast.error('Błąd zmiany uprawnień', {
                description: error.message || 'Nieznany błąd',
            });
        },
    });

    return {
        matrix,
        isLoading,
        togglePermission: toggleMutation.mutate,
    };
};
