import { useState, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    fetchAllUsersAdmin,
    unblockUser,
    deleteUser,
    assignGlobalRole,
    removeGlobalRole,
    fetchAllGlobalRoles,
    updateUserAdmin,
} from '@/lib/api/admin';
import {UserUpdateRequestDto} from "@/types/user.types";

export const useAdminUsers = () => {
    const queryClient = useQueryClient();

    const [searchQuery, setSearchQuery] = useState('');
    const [roleFilter, setRoleFilter] = useState<string>('ALL');
    const [statusFilter, setStatusFilter] = useState<'ALL' | 'CONFIRMED' | 'BLOCKED'>('ALL');

    const { data: users, isLoading: isUsersLoading } = useQuery({
        queryKey: ['adminAllUsers'],
        queryFn: fetchAllUsersAdmin,
    });

    const { data: availableRoles = [] } = useQuery({
        queryKey: ['adminGlobalRoles'],
        queryFn: fetchAllGlobalRoles,
        staleTime: 1000 * 60 * 60,
    });

    const filteredUsers = useMemo(() => {
        if (!users) return [];
        return users.filter((u) => {
            const matchesSearch =
                u.fullName.toLowerCase().includes(searchQuery.toLowerCase()) ||
                u.email.toLowerCase().includes(searchQuery.toLowerCase());

            const matchesRole = roleFilter === 'ALL' || u.roles.includes(roleFilter);
            const matchesStatus = statusFilter === 'ALL' || u.status === statusFilter;

            return matchesSearch && matchesRole && matchesStatus;
        });
    }, [users, searchQuery, roleFilter, statusFilter]);

    const deleteMutation = useMutation({
        mutationFn: deleteUser,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['adminAllUsers'] });
            alert('Użytkownik usunięty (soft delete).');
        },
        onError: (err: any) => alert(err.message || 'Błąd usuwania.'),
    });

    const unblockMutation = useMutation({
        mutationFn: unblockUser,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['adminAllUsers'] });
            alert('Użytkownik odblokowany.');
        },
    });

    const assignRoleMutation = useMutation({
        mutationFn: ({ userId, role }: { userId: string; role: string }) =>
            assignGlobalRole(userId, role),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['adminAllUsers'] }),
        onError: (err: any) => alert(err.message || 'Błąd nadawania roli.'),
    });

    const removeRoleMutation = useMutation({
        mutationFn: ({ userId, role }: { userId: string; role: string }) =>
            removeGlobalRole(userId, role),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['adminAllUsers'] }),
        onError: (err: any) => alert(err.message || 'Błąd usuwania roli.'),
    });

    const updateMutation = useMutation({
        mutationFn: ({ userId, data }: { userId: string; data: UserUpdateRequestDto }) =>
            updateUserAdmin(userId, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['adminAllUsers'] });
        },
        onError: (err: any) => alert(err.message || 'Błąd aktualizacji użytkownika.'),
    });

    return {
        users: filteredUsers,
        availableRoles,
        isLoading: isUsersLoading,

        searchQuery,
        setSearchQuery,
        roleFilter,
        setRoleFilter,
        statusFilter,
        setStatusFilter,

        deleteUser: deleteMutation.mutate,
        unblockUser: unblockMutation.mutate,
        assignRole: assignRoleMutation.mutate,
        removeRole: removeRoleMutation.mutate,
        updateUser: updateMutation.mutate,
        isUpdating: updateMutation.isPending,
    };
};
