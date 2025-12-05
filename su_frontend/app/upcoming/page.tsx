"use client";

import { useEffect, useState } from "react";
import { fetchUpcomingEvents } from "@/lib/api/events";
import { EventResponseDto } from "@/types/event.types";
import EventCard from "@/components/events/EventCard";
import EventDetailsModal from "@/components/events/EventDetailsModal";
import { Lock } from "lucide-react";

export default function PublicEventsPage() {
    const [events, setEvents] = useState<EventResponseDto[]>([]);
    const [selectedEvent, setSelectedEvent] = useState<EventResponseDto | null>(null);

    useEffect(() => {
        fetchUpcomingEvents().then(data => {
            setEvents(data);
        });
    }, []);

    const handleFakeJoin = () => {
        alert("Musisz się zalogować, aby dołączyć do wydarzenia!");
    };

    return (
        <div className="container mx-auto p-6 max-w-7xl">
            <h1 className="text-3xl font-bold text-foreground mb-8 text-center">Nadchodzące Wydarzenia</h1>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                {events.map(event => (
                    <EventCard
                        key={event.id}
                        event={event}
                        onClick={() => setSelectedEvent(event)}
                    />
                ))}
                {events.length === 0 && <p className="text-center w-full text-txtcolor-300">Brak wydarzeń.</p>}
            </div>
            
            {selectedEvent && (
                <EventDetailsModal
                    event={selectedEvent}
                    onClose={() => setSelectedEvent(null)}
                    actions={
                        <button
                            onClick={handleFakeJoin}
                            className="flex items-center gap-2 px-6 py-2 rounded-lg bg-darkgrey text-txtcolor-300 cursor-not-allowed border border-txtcolor-300/20"
                        >
                            <Lock className="w-4 h-4" />
                            Zaloguj się, aby dołączyć
                        </button>
                    }
                />
            )}
        </div>
    );
}