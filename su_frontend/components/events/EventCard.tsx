import { EventResponseDto } from '@/types/event.types';
import CalendarDaysIcon from '../icons/CalendarDaysIcon';

const EventCard = ({ event }: { event: EventResponseDto }) => {
  return (
    <div className="rounded-lg border border-neutral-700 bg-secondarybg p-6 transition-transform hover:scale-[1.02]">
      <h3 className="text-2xl font-bold text-primary">{event.title}</h3>
      <div className="mt-2 flex items-center gap-2 text-sm text-txtcolor-300">
        <CalendarDaysIcon className="h-4 w-4" />
        <span>
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

export default EventCard;
