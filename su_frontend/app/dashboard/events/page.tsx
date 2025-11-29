'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchAllEvents, fetchEventsByDateRange } from '@/lib/api/events';
import { EventResponseDto } from '@/types/event.types';
import EventCard from '@/components/events/EventCard';
import { DateRange, DayPicker } from 'react-day-picker';
import 'react-day-picker/dist/style.css';
import { pl } from 'date-fns/locale';

export default function EventsPage() {
  const [isFiltering, setIsFiltering] = useState(false);
  const [dateRange, setDateRange] = useState<DateRange | undefined>();
  const [showDatePicker, setShowDatePicker] = useState(false);

  const {
    data: events,
    isLoading,
    isError,
    error,
    refetch,
  } = useQuery<EventResponseDto[], Error>({
    queryKey: ['events', dateRange],
    queryFn: () => {
      if (dateRange?.from && dateRange?.to) {
        return fetchEventsByDateRange(
          dateRange.from.toISOString(),
          dateRange.to.toISOString(),
        );
      }
      return fetchAllEvents();
    },
    enabled: !isFiltering,
  });

  const handleFilter = () => {
    if (dateRange?.from && dateRange?.to) {
      setIsFiltering(true);
      refetch().finally(() => setIsFiltering(false));
      setShowDatePicker(false);
    }
  };

  const clearFilter = () => {
    setDateRange(undefined);
    refetch();
  };

  const renderContent = () => {
    if (isLoading || isFiltering) {
      return <p>Ładowanie wydarzeń...</p>;
    }
    if (isError) {
      return (
        <p className="text-error">
          Wystąpił błąd podczas pobierania wydarzeń: {error.message}
        </p>
      );
    }
    if (events && events.length > 0) {
      return (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {events.map((event) => (
            <EventCard key={event.id} event={event} />
          ))}
        </div>
      );
    }
    return <p>Brak dostępnych wydarzeń.</p>;
  };

  return (
    <div className="container mx-auto p-4">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-4xl font-bold">Wydarzenia</h1>
        <button
          onClick={() => setShowDatePicker(!showDatePicker)}
          className="px-4 py-2 bg-primary text-background rounded-lg hover:bg-secondary transition-colors"
        >
          Filtruj wg daty
        </button>
      </div>

      {showDatePicker && (
        <div className="mb-8 p-4 bg-secondarybg rounded-lg border border-neutralbg flex flex-col items-center">
          <DayPicker
            mode="range"
            selected={dateRange}
            onSelect={setDateRange}
            numberOfMonths={2}
            locale={pl}
            classNames={{
              today: 'text-primary font-bold',
              selected: 'bg-primary text-background',
              range_start: 'bg-secondary font-bold rounded-l-[20px]',
              range_end: 'bg-secondary  font-bold rounded-r-[20px]',
              range_middle: 'bg-primary',
              chevron: 'fill-secondary',
            }}
          />
          <div className="flex gap-4 mt-4">
            <button
              onClick={handleFilter}
              className="px-4 py-2 bg-primary text-background rounded-lg hover:bg-secondary transition-colors"
            >
              Zastosuj
            </button>
            <button
              onClick={clearFilter}
              className="px-4 py-2 bg-secondarybg text-foreground rounded-lg hover:bg-border transition-colors"
            >
              Wyczyść
            </button>
          </div>
        </div>
      )}

      {renderContent()}
    </div>
  );
}
