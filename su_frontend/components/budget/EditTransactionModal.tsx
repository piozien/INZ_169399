'use client';

import { X, Save, Loader2, TrendingUp, TrendingDown, ChevronUp, ChevronDown } from 'lucide-react';
import { CouncilTransactionResponseDto } from '@/types/budget.types';
import FormField from '@/components/FormField';
import { useEditTransaction } from '@/hooks/council/budget/useEditTransaction';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    transaction: CouncilTransactionResponseDto;
}

export default function EditTransactionModal({ isOpen, onClose, transaction }: Props) {
    const {
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
        isPending,
    } = useEditTransaction(transaction, onClose, isOpen);

    if (!isOpen) return null;

    return (
        <div className="bg-background/80 animate-in fade-in fixed inset-0 z-50 flex items-center justify-center p-4 backdrop-blur-sm duration-200">
            <div className="bg-background border-border animate-in zoom-in-95 w-full max-w-lg overflow-hidden rounded-xl border shadow-2xl duration-200">
                <div className="border-border bg-secondarybg flex items-center justify-between border-b p-4">
                    <h3 className="text-foreground text-lg font-bold">Edytuj Transakcję</h3>
                    <button
                        onClick={onClose}
                        className="text-txtcolor-300 hover:text-foreground transition-colors"
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-5 p-6">
                    <div className="bg-inputbg flex gap-2 rounded-lg p-1">
                        <TypeButton
                            active={type === 'INCOME'}
                            onClick={() => setType('INCOME')}
                            icon={TrendingUp}
                            label="Wpływ"
                            colorClass="bg-success text-foreground shadow-md"
                            disabled={isPending}
                        />
                        <TypeButton
                            active={type === 'EXPENSE'}
                            onClick={() => setType('EXPENSE')}
                            icon={TrendingDown}
                            label="Wydatek"
                            colorClass="bg-error text-foreground shadow-md"
                            disabled={isPending}
                        />
                    </div>

                    <FormField
                        id="desc_edit"
                        label="OPIS OPERACJI"
                        type="text"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        placeholder=""
                        disabled={isPending}
                    />

                    <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                        <div className="sm:col-span-2">
                            <div className="space-y-1">
                                <label
                                    htmlFor="amount_edit"
                                    className="text-txtcolor-300 block text-xs font-bold tracking-wider uppercase"
                                >
                                    KWOTA
                                </label>
                                <div className="relative">
                                    <input
                                        id="amount_edit"
                                        type="number"
                                        value={amount}
                                        onChange={(e) => setAmount(e.target.value)}
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
                                id="date_edit"
                                label="DATA"
                                type="date"
                                value={date}
                                onChange={(e) => setDate(e.target.value)}
                                disabled={isPending}
                                placeholder=""
                            />
                        </div>
                        <div className="sm:col-span-1">
                            <FormField
                                id="time_edit"
                                label="GODZINA"
                                type="time"
                                value={time}
                                onChange={(e) => setTime(e.target.value)}
                                disabled={isPending}
                                placeholder=""
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
                            className="bg-primary text-darkgray flex items-center gap-2 rounded-lg px-6 py-2.5 text-sm font-bold transition-all hover:opacity-90 disabled:opacity-50"
                        >
                            {isPending ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                            ) : (
                                <Save className="h-4 w-4" />
                            )}{' '}
                            Zapisz
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
        className="hover:bg-foreground/10 text-txtcolor-300 hover:text-primary flex h-1/2 items-center rounded p-0.5 transition-colors"
        tabIndex={-1}
    >
        <Icon className="h-4 w-4" />
    </button>
);
