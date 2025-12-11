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
    date: string;
    addedById: string;
}

export interface CouncilTransactionRequestDto {
    budgetId: string;
    type: TransactionType;
    amount: number;
    description: string;
    date: string;
}
