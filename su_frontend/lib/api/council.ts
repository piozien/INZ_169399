import { CouncilResponseDto, CouncilRequestDto } from '@/types/council.types';

const API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080';


export async function fetchUserCouncils(): Promise<CouncilResponseDto[]> {
  const response = await fetch(`${API_URL}/api/council`, {
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    throw new Error('Failed to fetch councils');
  }

  return response.json();
}

export async function fetchCouncilById(id: string): Promise<CouncilResponseDto> {
  const response = await fetch(`${API_URL}/api/council/${id}`, {
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    throw new Error('Failed to fetch council');
  }

  return response.json();
}

export async function joinCouncilByCode(joinCode: string): Promise<CouncilResponseDto> {
  const response = await fetch(`${API_URL}/api/council/join/${joinCode}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Failed to join council' }));
    throw new Error(error.message || 'Failed to join council');
  }

  return response.json();
}


export async function createCouncil(data: CouncilRequestDto): Promise<CouncilResponseDto> {
  const response = await fetch(`${API_URL}/api/council`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    credentials: 'include',
    body: JSON.stringify(data),
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: 'Failed to create council' }));
    throw new Error(error.message || 'Failed to create council');
  }

  return response.json();
}

