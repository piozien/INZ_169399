import { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { updateBudget } from '@/lib/api/budget';
import { CouncilBudgetResponseDto, CouncilBudgetRequestDto } from '@/types/budget.types';

export const useEditBudget = (
    budget: CouncilBudgetResponseDto,
    onClose: () => void,
    isOpen: boolean
) => {
    const queryClient = useQueryClient();

    const [year, setYear] = useState(budget.year);
    const [initialAmount, setInitialAmount] = useState(budget.initialAmount.toString());

    useEffect(() => {
        if (isOpen) {
            setYear(budget.year);
            setInitialAmount(budget.initialAmount.toString());
        }
    }, [isOpen, budget]);

    const mutation = useMutation({
        mutationFn: () => {
            const payload: CouncilBudgetRequestDto = {
                year,
                initialAmount: parseFloat(initialAmount),
                councilId: budget.councilId,
            };
            return updateBudget(budget.id, payload);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['budget'] });
            toast.success('Zapisano zmiany', {
                description: 'Dane budżetu zostały zaktualizowane.',
            });
            onClose();
        },
        onError: (err: any) => {
            toast.error('Błąd edycji', { description: err.message });
        },
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        mutation.mutate();
    };

    return {
        year, setYear,
        initialAmount, setInitialAmount,
        handleSubmit,
        isPending: mutation.isPending,
    };
};