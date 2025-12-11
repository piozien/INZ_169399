'use client';

import { useParams, useRouter } from 'next/navigation';
import { ChevronLeft, Edit3, Loader2 } from 'lucide-react';
import EventForm from '@/components/events/EventForm';
import { useEventForm } from '@/hooks/council/events/useEventForm';

export default function EditEventPage() {
    const params = useParams();
    const router = useRouter();

    const councilId = Array.isArray(params.id) ? params.id[0] : params.id || '';
    const eventId = Array.isArray(params.eventId) ? params.eventId[0] : params.eventId || '';

    const { event, isLoading, isSubmitting, handleSubmit } = useEventForm(councilId, eventId);

    if (isLoading) {
        return (
            <div className="flex h-[50vh] items-center justify-center">
                <Loader2 className="text-primary h-8 w-8 animate-spin" />
            </div>
        );
    }

    if (!event || !councilId) {
        return <div className="text-error p-10 text-center">Nie znaleziono wydarzenia.</div>;
    }

    return (
        <div className="animate-in fade-in mx-auto max-w-5xl p-6 duration-500 md:p-10">
            <div className="mb-8">
                <button
                    onClick={() => router.back()}
                    className="text-txtcolor-300 hover:text-secondary group mb-6 flex items-center gap-1 text-sm font-medium transition-colors"
                >
                    <ChevronLeft className="h-4 w-4 transition-transform group-hover:-translate-x-1" />
                    Anuluj edycję
                </button>

                <div className="flex items-center gap-4">
                    <div className="bg-secondarybg border-primary/10 rounded-xl border p-3">
                        <Edit3 className="text-warning h-8 w-8" />
                    </div>
                    <div>
                        <h1 className="text-foreground text-3xl font-bold tracking-tight">
                            Edycja Wydarzenia
                        </h1>
                        <p className="text-txtcolor-300 mt-1">
                            Edytujesz wydarzenie:{' '}
                            <span className="text-foreground font-semibold">{event.title}</span>
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
