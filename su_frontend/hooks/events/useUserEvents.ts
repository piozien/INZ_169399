import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { fetchUpcomingEvents, joinEvent, leaveEvent } from '@/lib/api/events';
import { useAuth } from '@/lib/contexts/AuthContext';
import { EventResponseDto } from '@/types/event.types';

export const useUserEvents = () => {
    const { user } = useAuth();
    const queryClient = useQueryClient();

    const [selectedEvent, setSelectedEvent] = useState<EventResponseDto | null>(null);

    const { data: events = [], isLoading } = useQuery({
        queryKey: ['upcomingEvents'],
        queryFn: fetchUpcomingEvents,
    });

    const isParticipating = (event: EventResponseDto) => {
        if (!user || !event.participants) return false;
        return event.participants.some((p) => p.userId === user.id);
    };

    const joinMutation = useMutation({
        mutationFn: joinEvent,
        onMutate: () => {
            const toastId = toast.loading('Zapisywanie...');
            return { toastId };
        },
        onSuccess: async (_, __, context) => {
            await queryClient.invalidateQueries({ queryKey: ['upcomingEvents'] });
            toast.dismiss(context?.toastId);
            toast.success('Pomyślnie dołączono!', {
                description: 'Zaproszenie zostało wysłane na email.',
            });
            closeModal();
        },
        onError: (err, _, context) => {
            toast.dismiss(context?.toastId);
            toast.error('Nie udało się dołączyć', {
                description: err instanceof Error ? err.message : 'Błąd',
            });
        },
    });

    const leaveMutation = useMutation({
        mutationFn: (eventId: string) => {
            if (!user) throw new Error('Nie jesteś zalogowany');
            return leaveEvent(eventId, user.id);
        },
        onSuccess: async () => {
            await queryClient.invalidateQueries({ queryKey: ['upcomingEvents'] });
            toast.info('Opuszczono wydarzenie.');
            closeModal();
        },
        onError: (err) =>
            toast.error('Nie udało się zrezygnować', {
                description: err instanceof Error ? err.message : 'Błąd',
            }),
    });

    const handleJoin = (eventId: string) => {
        joinMutation.mutate(eventId);
    };

    const handleLeave = (eventId: string) => {
        toast('Czy na pewno chcesz zrezygnować?', {
            description: 'Stracisz miejsce na liście uczestników.',
            action: {
                label: 'Tak, rezygnuję',
                onClick: () => leaveMutation.mutate(eventId),
            },
            cancel: {
                label: 'Anuluj',
                onClick: () => {},
            },
        });
    };

    const closeModal = () => setSelectedEvent(null);

    const activeEventDetails = selectedEvent
        ? events.find((e) => e.id === selectedEvent.id) || selectedEvent
        : null;

    return {
        events,
        isLoading,
        selectedEvent: activeEventDetails,
        setSelectedEvent,
        closeModal,
        handleJoin,
        handleLeave,
        isParticipating,
        isProcessing: joinMutation.isPending || leaveMutation.isPending,
        processingId: joinMutation.variables || leaveMutation.variables,
    };
};