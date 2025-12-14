import { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { addTransaction } from '@/lib/api/budget';
import { CouncilTransactionRequestDto } from '@/types/budget.types';

export const useAddTransaction = (
    budgetId: string,
    currentBalance: number,
    onClose: () => void,
    isOpen: boolean
) => {
    const queryClient = useQueryClient();

    const [description, setDescription] = useState('');
    const [amount, setAmount] = useState('');
    const [type, setType] = useState<'INCOME' | 'EXPENSE'>('EXPENSE');
    const [date, setDate] = useState('');
    const [time, setTime] = useState('');
    const [showDebitWarning, setShowDebitWarning] = useState(false);

    useEffect(() => {
        if (isOpen) {
            setShowDebitWarning(false);
            setDescription('');
            setAmount('');
            setType('EXPENSE');
            setDate(new Date().toISOString().split('T')[0]);
            setTime(new Date().toTimeString().slice(0, 5));
        }
    }, [isOpen]);

    const mutation = useMutation({
        mutationFn: (data: CouncilTransactionRequestDto) => addTransaction(budgetId, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['budget'] });
            toast.success('Transakcja dodana', {
                description: 'Saldo budżetu zostało zaktualizowane.',
            });
            onClose();
        },
        onError: (err: any) => {
            toast.error('Błąd dodawania transakcji', {
                description: err.message || 'Spróbuj ponownie później.',
            });
        },
    });

    const changeAmount = (delta: number) => {
        const currentVal = parseFloat(amount) || 0;
        const newVal = Math.max(0, currentVal + delta);
        setAmount(newVal.toFixed(2));
        setShowDebitWarning(false);
    };

    const changeType = (newType: 'INCOME' | 'EXPENSE') => {
        setType(newType);
        setShowDebitWarning(false);
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!amount || !description || !date || !time) return;

        const value = parseFloat(amount);

        if (type === 'EXPENSE' && value > currentBalance && !showDebitWarning) {
            setShowDebitWarning(true);
            return;
        }

        mutation.mutate({
            budgetId,
            description,
            amount: value,
            type,
            date: `${date}T${time}:00`,
        });
    };

    return {
        description, setDescription,
        amount, setAmount,
        type, changeType,
        date, setDate,
        time, setTime,
        showDebitWarning,
        changeAmount,
        handleSubmit,
        isPending: mutation.isPending,
    };
};