import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import {deleteCouncil, fetchCouncilById, leaveCouncil} from '@/lib/api/council';
import { useAuth } from '@/lib/contexts/AuthContext';
import { CouncilResponseDto } from '@/types/council.types';

export const useCouncilDetails = (councilId: string) => {
    const router = useRouter();
    const { user } = useAuth();
    const queryClient = useQueryClient();

    const [copied, setCopied] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);

    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

    const {
        data: council,
        isLoading,
        error,
    } = useQuery<CouncilResponseDto>({
        queryKey: ['council', councilId],
        queryFn: () => fetchCouncilById(councilId),
        retry: 1,
    });

    const leaveMutation = useMutation({
        mutationFn: () => {
            if (!user?.id) throw new Error('Brak użytkownika');
            return leaveCouncil(councilId, user.id);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['userCouncils'] });
            router.push('/dashboard');
        },
        onError: (err) => {
            alert('Nie udało się opuścić samorządu. ' + (err instanceof Error ? err.message : ''));
        },
    });

    const handleLeave = () => {
        if (
            confirm(
                `Czy na pewno chcesz opuścić samorząd "${council?.name}"? Ta operacja jest nieodwracalna.`
            )
        ) {
            leaveMutation.mutate();
        }
    };

    const deleteMutation = useMutation({
        mutationFn: () => deleteCouncil(councilId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['userCouncils'] });
            setIsDeleteModalOpen(false);
            router.push('/dashboard');
            router.refresh();
        },
        onError: (err) => {
            alert('Nie udało się usunąć samorządu. ' + (err instanceof Error ? err.message : ''));
        }
    });

    const hasPermission = (perm: string) => {
        if (!council?.myPermissions) return false;
        return council.myPermissions.includes('ALL_ACCESS') || council.myPermissions.includes(perm);
    };

    const copyJoinCode = () => {
        if (council?.joinCode) {
            navigator.clipboard.writeText(council.joinCode);
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
        }
    };

    return {
        council,
        isLoading,
        error,
        handleLeave,
        isLeaving: leaveMutation.isPending,
        hasPermission,
        copyJoinCode,
        isCopied: copied,
        isEditModalOpen,
        openEditModal: () => setIsEditModalOpen(true),
        closeEditModal: () => setIsEditModalOpen(false),

        removeCouncil: () => deleteMutation.mutate(),
        isDeleting: deleteMutation.isPending,
        isDeleteModalOpen,
        openDeleteModal: () => setIsDeleteModalOpen(true),
        closeDeleteModal: () => setIsDeleteModalOpen(false),
    };
};
