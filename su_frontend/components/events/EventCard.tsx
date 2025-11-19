import { EventResponseDto } from '@/types/event.types';
import CalendarDaysIcon from '../icons/CalendarDaysIcon';

export default function EventCard({ event }: { event: EventResponseDto }) {
  return (
    <div className="rounded-lg border bg-secondarybg p-6 transition-transform hover:scale-[1.02]">
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold">{event.title}</h2>
        <span
          className="text-sm text-txtcolor-300"
        >
          {new Date(event.startDate).toLocaleString('pl-PL', {
            dateStyle: 'short',
            timeStyle: 'short',
          })}{' '}
          -{' '}
          {new Date(event.endDate).toLocaleString('pl-PL', {
            dateStyle: 'short',
            timeStyle: 'short',
          })}
        </span>
      </div>
      <p className="mt-4 text-foreground">{event.description}</p>
      <div className="mt-4 text-xs text-txtcolor-300">
        <span>
          Utworzone przez: {event.createdBy?.fullName || 'N/A'}
        </span>
      </div>
    </div>
  );
};
