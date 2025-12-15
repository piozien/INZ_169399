import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createBudget } from '@/lib/api/budget';
import { CouncilBudgetRequestDto } from '@/types/budget.types';

export const useCreateBudget = (councilId: string, onClose: () => void) => {
    const queryClient = useQueryClient();

    const [initialAmount, setInitialAmount] = useState('0');
    const [year, setYear] = useState('2024/2025');

    const mutation = useMutation({
        mutationFn: () => {
            const payload: CouncilBudgetRequestDto = {
                initialAmount: parseFloat(initialAmount),
                year,
                councilId,
            };
            return createBudget(councilId, payload);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['budget'] });
            onClose();
        },
        onError: (err) => alert(err instanceof Error ? err.message : 'Błąd tworzenia budżetu'),
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        mutation.mutate();
    };

    return {
        initialAmount,
        setInitialAmount,
        year,
        setYear,
        handleSubmit,
        isPending: mutation.isPending,
    };
};
