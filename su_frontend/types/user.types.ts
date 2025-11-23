import { StatusEnum, AuthProvider } from './enums.types';
import { ClassDto } from './class.types';
import { CouncilDto } from './council.types';

// UserResponseDto
export interface User {
  id: string;
  fullName: string;
  email: string;
  status: StatusEnum;
  createdAt: string; // ISO datetime string
  authProvider: AuthProvider;
  externalId?: string;

  roles: string[];
  permissions: string[];
}

export type StudentClass = ClassDto;
