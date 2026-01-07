import { apiFetch } from './httpClient';

export interface PermissionsResponseDto {
    roles: string[];
    permissions: string[];
}

export const fetchMyPermissions = async (councilId?: string): Promise<PermissionsResponseDto> => {
    const query = councilId ? `?councilId=${councilId}` : '';
    return apiFetch<PermissionsResponseDto>(`/permissions${query}`);
};
