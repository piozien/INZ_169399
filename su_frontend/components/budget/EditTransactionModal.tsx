'use client';

import { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { X, Save, Loader2, TrendingUp, TrendingDown, ChevronUp, ChevronDown } from 'lucide-react';
import { updateTransaction } from '@/lib/api/budget';
import { CouncilTransactionRequestDto, CouncilTransactionResponseDto } from '@/types/budget.types';
import FormField from '@/components/FormField';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    transaction: CouncilTransactionResponseDto;
}

export default function EditTransactionModal({ isOpen, onClose, transaction }: Props) {
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
            setTime(timePart.slice(0, 5));
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

    const handleAmountChange = (delta: number) => {
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

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm p-4 animate-in fade-in duration-200">
            <div className="w-full max-w-lg bg-background border border-border rounded-xl shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">

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

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                        <div className="sm:col-span-2">
                            <div className="space-y-1">
                                <label htmlFor="amount_edit" className="block text-xs font-bold text-txtcolor-300 uppercase tracking-wider">
                                    KWOTA
                                </label>
                                <div className="relative">
                                    <input
                                        id="amount_edit"
                                        type="number"
                                        value={amount}
                                        onChange={(e) => setAmount(e.target.value)}
                                        disabled={mutation.isPending}
                                        placeholder="0.00"
                                        step="0.01"
                                        className="w-full bg-inputbg text-foreground border border-border rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all disabled:opacity-50 text-center"
                                    />
                                    <div className="absolute right-1 top-1 bottom-1 flex flex-col justify-center gap-0.5">
                                        <button
                                            type="button"
                                            onClick={() => handleAmountChange(1)}
                                            className="p-0.5 hover:bg-white/10 rounded text-txtcolor-300 hover:text-primary transition-colors h-1/2 flex items-center"
                                            tabIndex={-1}
                                        >
                                            <ChevronUp className="h-4 w-4" />
                                        </button>
                                        <button
                                            type="button"
                                            onClick={() => handleAmountChange(-1)}
                                            className="p-0.5 hover:bg-white/10 rounded text-txtcolor-300 hover:text-error transition-colors h-1/2 flex items-center"
                                            tabIndex={-1}
                                        >
                                            <ChevronDown className="h-4 w-4" />
                                        </button>
                                    </div>
                                </div>
                            </div>
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