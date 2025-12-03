import {apiFetch} from "./httpClient";
import {
    CouncilBudgetResponseDto,
    CouncilBudgetRequestDto,
    CouncilTransactionRequestDto,
    CouncilTransactionResponseDto
} from "@/types/budget.types";


export const createBudget = async (councilId: string, data: CouncilBudgetRequestDto): Promise<CouncilBudgetResponseDto> => {
    return apiFetch<CouncilBudgetResponseDto>(`/councils/${councilId}/budget`, {
        method: 'POST',
        body: JSON.stringify(data),
    });
};

export const fetchCouncilBudget = async (councilId: string):
    Promise<CouncilBudgetResponseDto> =>
    apiFetch<CouncilBudgetResponseDto>(`/councils/${councilId}/budget`);

export const fetchBudgetTransactions = async (budgetId: string):
    Promise<CouncilTransactionResponseDto[]> =>
    apiFetch<CouncilTransactionResponseDto[]>(`/councils/budget/${budgetId}/transactions`);

export const addTransaction = async (budgetId: string, data: CouncilTransactionRequestDto):
    Promise<CouncilTransactionResponseDto> =>
    apiFetch<CouncilTransactionResponseDto>(`/councils/budget/${budgetId}/transactions`,
        {method: 'POST', body: JSON.stringify(data)});

export const updateTransaction = async (transactionId: string, data: CouncilTransactionRequestDto):
    Promise<CouncilTransactionResponseDto> => apiFetch<CouncilTransactionResponseDto>(`/councils/budget/transactions/${transactionId}`,
    {method: 'PUT', body: JSON.stringify(data)});

export const deleteTransaction = async (transactionId: string):
    Promise<void> => apiFetch<void>(`/councils/budget/transactions/${transactionId}`,
    {method: 'DELETE'});

export const updateBudget = async (budgetId: string, data: CouncilBudgetRequestDto): Promise<CouncilBudgetResponseDto> =>
    apiFetch<CouncilBudgetResponseDto>(`/councils/budget/${budgetId}`,
        {method: 'PUT', body: JSON.stringify(data)});

export const deleteBudget = async (budgetId: string):
    Promise<void> =>
    apiFetch<void>(`/councils/budget/${budgetId}`, {method: 'DELETE'});