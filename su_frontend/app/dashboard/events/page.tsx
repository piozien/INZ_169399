"use client";

import { useEffect, useState } from "react";
import { fetchUpcomingEvents, joinEvent, leaveEvent } from "@/lib/api/events";
import { EventResponseDto } from "@/types/event.types";
import EventCard from "@/components/events/EventCard";
import EventDetailsModal from "@/components/events/EventDetailsModal";
import { CalendarCheck, Loader2, LogOut } from "lucide-react";
import { useAuth } from "@/lib/contexts/AuthContext";

export default function UserDashboardEventsPage() {
    const { user } = useAuth();
    const [events, setEvents] = useState<EventResponseDto[]>([]);
    const [selectedEvent, setSelectedEvent] = useState<EventResponseDto | null>(null);

    const [processingId, setProcessingId] = useState<string | null>(null);

    const loadEvents = async () => {
        try {
            const data = await fetchUpcomingEvents();
            setEvents(data);
            if (selectedEvent) {
                const updatedEvent = data.find(e => e.id === selectedEvent.id);
                if (updatedEvent) {
                    setSelectedEvent(updatedEvent);
                }
            }
        } catch (error) {
            console.error("Błąd pobierania wydarzeń:", error);
        }
    };

    useEffect(() => {
        loadEvents();
    }, []);

    const isParticipating = (event: EventResponseDto) => {
        if (!user || !event.participants) return false;
        return event.participants.some(p => p.userId === user.id);
    };

    const handleJoin = async (eventId: string) => {
        setProcessingId(eventId);
        try {
            await joinEvent(eventId);
            alert("Pomyślnie dołączono! Zaproszenie do wydarzenia zostało wysłane na Twój adres email.");
            await loadEvents();
        } catch (error) {
            const msg = error instanceof Error ? error.message : "Wystąpił błąd podczas dołączania.";
            alert(`Nie udało się dołączyć: ${msg}`);
        } finally {
            setProcessingId(null);
        }
    };

    const handleLeave = async (eventId: string) => {
        if (!user) return;

        if(!confirm("Czy na pewno chcesz zrezygnować z udziału w tym wydarzeniu?")) return;

        setProcessingId(eventId);
        try {
            await leaveEvent(eventId, user.id);
            alert("Opuszczono wydarzenie.");
            await loadEvents();
        } catch (error) {
            const msg = error instanceof Error ? error.message : "Wystąpił błąd podczas opuszczania wydarzenia.";
            alert(`Nie udało się zrezygnować: ${msg}`);
        } finally {
            setProcessingId(null);
        }
    };

    return (
        <div className="p-6 md:p-8 max-w-7xl mx-auto space-y-8">
            <div className="border-b border-secondarybg pb-6">
                <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
                    Dostępne Wydarzenia
                </h1>
                <p className="text-txtcolor-300 mt-1">
                    Przeglądaj i zapisuj się na wydarzenia w szkole.
                </p>
            </div>

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
                        isParticipating(selectedEvent) ? (
                            <button
                                onClick={() => handleLeave(selectedEvent.id)}
                                disabled={processingId === selectedEvent.id}
                                className="flex items-center gap-2 px-6 py-2.5 rounded-xl bg-error/10 text-error hover:bg-error hover:text-foreground border border-error/20 transition-all font-bold text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {processingId === selectedEvent.id ? (
                                    <>
                                        <Loader2 className="w-4 h-4 animate-spin" />
                                        Przetwarzanie...
                                    </>
                                ) : (
                                    <>
                                        <LogOut className="w-4 h-4" />
                                        Zrezygnuj
                                    </>
                                )}
                            </button>
                        ) : (
                            <button
                                onClick={() => handleJoin(selectedEvent.id)}
                                disabled={processingId === selectedEvent.id}
                                className="flex items-center gap-2 px-6 py-2.5 rounded-xl bg-success text-darkgray hover:bg-success/90 border border-success/50 transition-all font-bold text-sm shadow-lg shadow-success/20 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {processingId === selectedEvent.id ? (
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
                        )
                    }
                />
            )}
        </div>
    );
}