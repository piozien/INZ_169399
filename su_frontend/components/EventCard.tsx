import { Event } from '@/types/event.types';
import { Calendar, MapPin } from 'lucide-react';

function formatDate(dateString: string) {
  const date = new Date(dateString);
  return date.toLocaleDateString('pl-PL', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function EventCard({ event }: { event: Event }) {
  return (
    <div className="bg-secondarybg rounded-lg p-6 flex flex-col h-full shadow-lg hover:ring-2 hover:ring-secondary transition-all duration-300 ease-in-out">
      <h2 className="text-2xl font-bold text-primary mb-2">{event.title}</h2>
      <p className="text-txtcolor-300 mb-4 flex-grow">{event.description}</p>
      <div className="mt-auto pt-4 border-t border-gray-700 space-y-2">
        <div className="flex items-center gap-2">
          <Calendar size={18} className="text-secondary" />
          <span className="text-sm">
            {formatDate(event.startDate)}
          </span>
        </div>
        {event.location && (
          <div className="flex items-center gap-2">
            <MapPin size={18} className="text-secondary" />
            <span className="text-sm">{event.location}</span>
          </div>
        )}
      </div>
    </div>
  );
}
