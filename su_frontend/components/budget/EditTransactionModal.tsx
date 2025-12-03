'use client';

import { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { X, Save, Loader2, TrendingUp, TrendingDown } from 'lucide-react';
import { updateTransaction } from '@/lib/api/budget';
import { CouncilTransactionRequestDto, CouncilTransactionResponseDto } from '@/types/budget.types';
import FormField from '@/components/FormField';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    transaction: CouncilTransactionResponseDto;
}

const splitIsoDateTime = (isoString: string) => {
    const dateObj = new Date(isoString);

    const pad = (num: number) => num.toString().padStart(2, '0');

    const year = dateObj.getFullYear();
    const month = pad(dateObj.getMonth() + 1);
    const day = pad(dateObj.getDate());
    const datePart = `${year}-${month}-${day}`;

    const hours = pad(dateObj.getHours());
    const minutes = pad(dateObj.getMinutes());
    const timePart = `${hours}:${minutes}`;

    return { datePart, timePart };
};

export default function EditTransactionModal({ isOpen, onClose, transaction }: Props) {
    const queryClient = useQueryClient();

    const [description, setDescription] = useState('');
    const [amount, setAmount] = useState('');
    const [type, setType] = useState<'INCOME' | 'EXPENSE'>('EXPENSE');
    const [date, setDate] = useState('');
    const [time, setTime] = useState('');

    useEffect(() => {
        if (isOpen && transaction) {
            const { datePart, timePart } = splitIsoDateTime(transaction.date);
            setDescription(transaction.description);
            setAmount(transaction.amount.toString());
            setType(transaction.type);
            setDate(datePart);
            setTime(timePart);
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

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm p-4 animate-in fade-in duration-200">
            <div className="w-full max-w-md bg-background border border-border rounded-xl shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">

                <div className="flex justify-between items-center p-4 border-b border-border bg-secondarybg">
                    <h3 className="font-bold text-lg text-foreground">Edytuj Transakcję</h3>
                    <button onClick={onClose} className="text-txtcolor-300 hover:text-foreground transition-colors">
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-6 space-y-5">

                    <div className="flex gap-2 bg-inputbg p-1 rounded-lg">
                        <button
                            type="button"
                            onClick={() => setType('INCOME')}
                            className={`flex-1 py-2 rounded-md text-sm font-bold flex items-center justify-center gap-2 transition-all ${
                                type === 'INCOME'
                                    ? 'bg-success text-foreground shadow-md'
                                    : 'text-txtcolor-300 hover:text-foreground'
                            }`}
                            disabled={mutation.isPending}
                        >
                            <TrendingUp className="h-4 w-4" /> Wpływ
                        </button>
                        <button
                            type="button"
                            onClick={() => setType('EXPENSE')}
                            className={`flex-1 py-2 rounded-md text-sm font-bold flex items-center justify-center gap-2 transition-all ${
                                type === 'EXPENSE'
                                    ? 'bg-error text-foreground shadow-md'
                                    : 'text-txtcolor-300 hover:text-foreground'
                            }`}
                            disabled={mutation.isPending}
                        >
                            <TrendingDown className="h-4 w-4" /> Wydatek
                        </button>
                    </div>

                    <FormField
                        id="desc_edit" label="OPIS OPERACJI" type="text"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        placeholder=""
                        disabled={mutation.isPending}
                    />

                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                        <div className="sm:col-span-1">
                            <FormField
                                id="amount_edit" label="KWOTA" type="number"
                                value={amount}
                                onChange={(e) => setAmount(e.target.value)}
                                placeholder="0.00"
                                disabled={mutation.isPending}
                            />
                        </div>
                        <div className="sm:col-span-1">
                            <FormField
                                id="date_edit" label="DATA" type="date"
                                value={date}
                                onChange={(e) => setDate(e.target.value)}
                                placeholder=""
                                disabled={mutation.isPending}
                            />
                        </div>
                        <div className="sm:col-span-1">
                            <FormField
                                id="time_edit" label="GODZINA" type="time"
                                value={time}
                                onChange={(e) => setTime(e.target.value)}
                                placeholder=""
                                disabled={mutation.isPending}
                            />
                        </div>
                    </div>

                    <div className="flex justify-end pt-4 border-t border-border gap-3">
                        <button
                            type="button"
                            onClick={onClose}
                            disabled={mutation.isPending}
                            className="px-4 py-2 rounded-lg text-sm font-medium text-txtcolor-300 hover:bg-inputbg transition-colors"
                        >
                            Anuluj
                        </button>

                        <button
                            type="submit"
                            disabled={mutation.isPending}
                            className="bg-primary text-darkgray px-6 py-2.5 rounded-lg font-bold text-sm flex items-center gap-2 hover:opacity-90 disabled:opacity-50 transition-all"
                        >
                            {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
                            Zapisz
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}