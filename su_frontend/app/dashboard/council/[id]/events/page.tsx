'use client';

import { useParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { useCouncilEvents } from '@/hooks/council/events/useCouncilEvents';
import EventCard from '@/components/events/EventCard';
import EventDetailsModal from '@/components/events/EventDetailsModal';
import {
    ChevronDown,
    ChevronUp,
    Plus,
    Edit,
    Trash2,
    Search,
    Filter,
    CheckCircle,
    XCircle,
    Loader2,
    ArrowLeft,
    Send,
    RotateCcw,
} from 'lucide-react';

export default function CouncilEventsPage() {
    const params = useParams();
    const router = useRouter();
    const councilId = Array.isArray(params.id) ? params.id[0] : params.id || '';

    const {
        activeEvents,
        archiveEvents,
        isLoading,
        searchQuery,
        setSearchQuery,
        statusFilter,
        setStatusFilter,
        isArchiveOpen,
        setIsArchiveOpen,
        selectedEvent,
        setSelectedEvent,
        handleDelete,
        handleDecision,
        isProcessing,
        processingId,
        removeParticipant,
    } = useCouncilEvents(councilId);

    if (isLoading) return <div className="text-primary p-8">Ładowanie...</div>;

    return (
        <div className="space-y-8 p-6">
            <div className="flex flex-col items-start justify-between gap-4 md:flex-row md:items-center">
                <div className="flex items-center gap-3">
                    <button
                        onClick={() => router.push(`/dashboard/council/${councilId}`)}
                        className="text-txtcolor-300 hover:text-foreground hover:bg-secondarybg -ml-2 rounded-xl p-2 transition-colors"
                        title="Powrót do samorządu"
                    >
                        <ArrowLeft className="h-6 w-6" />
                    </button>
                    <h1 className="text-foreground text-2xl font-bold">Zarządzanie Wydarzeniami</h1>
                </div>

                <Link
                    href={`/dashboard/council/${councilId}/events/create`}
                    className="bg-primary text-darkgray hover:bg-secondary flex items-center gap-2 rounded-lg px-4 py-2 font-bold shadow-md transition-all hover:shadow-lg"
                >
                    <Plus className="h-5 w-5" /> Stwórz wydarzenie
                </Link>
            </div>

            <div className="bg-secondarybg border-secondarybg flex flex-col gap-4 rounded-xl border p-4 md:flex-row">
                <div className="relative flex-1">
                    <Search className="text-txtcolor-300 absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
                    <input
                        type="text"
                        placeholder="Szukaj po nazwie..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="bg-inputbg border-secondarybg text-foreground focus:ring-primary placeholder:text-txtcolor-300 w-full rounded-lg border py-2 pr-4 pl-10 focus:ring-2 focus:outline-none"
                    />
                </div>
                <div className="relative w-full md:w-48">
                    <Filter className="text-txtcolor-300 absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        className="bg-inputbg border-secondarybg text-foreground focus:ring-primary w-full cursor-pointer appearance-none rounded-lg border py-2 pr-8 pl-10 focus:ring-2 focus:outline-none"
                    >
                        <option value="ALL">Wszystkie</option>
                        <option value="DRAFT">Szkic</option>
                        <option value="PENDING">Oczekujące</option>
                        <option value="APPROVED">Zatwierdzone</option>
                        <option value="REJECTED">Odrzucone</option>
                    </select>
                    <ChevronDown className="text-txtcolor-300 pointer-events-none absolute top-1/2 right-3 h-4 w-4 -translate-y-1/2" />
                </div>
            </div>

            <div>
                <h2 className="text-txtcolor-300 mb-4 flex items-center gap-2 text-lg text-sm font-semibold tracking-wider uppercase">
                    Aktualne i Nadchodzące{' '}
                    <span className="bg-primary/10 text-primary rounded-full px-2 py-0.5 text-xs">
                        {activeEvents.length}
                    </span>
                </h2>
                <div className="grid grid-cols-1 gap-6 md:grid-cols-3 lg:grid-cols-4">
                    {activeEvents.map((event) => (
                        <EventCard
                            key={event.id}
                            event={event}
                            onClick={() => setSelectedEvent(event)}
                            variant="admin"
                        />
                    ))}
                    {activeEvents.length === 0 && (
                        <div className="text-txtcolor-300 bg-secondarybg/30 border-secondarybg col-span-full rounded-lg border border-dashed py-10 text-center">
                            Brak wydarzeń spełniających kryteria.
                        </div>
                    )}
                </div>
            </div>

            <div className="border-secondarybg border-t pt-4">
                <button
                    onClick={() => setIsArchiveOpen(!isArchiveOpen)}
                    className="text-txtcolor-300 hover:text-primary group flex w-full items-center gap-2 transition"
                >
                    {isArchiveOpen ? (
                        <ChevronUp className="h-5 w-5" />
                    ) : (
                        <ChevronDown className="h-5 w-5" />
                    )}
                    <span className="text-lg font-semibold">Archiwum (Zakończone / Odrzucone)</span>
                    <span className="bg-secondarybg group-hover:bg-primary/10 rounded-full px-2 py-0.5 text-xs transition-colors">
                        {archiveEvents.length}
                    </span>
                </button>
                {isArchiveOpen && (
                    <div className="animate-in slide-in-from-top-2 mt-6 grid grid-cols-1 gap-6 md:grid-cols-3 lg:grid-cols-4">
                        {archiveEvents.map((event) => (
                            <EventCard
                                key={event.id}
                                event={event}
                                onClick={() => setSelectedEvent(event)}
                                variant="admin"
                            />
                        ))}
                        {archiveEvents.length === 0 && (
                            <p className="text-txtcolor-300 col-span-full py-4 text-center text-sm">
                                Puste archiwum.
                            </p>
                        )}
                    </div>
                )}
            </div>

            {selectedEvent && (
                <EventDetailsModal
                    event={selectedEvent}
                    onClose={() => setSelectedEvent(null)}
                    onRemoveParticipant={(eventId, userId) => removeParticipant({ eventId, userId })}
                    actions={
                        <>
                            {selectedEvent.status === 'PENDING' && (
                                <>
                                    <ActionButton
                                        onClick={() => handleDecision(selectedEvent.id, 'REJECT')}
                                        isLoading={
                                            isProcessing && processingId === selectedEvent.id
                                        }
                                        icon={XCircle}
                                        label="Odrzuć"
                                        color="error"
                                    />
                                    <ActionButton
                                        onClick={() => handleDecision(selectedEvent.id, 'APPROVE')}
                                        isLoading={
                                            isProcessing && processingId === selectedEvent.id
                                        }
                                        icon={CheckCircle}
                                        label="Zatwierdź"
                                        color="success"
                                    />
                                </>
                            )}
                            {selectedEvent.status === 'DRAFT' && (
                                <ActionButton
                                    onClick={() => handleDecision(selectedEvent.id, 'PENDING')}
                                    isLoading={isProcessing && processingId === selectedEvent.id}
                                    icon={Send}
                                    label="Wyślij do akceptacji"
                                    color="primary"
                                />
                            )}
                            {(selectedEvent.status === 'APPROVED' ||
                                selectedEvent.status === 'REJECTED') && (
                                <ActionButton
                                    onClick={() => handleDecision(selectedEvent.id, 'PENDING')}
                                    isLoading={isProcessing && processingId === selectedEvent.id}
                                    icon={RotateCcw}
                                    label="Przywróć"
                                    color="neutral"
                                />
                            )}

                            <div className="bg-secondarybg mx-1 h-8 w-px" />
                            <button
                                onClick={() => handleDelete(selectedEvent.id)}
                                className="text-txtcolor-300 hover:text-error hover:bg-error/10 rounded-xl p-2 transition-all"
                                title="Usuń"
                            >
                                <Trash2 className="h-5 w-5" />
                            </button>
                            <Link
                                href={`/dashboard/council/${councilId}/events/${selectedEvent.id}/edit`}
                                className="bg-primary text-darkgray hover:bg-secondary flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-bold shadow-md transition-all hover:shadow-lg"
                            >
                                <Edit className="h-4 w-4" /> Edytuj
                            </Link>
                        </>
                    }
                />
            )}
        </div>
    );
}

const ActionButton = ({ onClick, isLoading, icon: Icon, label, color }: any) => {
    const styles: any = {
        error: 'border border-error text-error hover:bg-error hover:text-foreground',
        success:
            'bg-success text-darkgray hover:bg-success/90 border border-success/50 shadow-lg shadow-success/20',
        primary: 'bg-primary text-darkgray hover:bg-secondary shadow-lg hover:shadow-lg',
        neutral:
            'border border-txtcolor-300 text-txtcolor-300 hover:text-foreground hover:bg-secondarybg',
    };
    return (
        <button
            onClick={onClick}
            disabled={isLoading}
            className={`flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-bold transition-all disabled:opacity-50 ${styles[color]}`}
        >
            {isLoading ? (
                <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
                <Icon className="h-4 w-4" />
            )}{' '}
            {label}
        </button>
    );
};