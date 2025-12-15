import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { createCouncil } from '@/lib/api/council';

interface CouncilFormData {
    name: string;
    academicYear: string;
    startDate: string;
    endDate: string;
}

export const useCreateCouncilForm = (onSuccess: () => void) => {
    const queryClient = useQueryClient();

    const [formData, setFormData] = useState<CouncilFormData>({
        name: '',
        academicYear: '',
        startDate: '',
        endDate: '',
    });

    const [error, setError] = useState<string | null>(null);

    const createMutation = useMutation({
        mutationFn: createCouncil,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['userCouncils'] });
            toast.success('Samorząd utworzony pomyślnie');
            onSuccess();
        },
        onError: (err: any) => {
            const msg = err instanceof Error ? err.message : 'Wystąpił błąd podczas tworzenia samorządu';
            setError(msg);
            toast.error('Błąd tworzenia', { description: msg });
        },
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { id, value } = e.target;
        setFormData((prev) => ({
            ...prev,
            [id]: value,
        }));
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);

        if (
            !formData.name.trim() ||
            !formData.academicYear.trim() ||
            !formData.startDate ||
            !formData.endDate
        ) {
            setError('Wszystkie pola są wymagane.');
            return;
        }

        if (new Date(formData.startDate) > new Date(formData.endDate)) {
            setError('Data rozpoczęcia nie może być późniejsza niż data zakończenia.');
            return;
        }

        createMutation.mutate(formData);
    };

    return {
        formData,
        error,
        handleChange,
        handleSubmit,
        isPending: createMutation.isPending,
    };
};