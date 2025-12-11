import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchAvailableCouncilRoles } from '@/lib/api/council';
import { RoleOptionDto } from '@/types/council.types';

export const useEditRoleModal = (
    isOpen: boolean,
    currentRole: string,
    onSave: (roleCode: string) => void
) => {
    const [selectedRole, setSelectedRole] = useState(currentRole);

    const {
        data: roles,
        isLoading,
        error,
    } = useQuery<RoleOptionDto[]>({
        queryKey: ['councilRoles'],
        queryFn: fetchAvailableCouncilRoles,
        enabled: isOpen,
        staleTime: 1000 * 60 * 60 * 24,
    });

    useEffect(() => {
        if (isOpen) {
            setSelectedRole(currentRole);
        }
    }, [isOpen, currentRole]);

    const handleSave = () => {
        onSave(selectedRole);
    };

    return {
        selectedRole,
        setSelectedRole,
        roles,
        isLoading,
        error,
        handleSave,
    };
};
