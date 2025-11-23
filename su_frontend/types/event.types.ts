import { EventStatus, EventParticipantRole } from './enums.types';

export interface ParticipantResponseDto {
  eventId: string;
  userId: string;
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
  calendarEventId?: string;
  createdAt: string;
  status: EventStatus;
  participants: ParticipantResponseDto[];
}

export type Event = EventResponseDto;
