'use client';

import { useParams, useRouter } from 'next/navigation';
import { ChevronLeft, CalendarPlus } from 'lucide-react';
import EventForm from '@/components/events/EventForm';
import { useEventForm } from '@/hooks/council/events/useEventForm';

export default function CreateEventPage() {
    const params = useParams();
    const router = useRouter();

    const councilId = Array.isArray(params.id) ? params.id[0] : params.id || '';

    const { handleSubmit, isSubmitting } = useEventForm(councilId);

    if (!councilId) return <div>Błąd: Brak ID samorządu</div>;

    return (
        <div className="animate-in fade-in mx-auto max-w-5xl p-6 duration-500 md:p-10">
            <div className="mb-8">
                <button
                    onClick={() => router.back()}
                    className="text-txtcolor-300 hover:text-secondary group mb-6 flex items-center gap-1 text-sm font-medium transition-colors"
                >
                    <ChevronLeft className="h-4 w-4 transition-transform group-hover:-translate-x-1" />
                    Wróć do listy wydarzeń
                </button>

                <div className="flex items-center gap-4">
                    <div className="bg-secondarybg border-primary/10 rounded-xl border p-3">
                        <CalendarPlus className="text-secondary h-8 w-8" />
                    </div>
                    <div>
                        <h1 className="text-foreground text-3xl font-bold tracking-tight">
                            Nowe Wydarzenie
                        </h1>
                        <p className="text-txtcolor-300 mt-1">
                            Uzupełnij poniższy formularz, aby dodać kolejne wydarzenie do kalendarza
                            Twojego samorządu.
                        </p>
                    </div>
                </div>
            </div>

            <EventForm councilId={councilId} onSubmit={handleSubmit} isSubmitting={isSubmitting} />
        </div>
    );
}
