'use client';

import { useQuery } from '@tanstack/react-query';
import { Event } from '@/types/event.types';
import EventCard from '@/components/EventCard';

async function getUpcomingEvents(): Promise<Event[]> {
  const apiUrl = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';
  const response = await fetch(`${apiUrl}/api/events/upcoming`);

  if (!response.ok) {
    throw new Error('Nie udało się pobrać nadchodzących wydarzeń.');
  }
  return response.json();
}

export default function EventsPage() {
  const {
    data: events,
    isLoading,
    isError,
    error,
  } = useQuery<Event[], Error>({
    queryKey: ['upcomingEvents'],
    queryFn: getUpcomingEvents,
  });

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-4xl font-bold text-center mb-8">
        Nadchodzące wydarzenia
      </h1>

      {isLoading && (
        <p className="text-center">Ładowanie wydarzeń...</p>
      )}

      {isError && (
        <p className="text-center text-red-500">
          Wystąpił błąd: {error.message}
        </p>
      )}

      {events && (
        <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
          {events.length > 0 ? (
            events.map((event) => <EventCard key={event.id} event={event} />)
          ) : (
            <p className="text-center md:col-span-2 lg:col-span-3">
              Brak nadchodzących wydarzeń.
            </p>
          )}
        </div>
      )}
    </div>
  );
}
