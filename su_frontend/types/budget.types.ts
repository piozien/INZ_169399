export type TransactionType = 'INCOME' | 'EXPENSE';

export interface CouncilBudgetRequestDto {
    year: string;
    initialAmount: number;
    councilId: string;
}

export interface CouncilBudgetResponseDto {
    id: string;
    councilId: string;
    councilName: string;
    year: string;
    initialAmount: number;
    balance: number;
    createdById: string;
    createdAt: string;
    totalIncome?: number;
    totalExpenses?: number;
    myPermissions?: string[];
}

export interface CouncilTransactionResponseDto {
    id: string;
    budgetId: string;
    type: TransactionType;
    amount: number;
    description: string;
    date: string; // ISO Date
    addedById: string;
}

export interface CouncilTransactionRequestDto {
    budgetId: string;
    type: TransactionType;
    amount: number;
    description: string;
    date: string; // ISO Date (YYYY-MM-DDTHH:mm:ss)
}

export interface CategorySummaryDto {
    category: string;
    totalAmount: number;
    transactionCount: number;
    percentage: number;
}

export interface TransactionSummaryDto {
    description: string;
    amount: number;
    type: TransactionType;
    category: string;
    transactionDate: string;
    createdBy: string;
}

export interface BudgetReportDto {
    budgetName: string;
    budgetType: string;
    initialAmount: number;
    totalIncome: number;
    totalExpenses: number;
    currentBalance: number;
    reportDate: string;
    fromDate: string;
    toDate: string;
    transactions: TransactionSummaryDto[];
    incomeByCategory: CategorySummaryDto[];
    expensesByCategory: CategorySummaryDto[];
}