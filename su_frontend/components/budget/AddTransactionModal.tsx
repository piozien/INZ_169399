'use client';

import { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { X, Plus, Loader2, TrendingUp, TrendingDown, AlertTriangle } from 'lucide-react';
import { addTransaction } from '@/lib/api/budget';
import { CouncilTransactionRequestDto } from '@/types/budget.types';
import FormField from '@/components/FormField';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    budgetId: string;
    currentBalance: number;
}

const getTodayDate = () => new Date().toISOString().split('T')[0];
const getCurrentTime = () => new Date().toTimeString().slice(0, 5);

export default function AddTransactionModal({ isOpen, onClose, budgetId, currentBalance }: Props) {
    const queryClient = useQueryClient();

    const [description, setDescription] = useState('');
    const [amount, setAmount] = useState('');
    const [type, setType] = useState<'INCOME' | 'EXPENSE'>('EXPENSE');

    const [date, setDate] = useState(getTodayDate());
    const [time, setTime] = useState(getCurrentTime());

    const [showDebitWarning, setShowDebitWarning] = useState(false);

    useEffect(() => {
        if (isOpen) {
            setShowDebitWarning(false);
            setDescription('');
            setAmount('');
            setType('EXPENSE');
            setDate(getTodayDate());
            setTime(getCurrentTime());
        }
    }, [isOpen]);

    const mutation = useMutation({
        mutationFn: (data: CouncilTransactionRequestDto) => addTransaction(budgetId, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['budget'] });
            onClose();
        },
        onError: (err) => alert(err instanceof Error ? err.message : 'Błąd dodawania transakcji'),
    });

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

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm p-4 animate-in fade-in duration-200">
            <div className="w-full max-w-md bg-background border border-border rounded-xl shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">

                <div className="flex justify-between items-center p-4 border-b border-border bg-secondarybg">
                    <h3 className="font-bold text-lg text-foreground">Dodaj Transakcję</h3>
                    <button onClick={onClose} className="text-txtcolor-300 hover:text-foreground transition-colors">
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-6 space-y-5">

                    {showDebitWarning && (
                        <div className="bg-error/10 border border-error/20 p-4 rounded-lg flex gap-3 items-start animate-in slide-in-from-top-2">
                            <AlertTriangle className="h-5 w-5 text-error shrink-0 mt-0.5" />
                            <div>
                                <h4 className="text-sm font-bold text-error">Uwaga: Powstanie debet!</h4>
                                <p className="text-xs text-txtcolor-300 mt-1">
                                    Ta transakcja przekracza dostępne środki ({currentBalance.toFixed(2)} PLN).
                                </p>
                                <p className="text-xs font-bold text-error mt-2">Czy na pewno chcesz kontynuować?</p>
                            </div>
                        </div>
                    )}

                    <div className="flex gap-2 bg-inputbg p-1 rounded-lg">
                        <button
                            type="button"
                            onClick={() => { setType('INCOME'); setShowDebitWarning(false); }}
                            className={`flex-1 py-2 rounded-md text-sm font-bold flex items-center justify-center gap-2 transition-all ${type === 'INCOME' ? 'bg-success text-foreground shadow-md' : 'text-txtcolor-300 hover:text-foreground'}`}
                            disabled={mutation.isPending}
                        >
                            <TrendingUp className="h-4 w-4" /> Wpływ
                        </button>
                        <button
                            type="button"
                            onClick={() => setType('EXPENSE')}
                            className={`flex-1 py-2 rounded-md text-sm font-bold flex items-center justify-center gap-2 transition-all ${type === 'EXPENSE' ? 'bg-error text-foreground shadow-md' : 'text-txtcolor-300 hover:text-foreground'}`}
                            disabled={mutation.isPending}
                        >
                            <TrendingDown className="h-4 w-4" /> Wydatek
                        </button>
                    </div>

                    <FormField
                        id="desc" label="OPIS OPERACJI" type="text"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        placeholder="np. Zakup papieru"
                        disabled={mutation.isPending}
                    />

                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                        <div className="sm:col-span-1">
                            <FormField
                                id="amount" label="KWOTA (PLN)" type="number"
                                value={amount}
                                onChange={(e) => { setAmount(e.target.value); setShowDebitWarning(false); }}
                                placeholder="0.00"
                                disabled={mutation.isPending}
                            />
                        </div>
                        <div className="sm:col-span-1">
                            <FormField
                                id="date" label="DATA" type="date"
                                value={date}
                                onChange={(e) => setDate(e.target.value)}
                                placeholder=""
                                disabled={mutation.isPending}
                            />
                        </div>
                        <div className="sm:col-span-1">
                            <FormField
                                id="time" label="GODZINA" type="time"
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
                            className={`
                    px-6 py-2.5 rounded-lg font-bold text-sm flex items-center gap-2 hover:opacity-90 disabled:opacity-50 transition-all text-foreground
                    ${showDebitWarning ? 'bg-error hover:error/10' : 'bg-primary text-darkgray'}
                `}
                        >
                            {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : (showDebitWarning ? 'Zatwierdź Debet' : <><Plus className="h-4 w-4"/> Zatwierdź</>)}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}