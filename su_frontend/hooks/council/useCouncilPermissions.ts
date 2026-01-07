import { useQuery } from '@tanstack/react-query';
import { fetchMyPermissions } from '@/lib/api/permissions';

export const useCouncilPermissions = (councilId?: string) => {
    const { data: permissionsData, isLoading } = useQuery({
        queryKey: ['permissions', councilId],
        queryFn: () => fetchMyPermissions(councilId),
        enabled: !!councilId,
    });

    const hasPermission = (perm: string) => {
        if (!permissionsData?.permissions) return false;
        return (
            permissionsData.permissions.includes('ALL_ACCESS') ||
            permissionsData.permissions.includes(perm)
        );
    };

    return {
        permissions: permissionsData?.permissions || [],
        roles: permissionsData?.roles || [],
        hasPermission,
        isLoading,
    };
};
