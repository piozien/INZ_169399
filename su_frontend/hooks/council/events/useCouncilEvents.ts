import {useState, useMemo} from 'react';
import {useQuery, useMutation, useQueryClient} from '@tanstack/react-query';
import {toast} from 'sonner';
import {
    fetchCouncilEvents,
    deleteEvent,
    approveEvent,
    rejectEvent,
    resetToPending,
    removeParticipant,
} from '@/lib/api/events';
import {EventResponseDto} from '@/types/event.types';

export const useCouncilEvents = (councilId: string) => {
    const queryClient = useQueryClient();

    const [searchQuery, setSearchQuery] = useState('');
    const [statusFilter, setStatusFilter] = useState<string>('ALL');
    const [selectedEvent, setSelectedEvent] = useState<EventResponseDto | null>(null);
    const [isArchiveOpen, setIsArchiveOpen] = useState(false);

    const {data: events, isLoading} = useQuery({
        queryKey: ['councilEvents', councilId],
        queryFn: () => fetchCouncilEvents(councilId),
        enabled: !!councilId,
    });

    const {activeEvents, archiveEvents} = useMemo(() => {
        if (!events) return {activeEvents: [], archiveEvents: []};

        const now = new Date();
        const filtered = events.filter((event) => {
            const matchesSearch = event.title.toLowerCase().includes(searchQuery.toLowerCase());
            const matchesStatus = statusFilter === 'ALL' || event.status === statusFilter;
            return matchesSearch && matchesStatus;
        });

        const sorted = filtered.sort(
            (a, b) => new Date(a.startDate).getTime() - new Date(b.startDate).getTime()
        );

        const active: EventResponseDto[] = [];
        const archive: EventResponseDto[] = [];

        sorted.forEach((event) => {
            const isFinished = new Date(event.endDate) < now;
            const isRejected = event.status === 'REJECTED';
            if (isFinished || isRejected) archive.push(event);
            else active.push(event);
        });

        return {activeEvents: active, archiveEvents: archive.reverse()};
    }, [events, searchQuery, statusFilter]);

    const deleteMutation = useMutation({
        mutationFn: deleteEvent,
        onSuccess: () => {
            queryClient.invalidateQueries({queryKey: ['councilEvents', councilId]});
            setSelectedEvent(null);
            toast.success('Wydarzenie usunięte');
        },
        onError: (err: any) =>
            toast.error('Błąd usuwania', {description: err.message}),
    });

    const statusMutation = useMutation({
        mutationFn: async ({
                               id,
                               decision,
                           }: {
            id: string;
            decision: 'APPROVE' | 'REJECT' | 'PENDING';
        }) => {
            if (decision === 'APPROVE') return approveEvent(id);
            if (decision === 'REJECT') return rejectEvent(id);
            return resetToPending(id);
        },
        onSuccess: (updatedEvent) => {
            queryClient.invalidateQueries({queryKey: ['councilEvents', councilId]});
            if (selectedEvent?.id === updatedEvent.id) {
                setSelectedEvent(updatedEvent);
            }
            toast.success('Status zmieniony');
        },
        onError: () => toast.error('Nie udało się zmienić statusu.'),
    });

    const removeParticipantMutation = useMutation({
        mutationFn: ({eventId, userId}: { eventId: string; userId: string }) =>
            removeParticipant(eventId, userId),
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({queryKey: ['councilEvents', councilId]});

            if (selectedEvent && selectedEvent.id === variables.eventId) {
                setSelectedEvent((prev) => {
                    if (!prev) return null;
                    return {
                        ...prev,
                        participants: prev.participants?.filter((p) => p.userId !== variables.userId) || [],
                        participantsCount: Math.max(0, prev.participantsCount - 1),
                    };
                });
            }
            toast.success('Uczestnik usunięty z listy');
        },
        onError: (err: any) =>
            toast.error('Błąd usuwania uczestnika', {description: err.message}),
    });

    const handleDelete = (id: string) => {
        toast('Czy na pewno chcesz usunąć to wydarzenie?', {
            description: 'Operacja jest nieodwracalna.',
            action: {
                label: 'Usuń',
                onClick: () => deleteMutation.mutate(id),
            },
            cancel: {
                label: 'Anuluj',
                onClick: () => {
                },
            },
        });
    };

    const handleDecision = (id: string, decision: 'APPROVE' | 'REJECT' | 'PENDING') => {
        statusMutation.mutate({id, decision});
    };

    return {
        activeEvents,
        archiveEvents,
        isLoading,
        searchQuery, setSearchQuery,
        statusFilter, setStatusFilter,
        isArchiveOpen, setIsArchiveOpen,
        selectedEvent, setSelectedEvent,
        handleDelete,
        handleDecision,
        removeParticipant: removeParticipantMutation.mutate,
        isProcessing:
            statusMutation.isPending || deleteMutation.isPending || removeParticipantMutation.isPending,
        processingId: statusMutation.variables?.id,
    };
};