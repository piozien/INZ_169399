import { useState, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import {
    fetchCouncilMembers,
    removeMemberFromCouncil,
    updateMemberRole,
    addMemberToCouncil,
    fetchCouncilContext,
} from '@/lib/api/council';
import { CouncilMemberDto, CouncilContextDto } from '@/types/council.types';

export const useCouncilMembers = (councilId: string) => {
    const queryClient = useQueryClient();

    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [editingMember, setEditingMember] = useState<{
        id: string;
        name: string;
        role: string;
    } | null>(null);

    const {
        data: members,
        isLoading: membersLoading,
        error,
    } = useQuery<CouncilMemberDto[]>({
        queryKey: ['councilMembers', councilId],
        queryFn: () => fetchCouncilMembers(councilId),
    });

    const { data: context, isLoading: contextLoading } = useQuery<CouncilContextDto>({
        queryKey: ['councilContext', councilId],
        queryFn: () => fetchCouncilContext(councilId),
    });

    const sortedMembers = useMemo(() => {
        if (!members) return [];
        const getRolePriority = (roleString: string) => {
            const role = roleString.toUpperCase();
            if (role.includes('OPIEKUN')) return 1;
            if (role.includes('PRZEWODNICZ')) return 2;
            if (role.includes('ZASTĘPCA') || role.includes('ZASTEPCA')) return 3;
            if (role.includes('SKARBNIK')) return 4;
            if (role.includes('CZŁONEK') || role.includes('CZLONEK')) return 5;
            if (role.includes('BYŁY') || role.includes('BYLY')) return 6;
            return 99;
        };
        return [...members].sort((a, b) => {
            const priorityA = getRolePriority(a.role);
            const priorityB = getRolePriority(b.role);
            if (priorityA !== priorityB) return priorityA - priorityB;
            return a.userFullName.localeCompare(b.userFullName);
        });
    }, [members]);

    const canManage =
        context?.permissions?.includes('COUNCIL_MEMBER_MANAGE') ||
        context?.permissions?.includes('ALL_ACCESS') ||
        false;

    const removeMutation = useMutation({
        mutationFn: (userId: string) => removeMemberFromCouncil(councilId, userId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['councilMembers', councilId] });
            toast.success('Członek usunięty');
        },
        onError: (err: any) =>
            toast.error('Błąd usuwania', { description: err.message }),
    });

    const updateRoleMutation = useMutation({
        mutationFn: ({ userId, newRole }: { userId: string; newRole: string }) =>
            updateMemberRole(councilId, userId, newRole),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['councilMembers', councilId] });
            setIsEditModalOpen(false);
            setEditingMember(null);
            toast.success('Rola zaktualizowana');
        },
        onError: (err: any) =>
            toast.error('Błąd edycji roli', { description: err.message }),
    });

    const addMutation = useMutation({
        mutationFn: ({ userId, roleCode }: { userId: string; roleCode: string }) =>
            addMemberToCouncil(councilId, userId, roleCode),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['councilMembers', councilId] });
            setIsAddModalOpen(false);
            toast.success('Członek dodany pomyślnie');
        },
        onError: (err: any) =>
            toast.error('Błąd dodawania', { description: err.message }),
    });

    const openEditModal = (userId: string) => {
        const member = members?.find((m) => m.userId === userId);
        if (member) {
            setEditingMember({ id: member.userId, name: member.userFullName, role: member.role });
            setIsEditModalOpen(true);
        }
    };

    const saveRole = (newRole: string) => {
        if (editingMember) {
            updateRoleMutation.mutate({ userId: editingMember.id, newRole });
        }
    };

    const removeMember = (userId: string) => {
        toast('Czy na pewno chcesz usunąć członka?', {
            description: 'Straci on dostęp do panelu samorządu.',
            action: {
                label: 'Usuń',
                onClick: () => removeMutation.mutate(userId),
            },
            cancel: {
                label: 'Anuluj',
                onClick: () => {},
            },
        });
    };

    return {
        members: sortedMembers,
        isLoading: membersLoading || contextLoading,
        error,
        canManage,
        isAddModalOpen,
        openAddModal: () => setIsAddModalOpen(true),
        closeAddModal: () => setIsAddModalOpen(false),
        addMember: (userId: string, roleCode: string) => addMutation.mutate({ userId, roleCode }),
        isAdding: addMutation.isPending,
        isEditModalOpen,
        closeEditModal: () => setIsEditModalOpen(false),
        openEditModal,
        saveRole,
        editingMember,
        isSaving: updateRoleMutation.isPending,
        removeMember,
    };
};