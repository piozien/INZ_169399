import { useQuery } from '@tanstack/react-query';
import { fetchUserLogs } from '@/lib/api/admin';

export const useUserLogs = (userId: string | null) => {
    return useQuery({
        queryKey: ['userLogs', userId],
        queryFn: () => fetchUserLogs(userId!),
        enabled: !!userId,
        staleTime: 0,
    });
};
