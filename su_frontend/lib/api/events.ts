import { EventResponseDto } from '@/types/event.types';
import { Event } from '@/types/event.types';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export async function fetchAllEvents(): Promise<Event[]> {
  const response = await fetch(`${API_URL}/api/events`, {
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });
  if (!response.ok) {
    throw new Error('Failed to fetch events');
  }
  return response.json();
}

export async function fetchUpcomingEvents(): Promise<Event[]> {
    const response = await fetch(`${API_URL}/api/events/upcoming`, {
        headers: {
            'Content-Type': 'application/json',
        },
    });
    if (!response.ok) {
        throw new Error('Failed to fetch upcoming events');
    }
    return response.json();
}

export const fetchEventsByDateRange = async (
  startDate: string,
  endDate: string,
): Promise<EventResponseDto[]> => {
  const response = await fetch(
    `${API_URL}/api/events/range?startDate=${startDate}&endDate=${endDate}`,
    {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
    },
  );

  if (!response.ok) {
    const errorBody = await response.text();
    console.error('Failed to fetch events by date range:', errorBody);
    throw new Error('Failed to fetch events by date range');
  }

  return response.json();
};
