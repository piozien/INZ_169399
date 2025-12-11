'use client';

import { CalendarCheck, Loader2, LogOut } from 'lucide-react';
import EventCard from '@/components/events/EventCard';
import EventDetailsModal from '@/components/events/EventDetailsModal';
import { useUserEvents } from '@/hooks/events/useUserEvents';

export default function UserDashboardEventsPage() {
    const {
        events,
        isLoading,
        selectedEvent,
        setSelectedEvent,
        closeModal,
        handleJoin,
        handleLeave,
        isParticipating,
        isProcessing,
    } = useUserEvents();

    if (isLoading) {
        return (
            <div className="flex h-[50vh] items-center justify-center">
                <Loader2 className="text-primary h-8 w-8 animate-spin" />
            </div>
        );
    }

    return (
        <div className="animate-in fade-in mx-auto max-w-7xl space-y-8 p-6 duration-500 md:p-8">
            <div className="border-secondarybg border-b pb-6">
                <h1 className="text-foreground flex items-center gap-3 text-3xl font-bold">
                    Dostępne Wydarzenia
                </h1>
                <p className="text-txtcolor-300 mt-1">
                    Przeglądaj i zapisuj się na wydarzenia w szkole.
                </p>
            </div>

            <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                {events.map((event) => (
                    <EventCard
                        key={event.id}
                        event={event}
                        onClick={() => setSelectedEvent(event)}
                    />
                ))}
                {events.length === 0 && (
                    <div className="text-txtcolor-300 bg-secondarybg/30 border-secondarybg col-span-full rounded-xl border border-dashed py-12 text-center">
                        Brak nadchodzących wydarzeń dostępnych do zapisu.
                    </div>
                )}
            </div>

            {selectedEvent && (
                <EventDetailsModal
                    event={selectedEvent}
                    onClose={closeModal}
                    actions={
                        isParticipating(selectedEvent) ? (
                            <ActionButton
                                onClick={() => handleLeave(selectedEvent.id)}
                                isLoading={isProcessing}
                                icon={LogOut}
                                label="Zrezygnuj"
                                variant="danger"
                            />
                        ) : (
                            <ActionButton
                                onClick={() => handleJoin(selectedEvent.id)}
                                isLoading={isProcessing}
                                icon={CalendarCheck}
                                label="Dołącz do wydarzenia"
                                variant="success"
                            />
                        )
                    }
                />
            )}
        </div>
    );
}

const ActionButton = ({ onClick, isLoading, icon: Icon, label, variant }: any) => {
    const styles =
        variant === 'danger'
            ? 'bg-error/10 text-error hover:bg-error hover:text-foreground border-error/20'
            : 'bg-success text-darkgray hover:bg-success/90 border-success/50 shadow-success/20';

    return (
        <button
            onClick={onClick}
            disabled={isLoading}
            className={`flex items-center gap-2 rounded-xl border px-6 py-2.5 text-sm font-bold transition-all disabled:cursor-not-allowed disabled:opacity-50 ${styles}`}
        >
            {isLoading ? (
                <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
                <Icon className="h-4 w-4" />
            )}
            {isLoading ? 'Przetwarzanie...' : label}
        </button>
    );
};
