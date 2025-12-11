import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchUpcomingEvents } from '@/lib/api/events';
import { EventResponseDto } from '@/types/event.types';

export const usePublicEvents = () => {
    const [selectedEvent, setSelectedEvent] = useState<EventResponseDto | null>(null);
    const { data: events = [], isLoading } = useQuery({
        queryKey: ['publicEvents'],
        queryFn: fetchUpcomingEvents,
    });

    return {
        events,
        isLoading,
        selectedEvent,
        openModal: (event: EventResponseDto) => setSelectedEvent(event),
        closeModal: () => setSelectedEvent(null),
    };
};
