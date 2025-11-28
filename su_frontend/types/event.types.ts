export interface ParticipantResponseDto {
    eventId: string;
    userId: string;
    role: string;
    confirmed: boolean;
    assignedAt: string;
}

export interface EventResponseDto {
    id: string;
    title: string;
    description: string;
    startDate: string;
    endDate: string;
    location: boolean;
    createdById: string;
    calendarEventId: string;
    participants?: ParticipantResponseDto[];
}

export interface EventRequestDto {
    title: string;
    description: string;
    startDate: string; // YYYY-MM-DD
    endDate: string;   // YYYY-MM-DD
    location?: string;

}