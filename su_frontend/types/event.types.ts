export type EventStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
export type EventParticipantRole = 'ORGANIZER' | 'PARTICIPANT';

export interface ParticipantResponseDto {
    eventId: string;
    userId: string;
    userFullName?: string;
    userEmail?: string;
    role: EventParticipantRole;
    confirmed: boolean;
    assignedAt: string;
}

export interface EventResponseDto {
    id: string;
    title: string;
    description: string;
    startDate: string;
    endDate: string;
    location: string;
    createdById: string;
    councilId?: string;
    calendarEventId?: string;
    status: EventStatus;
    participants?: ParticipantResponseDto[];
    maxParticipants?: number | null;
    participantsCount: number;
}

export interface EventRequestDto {
    title: string;
    description: string;
    startDate: string;
    endDate: string;
    location?: string;
    councilId?: string;
    maxParticipants?: number | null;
}