"use client";

import { useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { createEvent } from "@/lib/api/events";
import { EventRequestDto } from "@/types/event.types";
import EventForm from "@/components/events/EventForm";
import { ChevronLeft, CalendarPlus } from "lucide-react";

export default function CreateEventPage() {
    const params = useParams();
    const router = useRouter();
    const councilId = Array.isArray(params.id) ? params.id[0] : params.id;
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async (data: EventRequestDto) => {
        if (!councilId) return;

        setIsSubmitting(true);
        try {
            await createEvent(data);
            router.push(`/dashboard/council/${councilId}/events`);
        } catch (error) {
            console.error("Błąd tworzenia:", error);
            alert("Wystąpił błąd podczas tworzenia wydarzenia.");
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!councilId) return <div>Błąd: Brak ID samorządu</div>;

    return (
        <div className="p-6 md:p-10 max-w-5xl mx-auto">
            <div className="mb-8">
                <button
                    onClick={() => router.back()}
                    className="flex items-center gap-1 text-txtcolor-300 hover:text-secondary mb-6 text-sm font-medium transition-colors group"
                >
                    <ChevronLeft className="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
                    Wróć do listy wydarzeń
                </button>

                <div className="flex items-center gap-4">
                    <div className="p-3 bg-secondarybg rounded-xl border border-primary/10">
                        <CalendarPlus className="w-8 h-8 text-secondary" />
                    </div>
                    <div>
                        <h1 className="text-3xl font-bold text-foreground tracking-tight">Nowe Wydarzenie</h1>
                        <p className="text-txtcolor-300 mt-1">
                            Uzupełnij poniższy formularz, aby dodać kolejne wydarzenie do kalendarza Twojego samorządu.
                        </p>
                    </div>
                </div>
            </div>
            <EventForm
                councilId={councilId}
                onSubmit={handleSubmit}
                isSubmitting={isSubmitting}
            />
        </div>
    );
}