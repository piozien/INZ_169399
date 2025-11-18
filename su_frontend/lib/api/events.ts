import { EventResponseDto } from '@/types/event.types';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export const fetchAllEvents = async (): Promise<EventResponseDto[]> => {
  const response = await fetch(`${API_URL}/api/events`, {
    method: 'GET',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    const errorBody = await response.text();
    console.error('Failed to fetch events:', errorBody);
    throw new Error('Failed to fetch events');
  }

  return response.json();
};

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
