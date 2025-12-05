'use client';

import { useQuery } from '@tanstack/react-query';
import { fetchAllEvents } from '@/lib/api/events';
import { Loader2, CalendarDays, Info } from 'lucide-react';
import { EventResponseDto } from '@/types/event.types';
import EventCard from '@/components/events/EventCard';
import { useState } from 'react';
import EventDetailsModal from '@/components/events/EventDetailsModal';

export default function DashboardEventsPage() {
    const [selectedEvent, setSelectedEvent] = useState<EventResponseDto | null>(null);
    const { data: events, isLoading, error } = useQuery<EventResponseDto[]>({
        queryKey: ['events'],
        queryFn: fetchAllEvents,
    });

    if (isLoading) {
        return <div className="flex justify-center items-center h-[50vh]"><Loader2 className="h-8 w-8 animate-spin text-primary" /></div>;
    }

    if (error) {
        return <div className="text-center p-10 text-error">Nie udało się pobrać listy wydarzeń.</div>;
    }

    return (
        <div className="p-6 space-y-8 max-w-7xl mx-auto">

            <div className="flex justify-between items-center border-b border-secondarybg pb-6">
                <div>
                    <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
                        <CalendarDays className="text-secondary h-8 w-8" />
                        Wszystkie Wydarzenia
                    </h1>
                    <p className="text-txtcolor-300 mt-1">Kalendarz szkolny i wydarzenia samorządów</p>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {events && events.length > 0 ? (
                    events.map((event) => (
                        <EventCard
                            key={event.id}
                            event={event}
                            onClick={() => setSelectedEvent(event)}
                        />
                    ))
                ) : (
                    <div className="col-span-full flex flex-col items-center justify-center py-20 text-txtcolor-300 bg-secondarybg/30 rounded-xl border border-dashed border-secondarybg">
                        <Info className="h-12 w-12 mb-4 opacity-20" />
                        <p>Brak wydarzeń.</p>
                    </div>
                )}
            </div>

            {selectedEvent && (
                <EventDetailsModal
                    event={selectedEvent}
                    onClose={() => setSelectedEvent(null)}
                    actions={
                        <div className="text-xs text-txtcolor-300 italic py-2">
                            Podgląd ogólny
                        </div>
                    }
                />
            )}
        </div>
    );
}