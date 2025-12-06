'use client';

import { useState, useEffect } from 'react';
import { Save, Loader2, MapPin, AlignLeft, CalendarClock } from 'lucide-react';
import { EventRequestDto, EventResponseDto } from "@/types/event.types";
import FormField from '@/components/FormField';

interface EventFormProps {
    councilId: string;
    initialData?: EventResponseDto | null;
    onSubmit: (data: EventRequestDto) => Promise<void>;
    isSubmitting: boolean;
}

export default function EventForm({ councilId, initialData, onSubmit, isSubmitting }: EventFormProps) {
    const [title, setTitle] = useState('');
    const [description, setDescription] = useState('');
    const [location, setLocation] = useState('');

    const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0]);
    const [startTime, setStartTime] = useState(new Date().toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' }));
    const [endDate, setEndDate] = useState(new Date().toISOString().split('T')[0]);
    const [endTime, setEndTime] = useState(new Date().toLocaleTimeString('pl-PL', { hour: '2-digit', minute: '2-digit' }));

    useEffect(() => {
        if (initialData) {
            setTitle(initialData.title);
            setDescription(initialData.description);
            setLocation(initialData.location || '');

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

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        const finalStartDateTime = `${startDate}T${startTime}:00`;
        const finalEndDateTime = `${endDate}T${endTime}:00`;

        if (finalStartDateTime > finalEndDateTime) {
            alert("Data zakończenia musi być późniejsza niż data rozpoczęcia.");
            return;
        }

        const payload: EventRequestDto = {
            title,
            description,
            location,
            startDate: finalStartDateTime,
            endDate: finalEndDateTime,
            councilId: councilId
        };

        onSubmit(payload);
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-8">

            <div className="bg-secondarybg rounded-xl border border-primary/10 p-6 shadow-sm">
                <h3 className="text-lg font-semibold text-foreground mb-4 flex items-center gap-2">
                    <AlignLeft className="w-5 h-5 text-secondary" />
                    Informacje podstawowe
                </h3>
                <div className="space-y-5">
                    <FormField
                        id="title"
                        label="TYTUŁ WYDARZENIA"
                        type="text"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        placeholder="np. Apel z okazji Dnia Nauczyciela"
                        disabled={isSubmitting}
                    />

                    <div className="relative">
                        <MapPin className="absolute left-3 top-[38px] h-5 w-5 text-txtcolor-300 z-10" />
                        <FormField
                            id="location"
                            label="LOKALIZACJA"
                            type="text"
                            value={location}
                            onChange={(e) => setLocation(e.target.value)}
                            placeholder="       np. Hol Główny (użyj spacji na początku dla ikony)"
                            disabled={isSubmitting}
                        />
                    </div>
                </div>
            </div>

            <div className="bg-secondarybg rounded-xl border border-primary/10 p-6 shadow-sm">
                <h3 className="text-lg font-semibold text-foreground mb-4 flex items-center gap-2">
                    <CalendarClock className="w-5 h-5 text-secondary" />
                    Termin
                </h3>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="space-y-2">
                        <label className="block text-xs font-bold text-txtcolor-300 uppercase tracking-wider">
                            Rozpoczęcie
                        </label>
                        <div className="flex gap-2">
                            <input
                                type="date"
                                required
                                value={startDate}
                                onChange={(e) => setStartDate(e.target.value)}
                                className="flex-1 bg-inputbg text-foreground rounded-lg px-4 py-3 border border-border focus:ring-2 focus:ring-primary outline-none transition-all"
                            />
                            <input
                                type="time"
                                required
                                value={startTime}
                                onChange={(e) => setStartTime(e.target.value)}
                                className="w-36 bg-inputbg text-foreground rounded-lg px-4 py-3 border border-border focus:ring-2 focus:ring-primary outline-none transition-all"
                            />
                        </div>
                    </div>

                    <div className="space-y-2">
                        <label className="block text-xs font-bold text-txtcolor-300 uppercase tracking-wider">
                            Zakończenie
                        </label>
                        <div className="flex gap-2">
                            <input
                                type="date"
                                required
                                value={endDate}
                                onChange={(e) => setEndDate(e.target.value)}
                                className="flex-1 bg-inputbg text-foreground rounded-lg px-4 py-3 border border-border focus:ring-2 focus:ring-primary outline-none transition-all"
                            />
                            <input
                                type="time"
                                required
                                value={endTime}
                                onChange={(e) => setEndTime(e.target.value)}
                                className="w-36 bg-inputbg text-foreground rounded-lg px-4 py-3 border border-border focus:ring-2 focus:ring-primary outline-none transition-all"
                            />
                        </div>
                    </div>
                </div>
            </div>

            <div className="bg-secondarybg rounded-xl border border-primary/10 p-6 shadow-sm">
                <div className="space-y-2">
                    <label className="block text-xs font-bold text-txtcolor-300 uppercase tracking-wider">
                        Opis Szczegółowy
                    </label>
                    <textarea
                        required
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        disabled={isSubmitting}
                        placeholder="Opisz plan wydarzenia, atrakcje i wymagania..."
                        className="w-full h-48 bg-inputbg text-foreground rounded-lg px-4 py-3 border border-border focus:ring-2 focus:ring-secondary outline-none resize-none scrollbar-thin transition-all"
                    />
                </div>
            </div>

            <div className="flex justify-end pt-4">
                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="bg-primary text-darkgray px-10 py-4 rounded-xl font-bold text-base flex items-center gap-3 hover:opacity-90 disabled:opacity-50 transition-all shadow-lg shadow-primary/20 hover:shadow-primary/30 transform hover:-translate-y-1"
                >
                    {isSubmitting ? <Loader2 className="h-5 w-5 animate-spin" /> : <Save className="h-5 w-5" />}
                    {initialData ? 'Zapisz Zmiany' : 'Opublikuj Wydarzenie'}
                </button>
            </div>
        </form>
    );
}