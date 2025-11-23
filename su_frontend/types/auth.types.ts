import { User } from './user.types';

export interface LoginRequestDTO {
  email: string;
  password: string;
}

export interface UserRequestDTO {
  fullName: string;
  email: string;
  password: string;
}
