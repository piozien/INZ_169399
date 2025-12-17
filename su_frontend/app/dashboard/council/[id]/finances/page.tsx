'use client';

import { use } from 'react';
import { useRouter } from 'next/navigation';
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
    ArrowLeft,
} from 'lucide-react';
import BudgetChart from '@/components/budget/BudgetChart';
import AddTransactionModal from '@/components/budget/AddTransactionModal';
import EditTransactionModal from '@/components/budget/EditTransactionModal';
import CreateBudgetModal from '@/components/budget/CreateBudgetModal';
import EditBudgetModal from '@/components/budget/EditBudgetModal';
import ReportModal from '@/components/budget/ReportModal';
import { useCouncilBudget } from '@/hooks/council/budget/useCouncilBudget';

export default function FinancesPage({ params }: { params: Promise<{ id: string }> }) {
    const { id: councilId } = use(params);
    const router = useRouter();

    const {
        budget,
        transactions,
        isLoading,
        isCouncilActive,
        isLocked,
        isAdmin,
        permissions,
        removeTransaction,
        removeBudget,
        isAddModalOpen,
        openAddModal,
        closeAddModal,
        isCreateModalOpen,
        openCreateModal,
        closeCreateModal,
        isEditBudgetModalOpen,
        openEditBudgetModal,
        closeEditBudgetModal,

        editingTransaction,
        openEditTransactionModal,
        closeEditTransactionModal,

        isReportModalOpen,
        reportType,
        openReportModal,
        closeReportModal,
    } = useCouncilBudget(councilId);

    if (isLoading)
        return (
            <div className="flex h-[50vh] items-center justify-center">
                <Loader2 className="text-primary h-8 w-8 animate-spin" />
            </div>
        );

    if (!budget) {
        return (
            <div className="text-txtcolor-300 animate-in fade-in zoom-in-95 flex h-[60vh] flex-col items-center justify-center gap-6 p-6 duration-300">
                <div className="bg-secondarybg border-border rounded-full border-2 border-dashed p-6">
                    <PiggyBank className="text-foreground h-16 w-16 opacity-20" />
                </div>
                <div className="max-w-md text-center">
                    <h2 className="text-foreground mb-2 text-2xl font-bold">
                        Rok budżetowy zamknięty
                    </h2>
                    {isLocked ? (
                        <div className="mt-4 flex flex-col items-center gap-2">
                            <span className="text-error bg-error/10 border-error/20 flex items-center gap-2 rounded-lg border px-4 py-2">
                                <Lock className="h-4 w-4" /> Samorząd jest archiwalny
                            </span>
                            <p className="text-txtcolor-300 text-sm">
                                Nie można utworzyć budżetu dla nieaktywnego samorządu.
                            </p>
                        </div>
                    ) : (
                        <>
                            <p className="mb-6">
                                Ten samorząd nie ma jeszcze otwartego roku budżetowego.
                            </p>
                            {!isCouncilActive && isAdmin && (
                                <p className="text-warning bg-warning/10 border-warning/20 mb-2 rounded border px-2 py-1 text-xs">
                                    Tryb Administratora: Edycja archiwum
                                </p>
                            )}
                            {permissions.canCreateBudget ? (
                                <button
                                    onClick={openCreateModal}
                                    className="bg-primary text-darkgray rounded-xl px-8 py-3 font-bold shadow-lg transition-transform hover:scale-105 hover:opacity-90"
                                >
                                    Otwórz Rok Budżetowy
                                </button>
                            ) : (
                                <p className="text-error bg-error/10 border-error/20 inline-block rounded-lg border px-4 py-2 text-sm">
                                    Brak uprawnień do otwierania budżetu.
                                </p>
                            )}
                        </>
                    )}
                </div>
                <CreateBudgetModal
                    isOpen={isCreateModalOpen}
                    onClose={closeCreateModal}
                    councilId={councilId}
                />
            </div>
        );
    }

    return (
        <div className="animate-in fade-in mx-auto max-w-7xl space-y-8 p-6 duration-500">
            <div className="border-border flex flex-col items-start justify-between gap-4 border-b pb-6 md:flex-row md:items-center">
                <div className="flex items-center gap-3">
                    <button
                        onClick={() => router.push(`/dashboard/council/${councilId}`)}
                        className="text-txtcolor-300 hover:text-foreground hover:bg-secondarybg -ml-2 rounded-xl p-2 transition-colors"
                        title="Powrót do samorządu"
                    >
                        <ArrowLeft className="h-6 w-6" />
                    </button>
                    <div>
                        <h1 className="text-foreground flex items-center gap-3 text-2xl font-bold">
                            <PiggyBank className="text-success h-8 w-8" /> Finanse Samorządu
                            {!isCouncilActive && (
                                <span className="bg-error/10 text-error border-error/20 flex items-center gap-1 rounded-md border px-2 py-1 text-xs">
                                    <Lock className="h-3 w-3" /> ARCHIWUM
                                </span>
                            )}
                        </h1>
                        <p className="text-txtcolor-300 mt-1">
                            Rok budżetowy:{' '}
                            <span className="text-foreground bg-secondarybg ml-1 rounded-md px-2 py-0.5 font-mono font-bold">
                                {budget.year}
                            </span>
                        </p>
                    </div>
                </div>

                <div className="flex flex-wrap items-center gap-3 self-end md:self-auto">
                    <button
                        onClick={() => openReportModal('pdf')}
                        className="bg-secondarybg border-border hover:bg-inputbg text-foreground flex items-center gap-2 rounded-lg border px-4 py-2 text-sm transition-colors"
                    >
                        <FileDown className="h-4 w-4" />{' '}
                        <span className="hidden sm:inline">PDF</span>
                    </button>
                    <button
                        onClick={() => openReportModal('xlsx')}
                        className="bg-secondarybg border-border hover:bg-inputbg text-foreground flex items-center gap-2 rounded-lg border px-4 py-2 text-sm transition-colors"
                    >
                        <FileDown className="h-4 w-4" />{' '}
                        <span className="hidden sm:inline">Excel</span>
                    </button>

                    {(permissions.canEditBudget || permissions.canDeleteBudget) && (
                        <div className="bg-border mx-2 hidden h-8 w-px sm:block"></div>
                    )}

                    {permissions.canEditBudget && (
                        <button
                            onClick={() => !isLocked && openEditBudgetModal()}
                            disabled={isLocked}
                            className={`bg-secondarybg border-border rounded-lg border p-2 transition-colors ${isLocked ? 'cursor-not-allowed opacity-50' : 'hover:border-secondary hover:text-secondary text-txtcolor-300'}`}
                        >
                            <Settings className="h-5 w-5" />
                        </button>
                    )}

                    {permissions.canDeleteBudget && (
                        <button
                            onClick={removeBudget}
                            disabled={isLocked}
                            className={`bg-secondarybg border-border rounded-lg border p-2 transition-colors ${isLocked ? 'cursor-not-allowed opacity-50' : 'hover:border-error hover:bg-error/10 hover:text-error text-txtcolor-300'}`}
                        >
                            <Trash2 className="h-5 w-5" />
                        </button>
                    )}

                    {permissions.canEditTransactions && (
                        <button
                            onClick={openAddModal}
                            disabled={isLocked}
                            className={`ml-2 flex items-center gap-2 rounded-lg px-4 py-2 font-bold shadow-md transition-all ${isLocked ? 'bg-inputbg text-txtcolor-300 border-border cursor-not-allowed border opacity-60' : 'bg-primary text-darkgray hover:opacity-90'}`}
                        >
                            {isLocked ? <Lock className="h-4 w-4" /> : <Plus className="h-4 w-4" />}
                            <span className="hidden sm:inline">
                                {isLocked ? 'Zablokowane' : 'Dodaj transakcję'}
                            </span>
                        </button>
                    )}
                </div>
            </div>

            <div className="grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-4">
                <div className="bg-secondarybg border-border relative flex flex-col overflow-hidden rounded-xl border p-6 shadow-sm xl:col-span-1">
                    <span className="text-txtcolor-300 z-10 text-sm font-bold tracking-wider uppercase">
                        Aktualne Saldo
                    </span>
                    <span
                        className={`z-10 mt-2 text-3xl font-bold ${budget.balance >= 0 ? 'text-foreground' : 'text-error'}`}
                    >
                        {budget.balance.toFixed(2)} PLN
                    </span>
                    <PiggyBank className="text-foreground absolute -right-4 -bottom-4 z-0 h-24 w-24 opacity-5" />
                </div>
                <StatCard
                    label="Początkowe saldo"
                    value={budget.initialAmount}
                    icon={Wallet}
                    color="text-info"
                    bg="bg-secondarybg/30 border-info/20"
                    iconBg="bg-info/10"
                />
                <StatCard
                    label="Przychody"
                    value={budget.totalIncome}
                    icon={TrendingUp}
                    color="text-success"
                    bg="bg-secondarybg/50 border-success/20"
                    iconBg="bg-success/10"
                    prefix="+"
                />
                <StatCard
                    label="Wydatki"
                    value={budget.totalExpenses}
                    icon={TrendingDown}
                    color="text-error"
                    bg="bg-secondarybg/50 border-error/20"
                    iconBg="bg-error/10"
                    prefix="-"
                />
            </div>

            <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
                <div className="h-full min-h-[300px] lg:col-span-1">
                    <BudgetChart
                        income={budget.totalIncome || 0}
                        expenses={budget.totalExpenses || 0}
                    />
                </div>

                <div className="bg-secondarybg border-border flex h-auto max-h-[500px] flex-col overflow-hidden rounded-xl border shadow-sm lg:col-span-2">
                    <div className="border-border bg-inputbg/30 flex shrink-0 items-center justify-between border-b p-4 backdrop-blur-sm">
                        <h3 className="text-foreground flex items-center gap-2 font-bold">
                            <AlertCircle className="text-primary h-4 w-4" /> Historia operacji
                        </h3>
                        <span className="bg-background/50 text-txtcolor-300 border-border rounded border px-2 py-1 font-mono text-xs">
                            Liczba wpisów: {transactions?.length || 0}
                        </span>
                    </div>

                    <div className="scrollbar-thin flex-1 overflow-y-auto p-0">
                        {transactions?.length === 0 ? (
                            <div className="text-txtcolor-300 flex flex-col items-center justify-center gap-2 p-10">
                                <PiggyBank className="h-8 w-8 opacity-30" />
                                <p>Brak transakcji w tym budżecie.</p>
                            </div>
                        ) : (
                            <table className="w-full border-collapse text-left text-sm">
                                <thead className="text-txtcolor-300 bg-inputbg/50 sticky top-0 z-10 text-xs uppercase backdrop-blur-md">
                                <tr>
                                    <th className="bg-secondarybg px-6 py-3 font-semibold tracking-wider">
                                        Opis
                                    </th>
                                    <th className="bg-secondarybg hidden px-6 py-3 font-semibold tracking-wider sm:table-cell">
                                        Data
                                    </th>
                                    <th className="bg-secondarybg px-6 py-3 text-right font-semibold tracking-wider">
                                        Kwota
                                    </th>
                                    {permissions.canEditTransactions && !isLocked && (
                                        <th className="bg-secondarybg w-20 px-6 py-3 text-center">
                                            Akcje
                                        </th>
                                    )}
                                </tr>
                                </thead>
                                <tbody className="divide-border divide-y">
                                {transactions?.map((t) => (
                                    <tr
                                        key={t.id}
                                        className="hover:bg-foreground/5 group transition-colors"
                                    >
                                        <td className="text-foreground px-6 py-4 font-medium">
                                            {t.description}
                                        </td>
                                        <td className="text-txtcolor-300 hidden px-6 py-4 font-mono text-xs sm:table-cell">
                                            {new Date(t.date).toLocaleDateString('pl-PL')}
                                        </td>
                                        <td
                                            className={`px-6 py-4 text-right font-mono font-bold whitespace-nowrap ${t.type === 'INCOME' ? 'text-success' : 'text-error'}`}
                                        >
                                            {t.type === 'INCOME' ? '+' : '-'}
                                            {t.amount.toFixed(2)}
                                        </td>
                                        {permissions.canEditTransactions && !isLocked && (
                                            <td className="px-6 py-4 text-right">
                                                <div className="flex justify-end gap-2 transition-opacity">
                                                    <button
                                                        onClick={() =>
                                                            openEditTransactionModal(t)
                                                        }
                                                        className="bg-background border-border hover:border-primary text-txtcolor-300 hover:text-primary rounded-lg border p-1.5 transition-colors"
                                                    >
                                                        <Edit className="h-4 w-4" />
                                                    </button>
                                                    {permissions.canDeleteTransaction && (
                                                        <button
                                                            onClick={() =>
                                                                removeTransaction(t.id)
                                                            }
                                                            className="bg-background border-border hover:border-error text-txtcolor-300 hover:text-error rounded-lg border p-1.5 transition-colors"
                                                        >
                                                            <Trash2 className="h-4 w-4" />
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
                    onClose={closeAddModal}
                    budgetId={budget.id}
                    currentBalance={budget.balance}
                />
            )}
            {editingTransaction && (
                <EditTransactionModal
                    isOpen={!!editingTransaction}
                    onClose={closeEditTransactionModal}
                    transaction={editingTransaction}
                />
            )}
            {budget && (
                <EditBudgetModal
                    isOpen={isEditBudgetModalOpen}
                    onClose={closeEditBudgetModal}
                    budget={budget}
                />
            )}

            {budget && (
                <ReportModal
                    isOpen={isReportModalOpen}
                    onClose={closeReportModal}
                    budget={budget}
                    transactions={transactions || []}
                    initialType={reportType}
                />
            )}
        </div>
    );
}

const StatCard = ({ label, value, icon: Icon, color, bg, iconBg, prefix = '' }: any) => (
    <div className={`flex items-center justify-between rounded-xl border p-6 shadow-sm ${bg}`}>
        <div>
            <span className={`text-sm font-bold tracking-wider uppercase ${color}`}>{label}</span>
            <span className={`mt-1 block text-2xl font-bold ${color}`}>
                {prefix}
                {value?.toFixed(2) || '0.00'} PLN
            </span>
        </div>
        <div className={`rounded-full p-3 ${iconBg} ${color}`}>
            <Icon className="h-6 w-6" />
        </div>
    </div>
);