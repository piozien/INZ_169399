export type EventStatus = 'DRAFT' | 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED';
export type EventParticipantRole = 'ORGANIZER' | 'PARTICIPANT' | 'GUEST';

export interface ParticipantResponseDto {
    eventId: string;
    userId: string;
    userFullName?: string
    role: EventParticipantRole;
    confirmed: boolean;
    assignedAt: string;
}

export interface EventResponseDto {
    id: string;
    title: string;
    description: string;
    startDate: string; // ISO String: "2025-09-01T09:00:00"
    endDate: string;   // ISO String
    location: string;
    createdById: string;
    councilId?: string;
    calendarEventId?: string;
    status: EventStatus;
    participants?: ParticipantResponseDto[];
}

export interface EventRequestDto {
    title: string;
    description: string;
    startDate: string; //"YYYY-MM-DDTHH:mm:ss"
    endDate: string;   //"YYYY-MM-DDTHH:mm:ss"
    location?: string;
    councilId?: string;
}