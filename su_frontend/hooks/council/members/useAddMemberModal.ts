import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchAvailableCouncilRoles } from '@/lib/api/council';
import { RoleOptionDto } from '@/types/council.types';

export const useAddMemberModal = (
    isOpen: boolean,
    onAdd: (userId: string, roleCode: string) => void
) => {
    const [userId, setUserId] = useState('');
    const [selectedRole, setSelectedRole] = useState('CZLONEK_SU');

    useEffect(() => {
        if (isOpen) {
            setUserId('');
            setSelectedRole('CZLONEK_SU');
        }
    }, [isOpen]);

    const { data: roles, isLoading } = useQuery<RoleOptionDto[]>({
        queryKey: ['councilRoles'],
        queryFn: fetchAvailableCouncilRoles,
        enabled: isOpen,
        staleTime: 1000 * 60 * 60 * 24,
    });

    const handleSubmit = () => {
        if (userId && selectedRole) {
            onAdd(userId, selectedRole);
        }
    };

    return {
        userId,
        setUserId,
        selectedRole,
        setSelectedRole,
        roles,
        isLoadingRoles: isLoading,
        handleSubmit,
    };
};
