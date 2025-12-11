import { useState, useMemo } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import {
    fetchCouncilBudget,
    fetchBudgetTransactions,
    deleteTransaction,
    deleteBudget,
} from '@/lib/api/budget';
import { fetchCouncilContext, fetchCouncilById } from '@/lib/api/council';
import { CouncilBudgetResponseDto, CouncilTransactionResponseDto } from '@/types/budget.types';
import { CouncilResponseDto, CouncilContextDto } from '@/types/council.types';
import { useAuth } from '@/lib/contexts/AuthContext';
import { generateBudgetPdf, generateBudgetExcel } from '@/lib/reports/budgetReport';

export const useCouncilBudget = (councilId: string) => {
    const { user } = useAuth();
    const router = useRouter();
    const queryClient = useQueryClient();

    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isEditBudgetModalOpen, setIsEditBudgetModalOpen] = useState(false);
    const [editingTransaction, setEditingTransaction] =
        useState<CouncilTransactionResponseDto | null>(null);

    const { data: council, isLoading: councilLoading } = useQuery<CouncilResponseDto>({
        queryKey: ['council', councilId],
        queryFn: () => fetchCouncilById(councilId),
    });

    const { data: budget, isLoading: budgetLoading } = useQuery<CouncilBudgetResponseDto>({
        queryKey: ['budget', councilId],
        queryFn: () => fetchCouncilBudget(councilId),
        retry: false,
    });

    const { data: transactions, isLoading: transLoading } = useQuery<
        CouncilTransactionResponseDto[]
    >({
        queryKey: ['budget', budget?.id, 'transactions'],
        queryFn: () => fetchBudgetTransactions(budget!.id),
        enabled: !!budget?.id,
    });

    const { data: context } = useQuery<CouncilContextDto>({
        queryKey: ['councilContext', councilId],
        queryFn: () => fetchCouncilContext(councilId),
    });

    const isAdmin = user?.roles?.includes('ADMINISTRATOR') || false;
    const isCouncilActive = council?.active ?? false;
    const isLocked = !isCouncilActive && !isAdmin;

    const permissions = useMemo(() => {
        const hasPerm = (perm: string) => {
            if (isAdmin) return true;
            if (
                budget?.myPermissions &&
                (budget.myPermissions.includes('ALL_ACCESS') || budget.myPermissions.includes(perm))
            )
                return true;
            if (
                context?.permissions &&
                (context.permissions.includes('ALL_ACCESS') || context.permissions.includes(perm))
            )
                return true;
            return false;
        };

        return {
            canEditTransactions:
                hasPerm('COUNCIL_TRANSACTION_EDIT') || hasPerm('COUNCIL_TRANSACTION_CREATE'),
            canDeleteTransaction: hasPerm('COUNCIL_TRANSACTION_DELETE'),
            canEditBudget: hasPerm('COUNCIL_BUDGET_EDIT'),
            canDeleteBudget: hasPerm('COUNCIL_BUDGET_DELETE'),
            canCreateBudget: hasPerm('COUNCIL_BUDGET_CREATE'),
        };
    }, [isAdmin, budget, context]);

    const deleteTransMutation = useMutation({
        mutationFn: deleteTransaction,
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['budget'] }),
        onError: (err) => alert(err instanceof Error ? err.message : 'Błąd usuwania transakcji'),
    });

    const deleteBudgetMutation = useMutation({
        mutationFn: deleteBudget,
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['budget'] }),
        onError: (err) => alert(err instanceof Error ? err.message : 'Błąd usuwania budżetu'),
    });

    const removeTransaction = (id: string) => {
        if (isLocked) return alert('Samorząd jest archiwalny. Nie można usuwać transakcji.');
        if (confirm('Czy na pewno chcesz usunąć tę transakcję? Saldo zostanie przeliczone.')) {
            deleteTransMutation.mutate(id);
        }
    };

    const removeBudget = () => {
        if (isLocked) return alert('Samorząd jest archiwalny. Nie można usunąć budżetu.');
        if (
            budget?.id &&
            confirm(
                'UWAGA! Czy na pewno chcesz usunąć CAŁY ROK BUDŻETOWY? Wszystkie transakcje zostaną utracone bezpowrotnie!'
            )
        ) {
            deleteBudgetMutation.mutate(budget.id);
        }
    };

    const downloadReport = (format: 'pdf' | 'xlsx') => {
        if (!budget || !transactions) return;
        if (format === 'pdf') generateBudgetPdf(budget, transactions);
        else generateBudgetExcel(budget, transactions);
    };

    return {
        council,
        budget,
        transactions,
        isLoading: budgetLoading || councilLoading || transLoading,

        isCouncilActive,
        isLocked,
        isAdmin,
        permissions,

        removeTransaction,
        removeBudget,
        downloadReport,

        isAddModalOpen,
        openAddModal: () => setIsAddModalOpen(true),
        closeAddModal: () => setIsAddModalOpen(false),

        isCreateModalOpen,
        openCreateModal: () => setIsCreateModalOpen(true),
        closeCreateModal: () => setIsCreateModalOpen(false),

        isEditBudgetModalOpen,
        openEditBudgetModal: () => setIsEditBudgetModalOpen(true),
        closeEditBudgetModal: () => setIsEditBudgetModalOpen(false),

        editingTransaction,
        openEditTransactionModal: (t: CouncilTransactionResponseDto) => setEditingTransaction(t),
        closeEditTransactionModal: () => setEditingTransaction(null),
    };
};
