export type SuggestionStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface SuggestionDto {
    id: string;
    userId: string;
    councilId: string;
    title: string;
    description: string;
    isAnonymous: boolean;
    status: SuggestionStatus;
    rejectionReason?: string;
    createdAt: string; // ISO Date string
    upvotes: number;
    downvotes: number;
    tags: string[];
}

export interface CreateSuggestionPayload {
    title: string;
    description: string;
    isAnonymous: boolean;
    tags: string[];
}