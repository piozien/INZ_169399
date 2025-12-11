'use client';

import {
    X,
    Plus,
    Loader2,
    TrendingUp,
    TrendingDown,
    AlertTriangle,
    ChevronUp,
    ChevronDown,
} from 'lucide-react';
import FormField from '@/components/FormField';
import { useAddTransaction } from '@/hooks/council/budget/useAddTransaction';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    budgetId: string;
    currentBalance: number;
}

export default function AddTransactionModal({ isOpen, onClose, budgetId, currentBalance }: Props) {
    const {
        description,
        setDescription,
        amount,
        setAmount,
        type,
        changeType,
        date,
        setDate,
        time,
        setTime,
        showDebitWarning,
        changeAmount,
        handleSubmit,
        isPending,
    } = useAddTransaction(budgetId, currentBalance, onClose, isOpen);

    if (!isOpen) return null;

    return (
        <div className="bg-background/80 animate-in fade-in fixed inset-0 z-50 flex items-center justify-center p-4 backdrop-blur-sm duration-200">
            <div className="bg-background border-border animate-in zoom-in-95 w-full max-w-lg overflow-hidden rounded-xl border shadow-2xl duration-200">
                <div className="border-border bg-secondarybg flex items-center justify-between border-b p-4">
                    <h3 className="text-foreground text-lg font-bold">Dodaj Transakcję</h3>
                    <button
                        onClick={onClose}
                        className="text-txtcolor-300 hover:text-foreground transition-colors"
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-5 p-6">
                    {showDebitWarning && (
                        <div className="bg-error/10 border-error/20 animate-in slide-in-from-top-2 flex items-start gap-3 rounded-lg border p-4">
                            <AlertTriangle className="text-error mt-0.5 h-5 w-5 shrink-0" />
                            <div>
                                <h4 className="text-error text-sm font-bold">
                                    Uwaga: Powstanie debet!
                                </h4>
                                <p className="text-txtcolor-300 mt-1 text-xs">
                                    Ta transakcja przekracza dostępne środki (
                                    {currentBalance.toFixed(2)} PLN).
                                </p>
                                <p className="text-error mt-2 text-xs font-bold">
                                    Czy na pewno chcesz kontynuować?
                                </p>
                            </div>
                        </div>
                    )}

                    <div className="bg-inputbg flex gap-2 rounded-lg p-1">
                        <TypeButton
                            active={type === 'INCOME'}
                            onClick={() => changeType('INCOME')}
                            icon={TrendingUp}
                            label="Wpływ"
                            colorClass="bg-success text-foreground shadow-md"
                            disabled={isPending}
                        />
                        <TypeButton
                            active={type === 'EXPENSE'}
                            onClick={() => changeType('EXPENSE')}
                            icon={TrendingDown}
                            label="Wydatek"
                            colorClass="bg-error text-foreground shadow-md"
                            disabled={isPending}
                        />
                    </div>

                    <FormField
                        id="desc"
                        label="OPIS OPERACJI"
                        type="text"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        placeholder="np. Zakup papieru"
                        disabled={isPending}
                    />

                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                        <div className="sm:col-span-2">
                            <div className="space-y-1">
                                <label
                                    htmlFor="amount"
                                    className="text-txtcolor-300 block text-xs font-bold tracking-wider uppercase"
                                >
                                    KWOTA (PLN)
                                </label>
                                <div className="relative">
                                    <input
                                        id="amount"
                                        type="number"
                                        value={amount}
                                        onChange={(e) => {
                                            setAmount(e.target.value);
                                            changeType(type);
                                        }}
                                        disabled={isPending}
                                        placeholder="0.00"
                                        step="0.01"
                                        className="bg-inputbg text-foreground border-border focus:ring-primary/50 w-full rounded-lg border px-4 py-3 text-center text-sm transition-all focus:ring-2 focus:outline-none disabled:opacity-50"
                                    />
                                    <div className="absolute top-1 right-1 bottom-1 flex flex-col justify-center gap-0.5">
                                        <AmountControl
                                            onClick={() => changeAmount(1)}
                                            icon={ChevronUp}
                                        />
                                        <AmountControl
                                            onClick={() => changeAmount(-1)}
                                            icon={ChevronDown}
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div className="sm:col-span-1">
                            <FormField
                                id="date"
                                label="DATA"
                                type="date"
                                value={date}
                                onChange={(e) => setDate(e.target.value)}
                                disabled={isPending}
                            />
                        </div>
                        <div className="sm:col-span-1">
                            <FormField
                                id="time"
                                label="GODZINA"
                                type="time"
                                value={time}
                                onChange={(e) => setTime(e.target.value)}
                                disabled={isPending}
                            />
                        </div>
                    </div>

                    <div className="border-border flex justify-end gap-3 border-t pt-4">
                        <button
                            type="button"
                            onClick={onClose}
                            disabled={isPending}
                            className="text-txtcolor-300 hover:bg-inputbg rounded-lg px-4 py-2 text-sm font-medium transition-colors"
                        >
                            Anuluj
                        </button>
                        <button
                            type="submit"
                            disabled={isPending}
                            className={`text-foreground flex items-center gap-2 rounded-lg px-6 py-2.5 text-sm font-bold transition-all hover:opacity-90 disabled:opacity-50 ${showDebitWarning ? 'bg-error' : 'bg-primary text-darkgray'}`}
                        >
                            {isPending ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                            ) : showDebitWarning ? (
                                'Zatwierdź Debet'
                            ) : (
                                <>
                                    <Plus className="h-4 w-4" /> Zatwierdź
                                </>
                            )}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

const TypeButton = ({ active, onClick, icon: Icon, label, colorClass, disabled }: any) => (
    <button
        type="button"
        onClick={onClick}
        disabled={disabled}
        className={`flex flex-1 items-center justify-center gap-2 rounded-md py-2 text-sm font-bold transition-all ${active ? colorClass : 'text-txtcolor-300 hover:text-foreground'}`}
    >
        <Icon className="h-4 w-4" /> {label}
    </button>
);

const AmountControl = ({ onClick, icon: Icon }: any) => (
    <button
        type="button"
        onClick={onClick}
        className="text-txtcolor-300 hover:text-primary flex h-1/2 items-center rounded p-0.5 transition-colors hover:bg-white/10"
        tabIndex={-1}
    >
        <Icon className="h-4 w-4" />
    </button>
);
