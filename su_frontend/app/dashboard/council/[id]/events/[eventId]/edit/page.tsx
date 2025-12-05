"use client";

import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { fetchEventById, updateEvent } from "@/lib/api/events";
import { EventRequestDto, EventResponseDto } from "@/types/event.types";
import EventForm from "@/components/events/EventForm";
import { ChevronLeft, Edit3 } from "lucide-react";

export default function EditEventPage() {
    const params = useParams();
    const router = useRouter();

    const councilId = Array.isArray(params.id) ? params.id[0] : params.id;
    const eventId = Array.isArray(params.eventId) ? params.eventId[0] : params.eventId;

    const [event, setEvent] = useState<EventResponseDto | null>(null);
    const [loading, setLoading] = useState(true);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        if (eventId) {
            fetchEventById(eventId)
                .then(data => setEvent(data))
                .catch(err => console.error("Nie znaleziono wydarzenia", err))
                .finally(() => setLoading(false));
        }
    }, [eventId]);

    const handleSubmit = async (data: EventRequestDto) => {
        if (!eventId || !councilId) return;

        setIsSubmitting(true);
        try {
            await updateEvent(eventId, data);
            router.push(`/dashboard/council/${councilId}/events`);
        } catch (error) {
            console.error("Błąd edycji:", error);
            alert("Nie udało się zaktualizować wydarzenia.");
        } finally {
            setIsSubmitting(false);
        }
    };

    if (loading) return <div className="p-10 text-center text-primary">Ładowanie danych...</div>;
    if (!event || !councilId) return <div className="p-10 text-center text-error">Nie znaleziono wydarzenia.</div>;

    return (
        <div className="p-6 md:p-10 max-w-5xl mx-auto">
            <div className="mb-8">
                <button
                    onClick={() => router.back()}
                    className="flex items-center gap-1 text-txtcolor-300 hover:text-secondary mb-6 text-sm font-medium transition-colors group"
                >
                    <ChevronLeft className="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
                    Anuluj edycję
                </button>

                <div className="flex items-center gap-4">
                    <div className="p-3 bg-secondarybg rounded-xl border border-primary/10">
                        <Edit3 className="w-8 h-8 text-secondary" />
                    </div>
                    <div>
                        <h1 className="text-3xl font-bold text-foreground tracking-tight">Edycja Wydarzenia</h1>
                        <p className="text-txtcolor-300 mt-1">
                            Edytujesz wydarzenie: <span className="text-foreground font-semibold">{event.title}</span>
                        </p>
                    </div>
                </div>
            </div>

            <EventForm
                councilId={councilId}
                initialData={event}
                onSubmit={handleSubmit}
                isSubmitting={isSubmitting}
            />
        </div>
    );
}