"use client";

import { useEffect, useState, useMemo } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { fetchCouncilEvents, deleteEvent, approveEvent, rejectEvent } from "@/lib/api/events";
import { EventResponseDto } from "@/types/event.types";
import EventCard from "@/components/events/EventCard";
import EventDetailsModal from "@/components/events/EventDetailsModal";
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
    Loader2
} from "lucide-react";

export default function CouncilEventsPage() {
    const params = useParams();
    const councilId = Array.isArray(params.id) ? params.id[0] : params.id;

    const [events, setEvents] = useState<EventResponseDto[]>([]);
    const [selectedEvent, setSelectedEvent] = useState<EventResponseDto | null>(null);
    const [isArchiveOpen, setIsArchiveOpen] = useState(false);
    const [loading, setLoading] = useState(true);
    const [processingId, setProcessingId] = useState<string | null>(null);
    const [searchQuery, setSearchQuery] = useState("");
    const [statusFilter, setStatusFilter] = useState<string>("ALL");

    useEffect(() => {
        if (councilId) {
            fetchCouncilEvents(councilId).then(data => {
                setEvents(data);
                setLoading(false);
            });
        }
    }, [councilId]);

    const handleDelete = async (id: string) => {
        if(confirm("Czy na pewno chcesz usunąć to wydarzenie?")) {
            await deleteEvent(id);
            setEvents(prev => prev.filter(e => e.id !== id));
            setSelectedEvent(null);
        }
    }

    const handleDecision = async (eventId: string, decision: 'APPROVE' | 'REJECT') => {
        setProcessingId(eventId);
        try {
            let updatedEvent: EventResponseDto;

            if (decision === 'APPROVE') {
                updatedEvent = await approveEvent(eventId);
            } else {
                updatedEvent = await rejectEvent(eventId);
            }

            setEvents(prev => prev.map(e => e.id === eventId ? updatedEvent : e));
            setSelectedEvent(null);

        } catch (error) {
            console.error(error);
            alert('Wystąpił błąd podczas przetwarzania decyzji.');
        } finally {
            setProcessingId(null);
        }
    };
    
    const { activeEvents, archiveEvents } = useMemo(() => {
        const now = new Date();

        const filtered = events.filter(event => {
            const matchesSearch = event.title.toLowerCase().includes(searchQuery.toLowerCase());
            const matchesStatus = statusFilter === "ALL" || event.status === statusFilter;
            return matchesSearch && matchesStatus;
        });

        const sorted = filtered.sort((a, b) =>
            new Date(a.startDate).getTime() - new Date(b.startDate).getTime()
        );

        const active: EventResponseDto[] = [];
        const archive: EventResponseDto[] = [];

        sorted.forEach(event => {
            const isFinished = new Date(event.endDate) < now;
            const isRejected = event.status === 'REJECTED';

            if (isFinished || isRejected) {
                archive.push(event);
            } else {
                active.push(event);
            }
        });

        archive.reverse();

        return { activeEvents: active, archiveEvents: archive };
    }, [events, searchQuery, statusFilter]);


    if (loading) return <div className="p-8 text-primary">Ładowanie...</div>;

    return (
        <div className="p-6 space-y-8">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <h1 className="text-2xl font-bold text-foregorund">Zarządzanie Wydarzeniami</h1>

                <Link
                    href={`/dashboard/council/${councilId}/events/create`}
                    className="flex items-center gap-2 px-4 py-2 bg-primary text-darkgray hover:bg-secondary rounded-lg font-bold shadow-md hover:shadow-lg transition-all"
                >
                    <Plus className="w-5 h-5" />
                    Stwórz wydarzenie
                </Link>
            </div>
            
            <div className="flex flex-col md:flex-row gap-4 bg-secondarybg p-4 rounded-xl border border-secondarybg">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-txtcolor-300 w-4 h-4" />
                    <input
                        type="text"
                        placeholder="Szukaj po nazwie..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="w-full pl-10 pr-4 py-2 rounded-lg bg-inputbg border border-secondarybg text-foregorund focus:outline-none focus:ring-2 focus:ring-primary placeholder:text-txtcolor-300"
                    />
                </div>

                <div className="relative w-full md:w-48">
                    <Filter className="absolute left-3 top-1/2 -translate-y-1/2 text-txtcolor-300 w-4 h-4" />
                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        className="w-full pl-10 pr-8 py-2 rounded-lg bg-inputbg border border-secondarybg text-foregorund focus:outline-none focus:ring-2 focus:ring-primary appearance-none cursor-pointer"
                    >
                        <option value="ALL">Wszystkie</option>
                        <option value="DRAFT">Szkic</option>
                        <option value="APPROVED">Zatwierdzone</option>
                        <option value="PENDING">Oczekujące</option>
                        <option value="REJECTED">Odrzucone</option>
                    </select>
                    <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 text-txtcolor-300 w-4 h-4 pointer-events-none" />
                </div>
            </div>
            
            <div>
                <h2 className="text-lg font-semibold text-txtcolor-300 mb-4 uppercase tracking-wider text-sm flex items-center gap-2">
                    Aktualne i Nadchodzące
                    <span className="bg-primary/10 text-primary px-2 py-0.5 rounded-full text-xs">
                        {activeEvents.length}
                    </span>
                </h2>

                <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6">
                    {activeEvents.map(event => (
                        <EventCard
                            key={event.id}
                            event={event}
                            onClick={() => setSelectedEvent(event)}
                            variant="admin"
                        />
                    ))}
                    {activeEvents.length === 0 && (
                        <div className="col-span-full py-10 text-center text-txtcolor-300 bg-secondarybg/30 rounded-lg border border-dashed border-secondarybg">
                            Brak wydarzeń spełniających kryteria.
                        </div>
                    )}
                </div>
            </div>
            
            <div className="border-t border-secondarybg pt-4">
                <button
                    onClick={() => setIsArchiveOpen(!isArchiveOpen)}
                    className="flex items-center gap-2 text-txtcolor-300 hover:text-primary transition w-full group"
                >
                    {isArchiveOpen ? <ChevronUp className="w-5 h-5"/> : <ChevronDown className="w-5 h-5"/>}
                    <span className="font-semibold text-lg">Archiwum (Zakończone / Odrzucone)</span>
                    <span className="bg-secondarybg group-hover:bg-primary/10 text-xs px-2 py-0.5 rounded-full transition-colors">
                        {archiveEvents.length}
                    </span>
                </button>

                {isArchiveOpen && (
                    <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-4 gap-6 mt-6 animate-in slide-in-from-top-2">
                        {archiveEvents.map(event => (
                            <EventCard
                                key={event.id}
                                event={event}
                                onClick={() => setSelectedEvent(event)}
                                variant="admin"
                            />
                        ))}
                        {archiveEvents.length === 0 && <p className="text-txtcolor-300 text-sm col-span-full text-center py-4">Puste archiwum.</p>}
                    </div>
                )}
            </div>
            {selectedEvent && (
                <EventDetailsModal
                    event={selectedEvent}
                    onClose={() => setSelectedEvent(null)}
                    actions={
                        <>
                            {selectedEvent.status !== 'APPROVED' && selectedEvent.status !== 'REJECTED' && (
                                <>
                                    <button
                                        onClick={() => handleDecision(selectedEvent.id, 'REJECT')}
                                        disabled={processingId === selectedEvent.id}
                                        className="px-4 py-2 rounded-xl border border-error text-error hover:bg-error hover:text-foreground transition-all font-bold text-sm flex items-center gap-2 disabled:opacity-50"
                                    >
                                        {processingId === selectedEvent.id ? <Loader2 className="animate-spin w-4 h-4"/> : <XCircle className="w-4 h-4" />}
                                        Odrzuć
                                    </button>
                                    <button
                                        onClick={() => handleDecision(selectedEvent.id, 'APPROVE')}
                                        disabled={processingId === selectedEvent.id}
                                        className="px-5 py-2 rounded-xl bg-success text-darkgrey hover:bg-success/90 border border-success/50 transition-all font-bold text-sm flex items-center gap-2 disabled:opacity-50 shadow-lg shadow-success/20"
                                    >
                                        {processingId === selectedEvent.id ? <Loader2 className="animate-spin w-4 h-4"/> : <CheckCircle className="w-4 h-4" />}
                                        Zatwierdź
                                    </button>
                                </>
                            )}
                            {(selectedEvent.status !== 'APPROVED' && selectedEvent.status !== 'REJECTED') && (
                                <div className="w-px h-8 bg-secondarybg mx-1" />
                            )}

                            <button
                                onClick={() => handleDelete(selectedEvent.id)}
                                className="p-2 rounded-xl text-txtcolor-300 hover:text-error hover:bg-error/10 transition-all"
                                title="Usuń"
                            >
                                <Trash2 className="w-5 h-5" />
                            </button>

                            <Link
                                href={`/dashboard/council/${councilId}/events/${selectedEvent.id}/edit`}
                                className="px-4 py-2 rounded-xl bg-primary text-darkgray hover:bg-secondary transition-all font-bold text-sm flex items-center gap-2 shadow-md hover:shadow-lg"
                            >
                                <Edit className="w-4 h-4" /> Edytuj
                            </Link>
                        </>
                    }
                />
            )}
        </div>
    );
}