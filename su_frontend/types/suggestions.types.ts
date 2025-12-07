export type SuggestionStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface SuggestionDto {
    id: string;
    title: string;
    description: string;
    anonymous: boolean;
    status: SuggestionStatus;
    rejectionReason?: string;
    createdAt: string; // ISO Date string
    userId: string;
    fullName?: string;
    tags: string[];
}

export interface CreateSuggestionPayload {
    title: string;
    userId: string;
    description: string;
    anonymous: boolean;
    tags: string[];
}