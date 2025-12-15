import { useRouter } from 'next/navigation';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchEventById, createEvent, updateEvent } from '@/lib/api/events';
import { EventRequestDto } from '@/types/event.types';

export const useEventForm = (councilId: string, eventId?: string) => {
    const router = useRouter();
    const queryClient = useQueryClient();
    const isEditing = !!eventId;

    const { data: event, isLoading: isFetching } = useQuery({
        queryKey: ['event', eventId],
        queryFn: () => fetchEventById(eventId!),
        enabled: isEditing,
        retry: 1,
    });

    const createMutation = useMutation({
        mutationFn: (data: EventRequestDto) => createEvent({ ...data, councilId }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['councilEvents', councilId] });
            router.push(`/dashboard/council/${councilId}/events`);
        },
        onError: (err) =>
            alert('Błąd tworzenia: ' + (err instanceof Error ? err.message : 'Nieznany błąd')),
    });

    const updateMutation = useMutation({
        mutationFn: (data: EventRequestDto) => updateEvent(eventId!, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['councilEvents', councilId] });
            queryClient.invalidateQueries({ queryKey: ['event', eventId] });
            router.push(`/dashboard/council/${councilId}/events`);
        },
        onError: (err) =>
            alert('Błąd edycji: ' + (err instanceof Error ? err.message : 'Nieznany błąd')),
    });

    const handleSubmit = (formData: EventRequestDto) => {
        if (isEditing) {
            updateMutation.mutate(formData);
        } else {
            createMutation.mutate(formData);
        }
    };

    return {
        event,
        isLoading: isEditing ? isFetching : false,
        isSubmitting: createMutation.isPending || updateMutation.isPending,
        handleSubmit,
        isEditing,
    };
};
