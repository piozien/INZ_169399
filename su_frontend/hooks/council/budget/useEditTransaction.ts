import { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateTransaction } from '@/lib/api/budget';
import { CouncilTransactionRequestDto, CouncilTransactionResponseDto } from '@/types/budget.types';

export const useEditTransaction = (
    transaction: CouncilTransactionResponseDto,
    onClose: () => void,
    isOpen: boolean
) => {
    const queryClient = useQueryClient();

    const [description, setDescription] = useState('');
    const [amount, setAmount] = useState('');
    const [type, setType] = useState<'INCOME' | 'EXPENSE'>('EXPENSE');
    const [date, setDate] = useState('');
    const [time, setTime] = useState('');

    useEffect(() => {
        if (isOpen && transaction) {
            const [datePart, timePart] = transaction.date.split('T');
            setDescription(transaction.description);
            setAmount(transaction.amount.toString());
            setType(transaction.type);
            setDate(datePart);
            setTime(timePart ? timePart.slice(0, 5) : '00:00');
        }
    }, [isOpen, transaction]);

    const mutation = useMutation({
        mutationFn: (data: CouncilTransactionRequestDto) => updateTransaction(transaction.id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['budget'] });
            onClose();
        },
        onError: (err) => alert(err instanceof Error ? err.message : 'Błąd edycji'),
    });

    const changeAmount = (delta: number) => {
        const currentVal = parseFloat(amount) || 0;
        const newVal = Math.max(0, currentVal + delta);
        setAmount(newVal.toFixed(2));
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!amount || !description || !date || !time) return;

        mutation.mutate({
            budgetId: transaction.budgetId,
            description,
            amount: parseFloat(amount),
            type,
            date: `${date}T${time}:00`,
        });
    };

    return {
        description,
        setDescription,
        amount,
        setAmount,
        type,
        setType,
        date,
        setDate,
        time,
        setTime,
        changeAmount,
        handleSubmit,
        isPending: mutation.isPending,
    };
};
