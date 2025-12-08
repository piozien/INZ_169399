'use client';

import {use, useState} from 'react';
import {useQuery, useMutation, useQueryClient} from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import {
    fetchCouncilBudget,
    fetchBudgetTransactions,
    deleteTransaction,
    deleteBudget
} from '@/lib/api/budget';
import {fetchCouncilContext, fetchCouncilById} from '@/lib/api/council';
import {CouncilBudgetResponseDto, CouncilTransactionResponseDto} from '@/types/budget.types';
import {CouncilContextDto, CouncilResponseDto} from '@/types/council.types';
import {useAuth} from '@/lib/contexts/AuthContext';
import {
    Loader2,
    PiggyBank,
    FileDown,
    Plus,
    AlertCircle,
    Edit,
    Trash2,
    Wallet,
    Settings,
    Lock,
    TrendingUp,
    TrendingDown,
    ArrowLeft
} from 'lucide-react';
import BudgetChart from '@/components/budget/BudgetChart';
import AddTransactionModal from '@/components/budget/AddTransactionModal';
import EditTransactionModal from '@/components/budget/EditTransactionModal';
import CreateBudgetModal from '@/components/budget/CreateBudgetModal';
import EditBudgetModal from '@/components/budget/EditBudgetModal';
import {generateBudgetPdf, generateBudgetExcel} from '@/lib/reports/budgetReport';

export default function FinancesPage({params}: { params: Promise<{ id: string }> }) {
    const {id: councilId} = use(params);
    const {user} = useAuth();
    const router = useRouter();
    const queryClient = useQueryClient();

    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isEditBudgetModalOpen, setIsEditBudgetModalOpen] = useState(false);
    const [editingTransaction, setEditingTransaction] = useState<CouncilTransactionResponseDto | null>(null);

    const {data: council, isLoading: councilLoading} = useQuery<CouncilResponseDto>({
        queryKey: ['council', councilId],
        queryFn: () => fetchCouncilById(councilId),
    });

    const {data: budget, isLoading: budgetLoading} = useQuery<CouncilBudgetResponseDto>({
        queryKey: ['budget', councilId],
        queryFn: () => fetchCouncilBudget(councilId),
        retry: false,
    });

    const {data: transactions, isLoading: transLoading} = useQuery<CouncilTransactionResponseDto[]>({
        queryKey: ['budget', budget?.id, 'transactions'],
        queryFn: () => fetchBudgetTransactions(budget!.id),
        enabled: !!budget?.id,
    });

    const {data: context} = useQuery<CouncilContextDto>({
        queryKey: ['councilContext', councilId],
        queryFn: () => fetchCouncilContext(councilId),
    });

    const isCouncilActive = council?.active ?? false;
    const isAdmin = user?.roles?.includes('ADMINISTRATOR') || false;

    const isLocked = !isCouncilActive && !isAdmin;

    const deleteTransMutation = useMutation({
        mutationFn: deleteTransaction,
        onSuccess: () => queryClient.invalidateQueries({queryKey: ['budget']}),
        onError: (err) => alert(err instanceof Error ? err.message : "Błąd usuwania transakcji"),
    });

    const deleteBudgetMutation = useMutation({
        mutationFn: deleteBudget,
        onSuccess: () => queryClient.invalidateQueries({queryKey: ['budget']}),
        onError: (err) => alert(err instanceof Error ? err.message : "Błąd usuwania budżetu"),
    });


    const handleDeleteTransaction = (id: string) => {
        if (isLocked) return alert("Samorząd jest archiwalny. Nie można usuwać transakcji.");
        if (confirm("Czy na pewno chcesz usunąć tę transakcję? Saldo zostanie przeliczone.")) {
            deleteTransMutation.mutate(id);
        }
    };

    const handleDeleteBudget = () => {
        if (isLocked) return alert("Samorząd jest archiwalny. Nie można usunąć budżetu.");
        if (budget?.id && confirm("UWAGA! Czy na pewno chcesz usunąć CAŁY ROK BUDŻETOWY? Wszystkie transakcje zostaną utracone bezpowrotnie!")) {
            deleteBudgetMutation.mutate(budget.id);
        }
    };

    const handleDownload = (format: 'pdf' | 'xlsx') => {
        if (!budget || !transactions) return;
        if (format === 'pdf') {
            generateBudgetPdf(budget, transactions);
        } else {
            generateBudgetExcel(budget, transactions);
        }
    };

    const hasPermission = (perm: string) => {
        if (isAdmin) return true;

        if (budget?.myPermissions) {
            if (budget.myPermissions.includes('ALL_ACCESS') || budget.myPermissions.includes(perm)) return true;
        }
        if (context?.permissions) {
            if (context.permissions.includes('ALL_ACCESS') || context.permissions.includes(perm)) return true;
        }
        return false;
    };

    const canEditTransactions = hasPermission('COUNCIL_TRANSACTION_EDIT') || hasPermission('COUNCIL_TRANSACTION_CREATE');
    const canDeleteTransaction = hasPermission('COUNCIL_TRANSACTION_DELETE');
    const canEditBudget = hasPermission('COUNCIL_BUDGET_EDIT');
    const canDeleteBudget = hasPermission('COUNCIL_BUDGET_DELETE');
    const canCreateBudget = hasPermission('COUNCIL_BUDGET_CREATE');

    if (budgetLoading || councilLoading) return <div className="flex justify-center h-[50vh] items-center"><Loader2
        className="animate-spin text-primary h-8 w-8"/></div>;

    if (!budget) {
        return (
            <div
                className="flex flex-col items-center justify-center h-[60vh] text-txtcolor-300 gap-6 p-6 animate-in fade-in zoom-in-95 duration-300">
                <div className="p-6 bg-secondarybg rounded-full border-2 border-dashed border-border">
                    <PiggyBank className="h-16 w-16 opacity-20 text-foreground"/>
                </div>
                <div className="text-center max-w-md">
                    <h2 className="text-2xl font-bold text-foreground mb-2">Rok budżetowy zamknięty</h2>

                    {isLocked ? (
                        <div className="flex flex-col items-center gap-2 mt-4">
                            <span
                                className="text-error bg-error/10 px-4 py-2 rounded-lg border border-error/20 flex items-center gap-2">
                                <Lock className="w-4 h-4"/> Samorząd jest archiwalny
                            </span>
                            <p className="text-sm text-txtcolor-300">Nie można utworzyć budżetu dla nieaktywnego
                                samorządu.</p>
                        </div>
                    ) : (
                        <>
                            <p className="mb-6">Ten samorząd nie ma jeszcze otwartego roku budżetowego.</p>
                            {!isCouncilActive && isAdmin && (
                                <p className="text-xs text-warning mb-2 bg-warning/10 px-2 py-1 rounded border border-warning/20">
                                    Tryb Administratora: Edycja archiwum
                                </p>
                            )}

                            {canCreateBudget ? (
                                <button
                                    onClick={() => setIsCreateModalOpen(true)}
                                    className="bg-primary text-darkgray font-bold px-8 py-3 rounded-xl hover:opacity-90 transition-transform hover:scale-105 shadow-lg"
                                >
                                    Otwórz Rok Budżetowy
                                </button>
                            ) : (
                                <p className="text-sm text-error bg-error/10 px-4 py-2 rounded-lg inline-block border border-error/20">
                                    Brak uprawnień do otwierania budżetu.
                                </p>
                            )}
                        </>
                    )}
                </div>
                <CreateBudgetModal
                    isOpen={isCreateModalOpen}
                    onClose={() => setIsCreateModalOpen(false)}
                    councilId={councilId}
                />
            </div>
        );
    }

    return (
        <div className="p-6 space-y-8 max-w-7xl mx-auto animate-in fade-in duration-500">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 border-b border-border pb-6">

                <div className="flex items-center gap-3">
                    <button
                        onClick={() => router.push(`/dashboard/council/${councilId}`)}
                        className="p-2 -ml-2 rounded-xl text-txtcolor-300 hover:text-foreground hover:bg-secondarybg transition-colors"
                        title="Powrót do samorządu"
                    >
                        <ArrowLeft className="h-6 w-6" />
                    </button>
                    <div>
                        <h1 className="text-2xl font-bold text-foreground flex items-center gap-3">
                            <PiggyBank className="text-success h-8 w-8"/> Finanse Samorządu
                            {!isCouncilActive && (
                                <span className="text-xs bg-error/10 text-error border border-error/20 px-2 py-1 rounded-md flex items-center gap-1">
                                    <Lock className="w-3 h-3"/> ARCHIWUM
                                </span>
                            )}
                        </h1>
                        <p className="text-txtcolor-300 mt-1">
                            Rok budżetowy: <span className="text-foreground font-mono font-bold ml-1 px-2 py-0.5 bg-secondarybg rounded-md">{budget.year}</span>
                        </p>
                    </div>
                </div>

                <div className="flex gap-3 flex-wrap items-center self-end md:self-auto">
                    <button onClick={() => handleDownload('pdf')}
                            className="flex items-center gap-2 px-4 py-2 bg-secondarybg border border-border hover:bg-inputbg rounded-lg text-sm transition-colors text-foreground"
                            title="Pobierz PDF">
                        <FileDown className="h-4 w-4"/> <span className="hidden sm:inline">PDF</span>
                    </button>
                    <button onClick={() => handleDownload('xlsx')}
                            className="flex items-center gap-2 px-4 py-2 bg-secondarybg border border-border hover:bg-inputbg rounded-lg text-sm transition-colors text-foreground"
                            title="Pobierz Excel">
                        <FileDown className="h-4 w-4"/> <span className="hidden sm:inline">Excel</span>
                    </button>

                    {(canEditBudget || canDeleteBudget) &&
                        <div className="w-px h-8 bg-border mx-2 hidden sm:block"></div>}

                    {canEditBudget && (
                        <button
                            onClick={() => !isLocked && setIsEditBudgetModalOpen(true)}
                            disabled={isLocked}
                            className={`p-2 bg-secondarybg border border-border rounded-lg transition-colors ${isLocked ? 'opacity-50 cursor-not-allowed' : 'hover:border-secondary hover:text-secondary text-txtcolor-300'}`}
                            title={isLocked ? "Edycja zablokowana" : "Ustawienia Budżetu"}
                        >
                            <Settings className="h-5 w-5"/>
                        </button>
                    )}

                    {canDeleteBudget && (
                        <button
                            onClick={handleDeleteBudget}
                            disabled={isLocked}
                            className={`p-2 bg-secondarybg border border-border rounded-lg transition-colors ${isLocked ? 'opacity-50 cursor-not-allowed' : 'hover:border-error hover:bg-error/10 hover:text-error text-txtcolor-300'}`}
                            title={isLocked ? "Usuwanie zablokowane" : "Usuń CAŁY Budżet"}
                        >
                            <Trash2 className="h-5 w-5"/>
                        </button>
                    )}

                    {canEditTransactions && (
                        <button
                            onClick={() => setIsAddModalOpen(true)}
                            disabled={isLocked}
                            className={`flex items-center gap-2 font-bold px-4 py-2 rounded-lg transition-all shadow-md ml-2
                                ${isLocked
                                ? 'bg-inputbg text-txtcolor-300 cursor-not-allowed opacity-60 border border-border'
                                : 'bg-primary text-darkgray hover:opacity-90'
                            }`}
                        >
                            {isLocked ? <Lock className="h-4 w-4"/> : <Plus className="h-4 w-4"/>}
                            <span className="hidden sm:inline">
                                {isLocked ? 'Zablokowane' : 'Dodaj transakcję'}
                            </span>
                        </button>
                    )}
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
                <div
                    className="bg-secondarybg p-6 rounded-xl border border-border flex flex-col relative overflow-hidden shadow-sm xl:col-span-1">
                    <span
                        className="text-sm font-bold text-txtcolor-300 uppercase z-10 tracking-wider">Aktualne Saldo</span>
                    <span
                        className={`text-3xl font-bold mt-2 z-10 ${budget.balance >= 0 ? 'text-foreground' : 'text-error'}`}>
                        {budget.balance.toFixed(2)} PLN
                    </span>
                    <PiggyBank className="absolute -bottom-4 -right-4 h-24 w-24 text-foreground opacity-5 z-0"/>
                </div>

                <div
                    className="bg-secondarybg/30 p-6 rounded-xl border border-info/20 flex items-center justify-between shadow-sm">
                    <div>
                        <span className="text-sm font-bold text-info uppercase tracking-wider">Początkowe saldo</span>
                        <span
                            className="block text-2xl font-bold mt-1 text-info">{budget.initialAmount.toFixed(2)} PLN</span>
                    </div>
                    <div className="p-3 bg-info/10 rounded-full text-info"><Wallet className="h-6 w-6"/></div>
                </div>

                <div
                    className="bg-secondarybg/50 p-6 rounded-xl border border-success/20 flex items-center justify-between shadow-sm">
                    <div>
                        <span className="text-sm font-bold text-success uppercase tracking-wider">Przychody</span>
                        <span
                            className="block text-2xl font-bold mt-1 text-success">+{budget.totalIncome?.toFixed(2) || '0.00'} PLN</span>
                    </div>
                    <div className="p-3 bg-success/10 rounded-full text-success"><TrendingUp className="h-6 w-6"/></div>
                </div>

                <div
                    className="bg-secondarybg/50 p-6 rounded-xl border border-error/20 flex items-center justify-between shadow-sm">
                    <div>
                        <span className="text-sm font-bold text-error uppercase tracking-wider">Wydatki</span>
                        <span
                            className="block text-2xl font-bold mt-1 text-error">-{budget.totalExpenses?.toFixed(2) || '0.00'} PLN</span>
                    </div>
                    <div className="p-3 bg-error/10 rounded-full text-error"><TrendingDown className="h-6 w-6"/></div>
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="lg:col-span-1 h-full min-h-[300px]">
                    <BudgetChart income={budget.totalIncome || 0} expenses={budget.totalExpenses || 0}/>
                </div>

                <div
                    className="lg:col-span-2 bg-secondarybg rounded-xl border border-border overflow-hidden flex flex-col h-auto max-h-[500px] shadow-sm">
                    <div
                        className="p-4 border-b border-border bg-inputbg/30 flex justify-between items-center backdrop-blur-sm shrink-0">
                        <h3 className="font-bold text-foreground flex items-center gap-2">
                            <AlertCircle className="h-4 w-4 text-primary"/> Historia operacji
                        </h3>
                        <span
                            className="text-xs font-mono bg-background/50 px-2 py-1 rounded text-txtcolor-300 border border-border">
                            Liczba wpisów: {transactions?.length || 0}
                        </span>
                    </div>

                    <div className="flex-1 overflow-y-auto scrollbar-thin p-0">
                        {transLoading ? (
                            <div className="h-full flex items-center justify-center"><Loader2
                                className="animate-spin text-primary h-8 w-8"/></div>
                        ) : transactions?.length === 0 ? (
                            <div className="p-10 flex flex-col items-center justify-center text-txtcolor-300 gap-2">
                                <PiggyBank className="h-8 w-8 opacity-30"/>
                                <p>Brak transakcji w tym budżecie.</p>
                            </div>
                        ) : (
                            <table className="w-full text-sm text-left border-collapse">
                                <thead
                                    className="text-xs text-txtcolor-300 uppercase bg-inputbg/50 sticky top-0 backdrop-blur-md z-10">
                                <tr>
                                    <th className="px-6 py-3 font-semibold tracking-wider bg-secondarybg">Opis</th>
                                    <th className="px-6 py-3 font-semibold tracking-wider hidden sm:table-cell bg-secondarybg">Data</th>
                                    <th className="px-6 py-3 text-right font-semibold tracking-wider bg-secondarybg">Kwota</th>
                                    {canEditTransactions && !isLocked &&
                                        <th className="px-6 py-3 w-20 bg-secondarybg text-center">Akcje</th>}
                                </tr>
                                </thead>
                                <tbody className="divide-y divide-border">
                                {transactions?.map((t) => (
                                    <tr key={t.id} className="hover:bg-foreground/5 transition-colors group">
                                        <td className="px-6 py-4 font-medium text-foreground">{t.description}</td>
                                        <td className="px-6 py-4 text-txtcolor-300 hidden sm:table-cell font-mono text-xs">
                                            {new Date(t.date).toLocaleDateString('pl-PL')}
                                        </td>
                                        <td className={`px-6 py-4 text-right font-mono font-bold whitespace-nowrap ${t.type === 'INCOME' ? 'text-success' : 'text-error'}`}>
                                            {t.type === 'INCOME' ? '+' : '-'}{t.amount.toFixed(2)}
                                        </td>
                                        {canEditTransactions && !isLocked && (
                                            <td className="px-6 py-4 text-right">
                                                <div className="flex justify-end gap-2 transition-opacity">
                                                    <button
                                                        onClick={() => setEditingTransaction(t)}
                                                        className="p-1.5 rounded-lg bg-background border border-border hover:border-primary text-txtcolor-300 hover:text-primary transition-colors"
                                                        title="Edytuj"
                                                    >
                                                        <Edit className="h-4 w-4"/>
                                                    </button>

                                                    {canDeleteTransaction && (
                                                        <button
                                                            onClick={() => handleDeleteTransaction(t.id)}
                                                            className="p-1.5 rounded-lg bg-background border border-border hover:border-error text-txtcolor-300 hover:text-error transition-colors"
                                                            title="Usuń"
                                                        >
                                                            <Trash2 className="h-4 w-4"/>
                                                        </button>
                                                    )}
                                                </div>
                                            </td>
                                        )}
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        )}
                    </div>
                </div>
            </div>

            {budget && (
                <AddTransactionModal
                    isOpen={isAddModalOpen}
                    onClose={() => setIsAddModalOpen(false)}
                    budgetId={budget.id}
                    currentBalance={budget.balance}
                />
            )}

            {editingTransaction && (
                <EditTransactionModal
                    isOpen={!!editingTransaction}
                    onClose={() => setEditingTransaction(null)}
                    transaction={editingTransaction}
                />
            )}

            {budget && (
                <EditBudgetModal
                    isOpen={isEditBudgetModalOpen}
                    onClose={() => setIsEditBudgetModalOpen(false)}
                    budget={budget}
                />
            )}

        </div>
    );
}