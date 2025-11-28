import { UserDto } from "./user.types";

export interface LoginRequestDto {
    email: string;
    password?: string;
}

export interface MicrosoftLoginRequest {
    token: string;
}

export type LoginResponse = UserDto;