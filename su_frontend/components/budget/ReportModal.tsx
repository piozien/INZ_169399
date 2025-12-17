'use client';

import { useState } from 'react';
import { X, Calendar, FileDown, Loader2 } from 'lucide-react';
import { CouncilBudgetResponseDto, CouncilTransactionResponseDto } from '@/types/budget.types';
import { generateBudgetPdf, generateBudgetExcel } from '@/lib/reports/budgetReport';

interface ReportModalProps {
    isOpen: boolean;
    onClose: () => void;
    budget: CouncilBudgetResponseDto;
    transactions: CouncilTransactionResponseDto[];
    initialType?: 'pdf' | 'xlsx';
}

export default function ReportModal({
                                        isOpen,
                                        onClose,
                                        budget,
                                        transactions,
                                        initialType = 'pdf',
                                    }: ReportModalProps) {
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [isGenerating, setIsGenerating] = useState(false);
    const [reportType, setReportType] = useState<'pdf' | 'xlsx'>(initialType);

    if (!isOpen) return null;

    const handleGenerate = async () => {
        setIsGenerating(true);

        let filteredTransactions = [...transactions];

        if (startDate) {
            filteredTransactions = filteredTransactions.filter(
                (t) => new Date(t.date) >= new Date(startDate)
            );
        }

        if (endDate) {
            const end = new Date(endDate);
            end.setHours(23, 59, 59, 999);
            filteredTransactions = filteredTransactions.filter(
                (t) => new Date(t.date) <= end
            );
        }

        filteredTransactions.sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());

        const dateRange = {
            from: startDate || null,
            to: endDate || null
        };

        try {
            if (reportType === 'pdf') {
                await generateBudgetPdf(budget, filteredTransactions, dateRange);
            } else {
                await generateBudgetExcel(budget, filteredTransactions, dateRange);
            }
            onClose();
        } catch (error) {
            console.error("Błąd generowania raportu", error);
        } finally {
            setIsGenerating(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/50 p-4 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="bg-background border-border w-full max-w-md rounded-xl border shadow-xl animate-in zoom-in-95 duration-200">
                <div className="border-border flex items-center justify-between border-b p-4">
                    <h3 className="text-foreground flex items-center gap-2 text-lg font-bold">
                        <FileDown className="text-primary h-5 w-5" />
                        Generuj Raport
                    </h3>
                    <button
                        onClick={onClose}
                        className="text-txtcolor-300 hover:text-foreground rounded-lg p-1 transition-colors hover:bg-secondarybg"
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <div className="space-y-4 p-6">
                    <div className="space-y-2">
                        <label className="text-txtcolor-300 text-sm font-bold uppercase">Format</label>
                        <div className="flex gap-2">
                            <button
                                onClick={() => setReportType('pdf')}
                                className={`flex-1 rounded-lg border py-2 text-sm font-bold transition-all ${
                                    reportType === 'pdf'
                                        ? 'border-primary bg-primary/10 text-primary'
                                        : 'border-border bg-inputbg text-txtcolor-300 hover:text-foreground'
                                }`}
                            >
                                PDF
                            </button>
                            <button
                                onClick={() => setReportType('xlsx')}
                                className={`flex-1 rounded-lg border py-2 text-sm font-bold transition-all ${
                                    reportType === 'xlsx'
                                        ? 'border-success bg-success/10 text-success'
                                        : 'border-border bg-inputbg text-txtcolor-300 hover:text-foreground'
                                }`}
                            >
                                Excel
                            </button>
                        </div>
                    </div>

                    <div className="space-y-4">
                        <div className="space-y-2">
                            <label className="text-txtcolor-300 text-sm font-bold uppercase flex items-center gap-2">
                                <Calendar className="w-4 h-4" /> Zakres dat (opcjonalne)
                            </label>
                            <div className="grid grid-cols-2 gap-4">
                                <div>
                                    <span className="text-xs text-txtcolor-300 mb-1 block">Od:</span>
                                    <input
                                        type="date"
                                        value={startDate}
                                        onChange={(e) => setStartDate(e.target.value)}
                                        className="bg-inputbg border-border text-foreground w-full rounded-lg border px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                                    />
                                </div>
                                <div>
                                    <span className="text-xs text-txtcolor-300 mb-1 block">Do:</span>
                                    <input
                                        type="date"
                                        value={endDate}
                                        onChange={(e) => setEndDate(e.target.value)}
                                        className="bg-inputbg border-border text-foreground w-full rounded-lg border px-3 py-2 text-sm focus:border-primary focus:outline-none focus:ring-1 focus:ring-primary"
                                    />
                                </div>
                            </div>
                            <p className="text-xs text-txtcolor-300 italic">
                                Pozostaw puste, aby wygenerować raport za cały rok budżetowy.
                            </p>
                        </div>
                    </div>
                </div>

                <div className="bg-secondarybg/50 border-border flex justify-end gap-3 border-t p-4">
                    <button
                        onClick={onClose}
                        className="text-txtcolor-300 hover:bg-secondarybg hover:text-foreground rounded-lg px-4 py-2 text-sm font-medium transition-colors"
                    >
                        Anuluj
                    </button>
                    <button
                        onClick={handleGenerate}
                        disabled={isGenerating}
                        className="bg-primary text-darkgray hover:opacity-90 flex items-center gap-2 rounded-lg px-6 py-2 text-sm font-bold shadow-md transition-all disabled:opacity-50"
                    >
                        {isGenerating ? (
                            <>
                                <Loader2 className="h-4 w-4 animate-spin" /> Generowanie...
                            </>
                        ) : (
                            <>
                                <FileDown className="h-4 w-4" /> Pobierz
                            </>
                        )}
                    </button>
                </div>
            </div>
        </div>
    );
}