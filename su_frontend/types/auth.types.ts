export interface UserRequestDto {
    fullName: string;
    email: string;
    password: string;
}
export interface LoginRequestDto {
    email: string;
    password?: string;
}

export interface MicrosoftLoginRequest {
    token: string;
}
