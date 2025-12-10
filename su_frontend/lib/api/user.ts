import {ChangePasswordRequestDto, UserDto} from "@/types/user.types";
import { apiFetch } from "./httpClient";

export const fetchAllUsers = async (): Promise<UserDto[]> => {
    return apiFetch<UserDto[]>("/users");
};

export const changePassword = async (data: ChangePasswordRequestDto): Promise<void> => {
    return apiFetch<void>('/users/change-password', {
        method: 'PATCH',
        body: JSON.stringify(data),
    });
};
