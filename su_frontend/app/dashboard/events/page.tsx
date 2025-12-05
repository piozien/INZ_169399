"use client";

import { useEffect, useState } from "react";
import { fetchUpcomingEvents, joinEvent } from "@/lib/api/events";
import { EventResponseDto } from "@/types/event.types";
import EventCard from "@/components/events/EventCard";
import EventDetailsModal from "@/components/events/EventDetailsModal";
import { CalendarCheck, Loader2 } from "lucide-react";

export default function UserDashboardEventsPage() {
    const [events, setEvents] = useState<EventResponseDto[]>([]);
    const [selectedEvent, setSelectedEvent] = useState<EventResponseDto | null>(null);
    
    const [joiningId, setJoiningId] = useState<string | null>(null);

    useEffect(() => {
        fetchUpcomingEvents().then(data => {
            setEvents(data);
        });
    }, []);

    const handleJoin = async (eventId: string) => {
        setJoiningId(eventId);
        try {
            await joinEvent(eventId);
            alert("Pomyślnie dołączono! Jeśli korzystasz z konta szkolnego, wydarzenie zostało dodane do Twojego kalendarza Outlook.");
            
            setSelectedEvent(null);
            fetchUpcomingEvents().then(setEvents);

        } catch (e: any) {
            alert(e.message || "Wystąpił błąd podczas dołączania.");
        } finally {
            setJoiningId(null);
        }
    };

    return (
        <div className="p-6">
            <h1 className="text-2xl font-bold text-foregorund mb-6">Dostępne Wydarzenia</h1>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
                {events.map(event => (
                    <EventCard
                        key={event.id}
                        event={event}
                        onClick={() => setSelectedEvent(event)}
                    />
                ))}
                {events.length === 0 && (
                    <div className="col-span-full py-12 text-center text-txtcolor-300 bg-secondarybg/30 rounded-xl border border-dashed border-secondarybg">
                        Brak nadchodzących wydarzeń dostępnych do zapisu.
                    </div>
                )}
            </div>
            
            {selectedEvent && (
                <EventDetailsModal
                    event={selectedEvent}
                    onClose={() => setSelectedEvent(null)}
                    actions={
                        <button
                            onClick={() => handleJoin(selectedEvent.id)}
                            disabled={joiningId === selectedEvent.id}
                            className="flex items-center gap-2 px-6 py-2.5 rounded-xl bg-success text-darkgray hover:bg-success/90 border border-success/50 transition-all font-bold text-sm shadow-lg shadow-success/20 disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {joiningId === selectedEvent.id ? (
                                <>
                                    <Loader2 className="w-4 h-4 animate-spin" />
                                    Przetwarzanie...
                                </>
                            ) : (
                                <>
                                    <CalendarCheck className="w-4 h-4" />
                                    Dołącz do wydarzenia
                                </>
                            )}
                        </button>
                    }
                />
            )}
        </div>
    );
}