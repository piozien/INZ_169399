import { apiFetch } from "./httpClient";
import { EventRequestDto, EventResponseDto, ParticipantResponseDto } from "@/types/event.types";

export const createEvent = async (data: EventRequestDto): Promise<EventResponseDto> => {
    return apiFetch<EventResponseDto>("/events", {
        method: "POST",
        body: JSON.stringify(data),
    });
};

export const fetchAllEvents = async (): Promise<EventResponseDto[]> => {
    return apiFetch<EventResponseDto[]>("/events");
};

export const fetchCouncilEvents = async (councilId: string): Promise<EventResponseDto[]> => {
    return apiFetch<EventResponseDto[]>(`/events/council/${councilId}`);
};

export const fetchUpcomingEvents = async (): Promise<EventResponseDto[]> => {
    return apiFetch<EventResponseDto[]>("/events/upcoming");
};

export const fetchEventById = async (eventId: string): Promise<EventResponseDto> => {
    return apiFetch<EventResponseDto>(`/events/${eventId}`);
};

export const updateEvent = async (eventId: string, data: EventRequestDto): Promise<EventResponseDto> => {
    return apiFetch<EventResponseDto>(`/events/${eventId}`, {
        method: "PUT",
        body: JSON.stringify(data),
    });
};

export const deleteEvent = async (eventId: string): Promise<void> => {
    return apiFetch<void>(`/events/${eventId}`, {
        method: "DELETE",
    });
};

export const approveEvent = async (eventId: string): Promise<EventResponseDto> => {
    return apiFetch<EventResponseDto>(`/events/${eventId}/approve`, {
        method: "PUT",
    });
};

export const rejectEvent = async (eventId: string): Promise<EventResponseDto> => {
    return apiFetch<EventResponseDto>(`/events/${eventId}/reject`, {
        method: "PUT",
    });
};

export const resetToPending = async (eventId: string): Promise<EventResponseDto> => {
    return apiFetch<EventResponseDto>(`/events/${eventId}/pending`, {
        method: "PUT",
    });
};


export const joinEvent = async (eventId: string): Promise<ParticipantResponseDto> => {
    return apiFetch<ParticipantResponseDto>(`/events/${eventId}/participants/join?role=PARTICIPANT&confirmed=true`, {
        method: 'POST',
    });
};

export const leaveEvent = async (eventId: string, userId: string): Promise<void> => {
    return apiFetch<void>(`/events/${eventId}/participants/${userId}`, {
        method: 'DELETE',
    });
};