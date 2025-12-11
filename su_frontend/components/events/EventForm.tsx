'use client';

import { Save, Loader2, MapPin, AlignLeft, CalendarClock } from 'lucide-react';
import { EventRequestDto, EventResponseDto } from '@/types/event.types';
import FormField from '@/components/FormField';
import { useEventFormState } from '@/hooks/council/events/useEventFormState';

interface EventFormProps {
    councilId: string;
    initialData?: EventResponseDto | null;
    onSubmit: (data: EventRequestDto) => void;
    isSubmitting: boolean;
}

export default function EventForm({
    councilId,
    initialData,
    onSubmit,
    isSubmitting,
}: EventFormProps) {
    const {
        title,
        setTitle,
        description,
        setDescription,
        location,
        setLocation,
        startDate,
        startTime,
        endDate,
        endTime,
        setEndDate,
        setEndTime,
        handleStartDateChange,
        handleStartTimeChange,
        getPayload,
    } = useEventFormState(initialData);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        const payload = getPayload(councilId);

        if (payload) {
            onSubmit(payload);
        }
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-8">
            <div className="bg-secondarybg border-primary/10 rounded-xl border p-6 shadow-sm">
                <h3 className="text-foreground mb-4 flex items-center gap-2 text-lg font-semibold">
                    <AlignLeft className="text-secondary h-5 w-5" /> Informacje podstawowe
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
                        <MapPin className="text-txtcolor-300 absolute top-[38px] left-3 z-10 h-5 w-5" />
                        <FormField
                            id="location"
                            label="LOKALIZACJA"
                            type="text"
                            value={location}
                            onChange={(e) => setLocation(e.target.value)}
                            placeholder="       np. Hol Główny"
                            disabled={isSubmitting}
                        />
                    </div>
                </div>
            </div>

            <div className="bg-secondarybg border-primary/10 rounded-xl border p-6 shadow-sm">
                <h3 className="text-foreground mb-4 flex items-center gap-2 text-lg font-semibold">
                    <CalendarClock className="text-secondary h-5 w-5" /> Termin
                </h3>
                <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                    <div className="space-y-2">
                        <label className="text-txtcolor-300 block text-xs font-bold tracking-wider uppercase">
                            Rozpoczęcie
                        </label>
                        <div className="flex gap-2">
                            <input
                                type="date"
                                required
                                value={startDate}
                                onChange={(e) => handleStartDateChange(e.target.value)}
                                className="bg-inputbg text-foreground border-border focus:ring-primary flex-1 rounded-lg border px-4 py-3 transition-all outline-none focus:ring-2"
                            />
                            <input
                                type="time"
                                required
                                value={startTime}
                                onChange={(e) => handleStartTimeChange(e.target.value)}
                                className="bg-inputbg text-foreground border-border focus:ring-primary w-36 rounded-lg border px-4 py-3 transition-all outline-none focus:ring-2"
                            />
                        </div>
                    </div>
                    <div className="space-y-2">
                        <label className="text-txtcolor-300 block text-xs font-bold tracking-wider uppercase">
                            Zakończenie
                        </label>
                        <div className="flex gap-2">
                            <input
                                type="date"
                                required
                                value={endDate}
                                onChange={(e) => setEndDate(e.target.value)}
                                className="bg-inputbg text-foreground border-border focus:ring-primary flex-1 rounded-lg border px-4 py-3 transition-all outline-none focus:ring-2"
                            />
                            <input
                                type="time"
                                required
                                value={endTime}
                                onChange={(e) => setEndTime(e.target.value)}
                                className="bg-inputbg text-foreground border-border focus:ring-primary w-36 rounded-lg border px-4 py-3 transition-all outline-none focus:ring-2"
                            />
                        </div>
                    </div>
                </div>
            </div>

            <div className="bg-secondarybg border-primary/10 rounded-xl border p-6 shadow-sm">
                <div className="space-y-2">
                    <label className="text-txtcolor-300 block text-xs font-bold tracking-wider uppercase">
                        Opis Szczegółowy
                    </label>
                    <textarea
                        required
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        disabled={isSubmitting}
                        placeholder="Opisz plan wydarzenia, atrakcje i wymagania..."
                        className="bg-inputbg text-foreground border-border focus:ring-secondary scrollbar-thin h-48 w-full resize-none rounded-lg border px-4 py-3 transition-all outline-none focus:ring-2"
                    />
                </div>
            </div>

            <div className="flex justify-end pt-4">
                <button
                    type="submit"
                    disabled={isSubmitting}
                    className="bg-primary text-darkgray shadow-primary/20 flex transform items-center gap-3 rounded-xl px-10 py-4 text-base font-bold shadow-lg transition-all hover:-translate-y-1 hover:opacity-90 disabled:opacity-50"
                >
                    {isSubmitting ? (
                        <Loader2 className="h-5 w-5 animate-spin" />
                    ) : (
                        <Save className="h-5 w-5" />
                    )}
                    {initialData ? 'Zapisz Zmiany' : 'Opublikuj Wydarzenie'}
                </button>
            </div>
        </form>
    );
}
