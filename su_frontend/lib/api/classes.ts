import { ClassDto } from '@/types/class.types';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';

export interface ClassesResponseDto {
  id: string;
  name: string;
  year: string;
}

export async function fetchUserClasses(): Promise<ClassesResponseDto[]> {
  const response = await fetch(`${API_URL}/api/classes`, {
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    throw new Error('Failed to fetch classes');
  }

  return response.json();
}

export async function fetchClassById(id: string): Promise<ClassesResponseDto> {
  const response = await fetch(`${API_URL}/api/classes/${id}`, {
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    throw new Error('Failed to fetch class');
  }

  return response.json();
}

export async function fetchCurrentUserClass(): Promise<ClassDto | null> {
  const classes = await fetchUserClasses();
  if (classes.length === 0) {
    return null;
  }
  return {
    id: classes[0].id,
    name: classes[0].name,
  };
}

