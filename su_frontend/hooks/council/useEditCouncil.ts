import { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { updateCouncil } from '@/lib/api/council';
import { CouncilResponseDto, CouncilRequestDto } from '@/types/council.types';
import { format } from 'date-fns';

const toInputDate = (dateString?: string) => {
    if (!dateString) return '';
    return format(new Date(dateString), 'yyyy-MM-dd');
};

export const useEditCouncil = (
    council: CouncilResponseDto,
    onClose: () => void,
    isOpen: boolean
) => {
    const queryClient = useQueryClient();

    const [name, setName] = useState(council.name);
    const [academicYear, setAcademicYear] = useState(council.academicYear);

    const [startDate, setStartDate] = useState(toInputDate(council.startDate));
    const [endDate, setEndDate] = useState(toInputDate(council.endDate));

    const [active, setActive] = useState(council.active);
    const [defaultCouncil, setDefaultCouncil] = useState(council.defaultCouncil);

    useEffect(() => {
        if (isOpen) {
            setName(council.name);
            setAcademicYear(council.academicYear);
            setStartDate(toInputDate(council.startDate));
            setEndDate(toInputDate(council.endDate));
            setActive(council.active);
            setDefaultCouncil(council.defaultCouncil);
        }
    }, [council, isOpen]);

    const mutation = useMutation({
        mutationFn: (data: CouncilRequestDto) => updateCouncil(council.id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['council', council.id] });
            queryClient.invalidateQueries({ queryKey: ['userCouncils'] });
            onClose();
        },
        onError: (err) => alert(err instanceof Error ? err.message : 'Błąd edycji'),
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        const apiStartDate = startDate
            ? new Date(startDate).toISOString()
            : new Date().toISOString();
        const apiEndDate = endDate ? new Date(endDate).toISOString() : new Date().toISOString();

        mutation.mutate({
            name,
            academicYear,
            startDate: apiStartDate,
            endDate: apiEndDate,
            active,
            defaultCouncil,
        });
    };

    const toggleArchive = () => {
        if (active) {
            if (
                confirm(
                    'Czy na pewno chcesz zarchiwizować ten samorząd? Nie będzie on już domyślny.'
                )
            ) {
                setActive(false);
                setDefaultCouncil(false);
            }
        } else {
            setActive(true);
        }
    };

    const toggleDefault = () => {
        if (!active) return;
        if (!defaultCouncil) {
            setDefaultCouncil(true);
            setActive(true);
        } else {
            setDefaultCouncil(false);
        }
    };

    return {
        name,
        setName,
        academicYear,
        setAcademicYear,
        startDate,
        setStartDate,
        endDate,
        setEndDate,
        active,
        toggleArchive,
        defaultCouncil,
        toggleDefault,
        handleSubmit,
        isPending: mutation.isPending,
    };
};
