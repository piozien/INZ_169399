'use client';

import {Loader2, Lock, PartyPopper} from 'lucide-react';
import EventCard from '@/components/events/EventCard';
import EventDetailsModal from '@/components/events/EventDetailsModal';
import {usePublicEvents} from '@/hooks/events/usePublicEvents';

export default function PublicEventsPage() {
    const {events, isLoading, selectedEvent, openModal, closeModal} = usePublicEvents();

    const handleFakeJoin = () => {
        alert('Musisz się zalogować, aby dołączyć do wydarzenia!');
    };

    if (isLoading) {
        return (
            <div className="flex h-[50vh] items-center justify-center">
                <Loader2 className="text-primary h-8 w-8 animate-spin"/>
            </div>
        );
    }

    return (
        <div className="container mx-auto max-w-7xl p-6">
            <h1 className="text-foreground mb-8 text-center text-3xl font-bold">
                Nadchodzące Wydarzenia
            </h1>

            <div className="grid grid-cols-1 gap-8 md:grid-cols-3">
                {events.map((event) => (
                    <EventCard key={event.id} event={event} onClick={() => openModal(event)}/>
                ))}

                {events.length === 0 && (
                    <div className="md:col-span-3 flex min-h-[40vh] items-center justify-center p-12 text-center">
                        <div className="flex flex-col items-center">
                            <div
                                className="text-primary mb-4 rounded-full border border-border bg-secondarybg p-4"
                            >
                                <PartyPopper className="h-10 w-10"/>
                            </div>

                            <h3 className="text-foreground mb-1 text-2xl font-bold">
                                Brak nadchodzących wydarzeń
                            </h3>

                            <p className="text-txtcolor-300 max-w-sm">
                                Zaplanowane wydarzenia pojawią się wkrótce.
                            </p>
                        </div>
                    </div>
                )}
            </div>

            {selectedEvent && (
                <EventDetailsModal
                    event={selectedEvent}
                    onClose={closeModal}
                    actions={
                        <button
                            onClick={handleFakeJoin}
                            className="bg-darkgrey text-txtcolor-300 border-txtcolor-300/20 flex cursor-not-allowed items-center gap-2 rounded-lg border px-6 py-2"
                        >
                            <Lock className="h-4 w-4"/>
                            Zaloguj się, aby dołączyć
                        </button>
                    }
                />
            )}
        </div>
    );
}
