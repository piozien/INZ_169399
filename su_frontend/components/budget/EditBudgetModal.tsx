'use client';

import { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { X, Save, Loader2 } from 'lucide-react';
import { updateBudget } from '@/lib/api/budget';
import { CouncilBudgetResponseDto, CouncilBudgetRequestDto } from '@/types/budget.types';
import FormField from '@/components/FormField';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    budget: CouncilBudgetResponseDto;
}

export default function EditBudgetModal({ isOpen, onClose, budget }: Props) {
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
                councilId: budget.councilId
            };
            return updateBudget(budget.id, payload);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['budget'] });
            onClose();
        },
        onError: (err) => alert(err instanceof Error ? err.message : 'Błąd edycji'),
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        mutation.mutate();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm p-4 animate-in fade-in duration-200">
            <div className="w-full max-w-md bg-background border border-border rounded-xl shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">

                <div className="flex justify-between items-center p-4 border-b border-border bg-secondarybg">
                    <h3 className="font-bold text-lg text-foreground">Edytuj Ustawienia Budżetu</h3>
                    <button onClick={onClose} className="text-txtcolor-300 hover:text-foreground transition-colors">
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-6 space-y-4">
                    <FormField
                        id="year" label="Rok Budżetowy" type="text"
                        value={year} onChange={(e) => setYear(e.target.value)}
                        disabled={mutation.isPending}
                        placeholder="np. 2025/2026"
                    />
                    <FormField
                        id="amount" label="Saldo Początkowe (PLN)" type="number"
                        value={initialAmount} onChange={(e) => setInitialAmount(e.target.value)}
                        disabled={mutation.isPending}
                        placeholder="0.00"
                    />

                    <p className="text-xs text-warning bg-warning/10 p-3 rounded-lg border border-warning/20 font-medium">
                        Uwaga: Zmiana salda początkowego spowoduje automatyczne przeliczenie aktualnego stanu konta!
                    </p>

                    <div className="flex justify-end pt-4 border-t border-border gap-3 mt-6">
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