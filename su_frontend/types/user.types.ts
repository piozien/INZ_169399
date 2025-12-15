export enum StatusEnum {
    PENDING = 'PENDING',
    CONFIRMED = 'CONFIRMED',
    BLOCKED = 'BLOCKED',
}

export interface UserDto {
    id: string;
    email: string;
    fullName: string;
    status: StatusEnum;
    roles: string[];
    permissions?: string[];
}
export interface ChangePasswordRequestDto {
    oldPassword: string;
    newPassword: string;
}
