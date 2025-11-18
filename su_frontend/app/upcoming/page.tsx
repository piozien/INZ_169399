'use client';

import { useQuery } from '@tanstack/react-query';
import { fetchUpcomingEvents } from '@/lib/api/events';
import EventCard from '@/components/events/EventCard';
import Link from 'next/link';

export default function UpcomingEventsPage() {
  const {
    data: events,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ['upcomingEvents'],
    queryFn: fetchUpcomingEvents,
  });

  return (
    <div className="container mx-auto p-4">
      <header className="mb-8 flex items-center justify-between">
        <h1 className="text-4xl font-bold">Nadchodzące Wydarzenia</h1>
      </header>
      
      {isLoading && <p>Ładowanie wydarzeń...</p>}
      {isError && <p>Wystąpił błąd podczas ładowania wydarzeń.</p>}

      <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
        {events?.map((event) => (
          <EventCard key={event.id} event={event} />
        ))}
      </div>
    </div>
  );
}
