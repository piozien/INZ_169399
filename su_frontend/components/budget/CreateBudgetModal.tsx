'use client';

import { X, Save, Loader2 } from 'lucide-react';
import FormField from '@/components/FormField';
import { useCreateBudget } from '@/hooks/council/budget/useCreateBudget';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    councilId: string;
}

export default function CreateBudgetModal({ isOpen, onClose, councilId }: Props) {
    const { initialAmount, setInitialAmount, year, setYear, handleSubmit, isPending } =
        useCreateBudget(councilId, onClose);

    if (!isOpen) return null;

    return (
        <div className="bg-background/80 animate-in fade-in zoom-in-95 fixed inset-0 z-50 flex items-center justify-center p-4 backdrop-blur-sm duration-200">
            <div className="bg-background border-border w-full max-w-md overflow-hidden rounded-xl border shadow-2xl">
                <div className="border-border bg-secondarybg flex items-center justify-between border-b p-4">
                    <h3 className="text-foreground text-lg font-bold">Otwórz Rok Budżetowy</h3>
                    <button
                        onClick={onClose}
                        className="text-txtcolor-300 hover:text-foreground transition-colors"
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-4 p-6">
                    <FormField
                        id="year"
                        label="Rok Budżetowy"
                        type="text"
                        value={year}
                        onChange={(e) => setYear(e.target.value)}
                        disabled={isPending}
                        placeholder="np. 2025/2026"
                    />
                    <FormField
                        id="amount"
                        label="Saldo Początkowe (PLN)"
                        type="number"
                        value={initialAmount}
                        onChange={(e) => setInitialAmount(e.target.value)}
                        disabled={isPending}
                        placeholder="0.00"
                    />

                    <div className="border-border mt-6 flex justify-end border-t pt-4">
                        <button
                            type="button"
                            onClick={onClose}
                            className="text-txtcolor-300 hover:bg-inputbg mr-2 rounded-lg px-4 py-2 text-sm font-medium transition-colors"
                        >
                            Anuluj
                        </button>
                        <button
                            type="submit"
                            disabled={isPending}
                            className="bg-primary text-darkgray flex items-center gap-2 rounded-lg px-6 py-2 font-bold transition-all hover:opacity-90 disabled:opacity-50"
                        >
                            {isPending ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                            ) : (
                                <Save className="h-4 w-4" />
                            )}
                            Utwórz
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}
