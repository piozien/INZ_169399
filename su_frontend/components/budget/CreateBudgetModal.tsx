'use client';

import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { X, Save, Loader2 } from 'lucide-react';
import { createBudget } from '@/lib/api/budget';
import { CouncilBudgetRequestDto } from '@/types/budget.types';
import FormField from '@/components/FormField';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    councilId: string;
}

export default function CreateBudgetModal({ isOpen, onClose, councilId }: Props) {
    const queryClient = useQueryClient();
    const [initialAmount, setInitialAmount] = useState('0');
    const [year, setYear] = useState('2024/2025');

    const mutation = useMutation({
        mutationFn: () => {
            const payload: CouncilBudgetRequestDto = {
                initialAmount: parseFloat(initialAmount),
                year,
                councilId
            };
            return createBudget(councilId, payload);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['budget'] });
            onClose();
        },
        onError: (err) => alert(err instanceof Error ? err.message : 'Błąd'),
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        mutation.mutate();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-background backdrop-blur-sm p-4 animate-in fade-in zoom-in-95 duration-200">
            <div className="w-full max-w-md bg-background/60 border border-border rounded-xl shadow-2xl overflow-hidden">
                <div className="flex justify-between items-center p-4 border-b border-border bg-secondarybg">
                    <h3 className="font-bold text-lg text-foreground">Otwórz Rok Budżetowy</h3>
                    <button onClick={onClose} className="text-txtcolor-300 hover:text-foreground transition-colors">
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-6 space-y-4">
                    <FormField
                        id="year"
                        label="Rok Budżetowy"
                        type="text"
                        value={year}
                        onChange={(e) => setYear(e.target.value)}
                        disabled={mutation.isPending}
                        placeholder="np. 2025/2026"
                    />
                    <FormField
                        id="amount"
                        label="Saldo Początkowe (PLN)"
                        type="number"
                        value={initialAmount}
                        onChange={(e) => setInitialAmount(e.target.value)}
                        disabled={mutation.isPending}
                        placeholder="0.00"
                    />

                    <div className="flex justify-end pt-4 border-t border-border mt-6">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2 mr-2 rounded-lg text-sm font-medium text-txtcolor-300 hover:bg-inputbg transition-colors"
                        >
                            Anuluj
                        </button>
                        <button
                            type="submit"
                            disabled={mutation.isPending}
                            className="bg-primary text-darkgray px-6 py-2 rounded-lg font-bold flex items-center gap-2 hover:opacity-90 disabled:opacity-50 transition-all"
                        >
                            {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
                            Utwórz
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}