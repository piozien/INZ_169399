import { apiFetch } from './httpClient';
import { CreateSuggestionPayload, SuggestionDto } from '@/types/suggestions.types';

export const fetchSuggestions = async (): Promise<SuggestionDto[]> => {
    return apiFetch<SuggestionDto[]>('/suggestions');
};

export const fetchCouncilSuggestions = async (councilId: string): Promise<SuggestionDto[]> => {
    return apiFetch<SuggestionDto[]>(`/suggestions/council/${councilId}`);
};

export const createSuggestion = async (data: CreateSuggestionPayload): Promise<SuggestionDto> => {
    return apiFetch<SuggestionDto>('/suggestions', {
        method: 'POST',
        body: JSON.stringify(data),
    });
};

export const updateSuggestion = async (
    id: string,
    data: Partial<CreateSuggestionPayload>
): Promise<SuggestionDto> => {
    return apiFetch<SuggestionDto>(`/suggestions/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data),
    });
};

export const approveSuggestion = async (id: string): Promise<SuggestionDto> => {
    return apiFetch<SuggestionDto>(`/suggestions/${id}/approve`, {
        method: 'PUT',
    });
};

export const rejectSuggestion = async (id: string, reason: string): Promise<SuggestionDto> => {
    const params = new URLSearchParams({ rejectionReason: reason });
    return apiFetch<SuggestionDto>(`/suggestions/${id}/reject?${params.toString()}`, {
        method: 'PUT',
    });
};

export const deleteSuggestion = async (id: string): Promise<void> => {
    return apiFetch<void>(`/suggestions/${id}`, {
        method: 'DELETE',
    });
};
