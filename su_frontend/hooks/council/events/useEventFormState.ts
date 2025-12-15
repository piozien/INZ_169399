import { useState, useEffect } from 'react';
import { toast } from 'sonner';
import { EventResponseDto, EventRequestDto } from '@/types/event.types';

export const useEventFormState = (initialData?: EventResponseDto | null) => {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [location, setLocation] = useState('');

    const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
    const [startTime, setStartTime] = useState(
        new Date().toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' })
    );
    const [endDate, setEndDate] = useState(new Date().toISOString().split('T')[0]);
    const [endTime, setEndTime] = useState(
        new Date().toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' })
    );

    const [maxParticipants, setMaxParticipants] = useState<string>('');

    useEffect(() => {
        if (initialData) {
            setTitle(initialData.title);
            setDescription(initialData.description);
            setLocation(initialData.location || '');
            if (initialData.maxParticipants) {
                setMaxParticipants(initialData.maxParticipants.toString());
            } else {
                setMaxParticipants('');
            }
            if (initialData.startDate) {
                const [datePart, timePart] = initialData.startDate.split('T');
                setStartDate(datePart);
                setStartTime(timePart ? timePart.substring(0, 5) : '08:00');
            }
            if (initialData.endDate) {
                const [datePart, timePart] = initialData.endDate.split('T');
                setEndDate(datePart);
                setEndTime(timePart ? timePart.substring(0, 5) : '16:00');
            }
        }
    }, [initialData]);

    const handleStartDateChange = (val: string) => {
        setStartDate(val);
        if (val > endDate) {
            setEndDate(val);
        }
    };

    const handleStartTimeChange = (val: string) => {
        setStartTime(val);
        if (!val) return;
        try {
            const [hours, minutes] = val.split(':').map(Number);
            const date = new Date();
            date.setHours(hours);
            date.setMinutes(minutes);
            date.setHours(date.getHours() + 1);
            const nextHour = date.getHours().toString().padStart(2, '0');
            const sameMinutes = date.getMinutes().toString().padStart(2, '0');
            setEndTime(`${nextHour}:${sameMinutes}`);
        } catch (error) {}
    };

    const getPayload = (councilId: string): EventRequestDto | null => {
        const finalStartDateTime = `${startDate}T${startTime}:00`;
        const finalEndDateTime = `${endDate}T${endTime}:00`;

        if (finalStartDateTime > finalEndDateTime) {
            toast.error('Błąd daty', {
                description: 'Data zakończenia musi być późniejsza niż data rozpoczęcia.',
            });
            return null;
        }

        const limit = maxParticipants === '' ? null : parseInt(maxParticipants, 10);

        return {
            title,
            description,
            location,
            startDate: finalStartDateTime,
            endDate: finalEndDateTime,
            councilId,
            maxParticipants: limit,
        };
    };

    return {
        title, description, location,
        startDate, startTime, endDate, endTime,
        setTitle, setDescription, setLocation, setEndDate, setEndTime,
        handleStartDateChange, handleStartTimeChange,
        maxParticipants, setMaxParticipants,
        getPayload,
    };
};