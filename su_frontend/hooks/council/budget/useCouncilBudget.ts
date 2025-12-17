import {useState, useMemo} from 'react';
import {useQuery, useMutation, useQueryClient} from '@tanstack/react-query';
import {toast} from 'sonner';
import {
    fetchCouncilBudget,
    fetchBudgetTransactions,
    deleteTransaction,
    deleteBudget,
} from '@/lib/api/budget';
import {fetchCouncilContext, fetchCouncilById} from '@/lib/api/council';
import {CouncilBudgetResponseDto, CouncilTransactionResponseDto} from '@/types/budget.types';
import {CouncilResponseDto, CouncilContextDto} from '@/types/council.types';
import {useAuth} from '@/lib/contexts/AuthContext';

export const useCouncilBudget = (councilId: string) => {
    const {user} = useAuth();
    const queryClient = useQueryClient();

    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isEditBudgetModalOpen, setIsEditBudgetModalOpen] = useState(false);
    const [editingTransaction, setEditingTransaction] =
        useState<CouncilTransactionResponseDto | null>(null);

    const [isReportModalOpen, setIsReportModalOpen] = useState(false);
    const [reportType, setReportType] = useState<'pdf' | 'xlsx'>('pdf');


    const {data: council, isLoading: councilLoading} = useQuery<CouncilResponseDto>({
        queryKey: ['council', councilId],
        queryFn: () => fetchCouncilById(councilId),
    });

    const {data: budget, isLoading: budgetLoading} = useQuery<CouncilBudgetResponseDto>({
        queryKey: ['budget', councilId],
        queryFn: () => fetchCouncilBudget(councilId),
        retry: false,
    });

    const {data: transactions, isLoading: transLoading} = useQuery<
        CouncilTransactionResponseDto[]
    >({
        queryKey: ['budget', budget?.id, 'transactions'],
        queryFn: () => fetchBudgetTransactions(budget!.id),
        enabled: !!budget?.id,
    });

    const {data: context} = useQuery<CouncilContextDto>({
        queryKey: ['councilContext', councilId],
        queryFn: () => fetchCouncilContext(councilId),
    });


    const isAdmin = user?.roles?.includes('ADMINISTRATOR') || false;
    const isCouncilActive = council?.active ?? false;
    const isLocked = !isCouncilActive && !isAdmin;

    const permissions = useMemo(() => {
        const hasPerm = (perm: string) => {
            if (isAdmin) return true;
            if (budget?.myPermissions?.some((p) => p === 'ALL_ACCESS' || p === perm)) return true;
            if (context?.permissions?.some((p) => p === 'ALL_ACCESS' || p === perm)) return true;
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
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['budget']});
            toast.success('Transakcja usunięta', {description: 'Saldo zostało przeliczone.'});
        },
        onError: (err: any) =>
            toast.error('Błąd usuwania', {description: err.message}),
    });

    const deleteBudgetMutation = useMutation({
        mutationFn: deleteBudget,
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['budget']});
            toast.success('Budżet usunięty', {description: 'Rok budżetowy został zamknięty.'});
        },
        onError: (err: any) =>
            toast.error('Błąd usuwania budżetu', {description: err.message}),
    });

    const removeTransaction = (id: string) => {
        if (isLocked) {
            toast.error('Odmowa dostępu', {description: 'Samorząd jest archiwalny.'});
            return;
        }

        toast('Czy na pewno usunąć tę transakcję?', {
            description: 'Operacja jest nieodwracalna.',
            action: {
                label: 'Usuń',
                onClick: () => deleteTransMutation.mutate(id),
            },
            cancel: {
                label: 'Anuluj',
                onClick: () => {
                },
            },
        });
    };

    const removeBudget = () => {
        if (isLocked) {
            toast.error('Odmowa dostępu', {description: 'Samorząd jest archiwalny.'});
            return;
        }
        if (!budget?.id) return;

        toast('UWAGA: Usuwasz CAŁY ROK BUDŻETOWY!', {
            description: 'Wszystkie transakcje zostaną utracone bezpowrotnie. Kontynuować?',
            action: {
                label: 'Tak, usuń wszystko',
                onClick: () => deleteBudgetMutation.mutate(budget.id),
            },
            cancel: {
                label: 'Anuluj',
                onClick: () => {
                },
            },
            duration: 8000,
        });
    };

    const openReportModal = (type: 'pdf' | 'xlsx') => {
        setReportType(type);
        setIsReportModalOpen(true);
    };

    const closeReportModal = () => {
        setIsReportModalOpen(false);
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

        isReportModalOpen,
        reportType,
        openReportModal,
        closeReportModal,
    };
};