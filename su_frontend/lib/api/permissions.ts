import { apiFetch } from "./httpClient";

export interface PermissionsResponseDto {
    roles: string[];
    permissions: string[];
}

export const fetchMyPermissions = async (): Promise<PermissionsResponseDto> => {
    return apiFetch<PermissionsResponseDto>('/permissions'); 
};