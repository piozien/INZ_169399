import {apiFetch} from './httpClient';
import {UserDto, UserUpdateRequestDto} from '@/types/user.types';
import {SuggestionDto} from '@/types/suggestions.types';
import {ActivityLogResponseDto} from '@/types/log.types';
import {CouncilResponseDto} from '@/types/council.types';

export const fetchAllUsersAdmin = async (): Promise<UserDto[]> => {
    return apiFetch<UserDto[]>('/users');
};

export const unblockUser = async (userId: string): Promise<UserDto> => {
    return apiFetch<UserDto>(`/users/${userId}/unblock`, {method: 'POST'});
};

export const deleteUser = async (userId: string): Promise<void> => {
    return apiFetch(`/users/${userId}`, {method: 'DELETE'});
};

export const updateUserAdmin = async (userId: string, data: UserUpdateRequestDto): Promise<UserDto> => {
    return apiFetch<UserDto>(`/users/${userId}`, {
        method: 'PUT',
        body: JSON.stringify(data),
    });
};

export const fetchAllGlobalRoles = async (): Promise<string[]> => {
    return apiFetch<string[]>('/admin/roles');
};

export const assignGlobalRole = async (userId: string, roleCode: string): Promise<UserDto> => {
    return apiFetch<UserDto>(`/users/${userId}/roles/${roleCode}`, {method: 'POST'});
};

export const removeGlobalRole = async (userId: string, roleCode: string): Promise<UserDto> => {
    return apiFetch<UserDto>(`/users/${userId}/roles/${roleCode}`, {method: 'DELETE'});
};

export const fetchUserLogs = async (userId: string): Promise<ActivityLogResponseDto[]> => {
    return apiFetch<ActivityLogResponseDto[]>(`/logs/users/${userId}`);
};

export const fetchPermissionMatrix = async (): Promise<Record<string, string[]>> => {
    return apiFetch<Record<string, string[]>>('/admin/permissions/matrix');
};

export const assignPermission = async (roleCode: string, permissionCode: string): Promise<void> => {
    return apiFetch(`/admin/roles/${roleCode}/permissions/${permissionCode}`, {method: 'POST'});
};

export const revokePermission = async (roleCode: string, permissionCode: string): Promise<void> => {
    return apiFetch(`/admin/roles/${roleCode}/permissions/${permissionCode}`, {method: 'DELETE'});
};

export const fetchAllCouncilsAdmin = async (): Promise<CouncilResponseDto[]> => {
    return apiFetch<CouncilResponseDto[]>('/councils');
};

export const deleteCouncilAdmin = async (councilId: string): Promise<void> => {
    return apiFetch(`/councils/${councilId}`, {method: 'DELETE'});
};

export const fetchAllSuggestionsAdmin = async (): Promise<SuggestionDto[]> => {
    return apiFetch<SuggestionDto[]>('/suggestions');
};

export const deleteSuggestionAdmin = async (suggestionId: string): Promise<void> => {
    return apiFetch(`/suggestions/${suggestionId}`, {method: 'DELETE'});
};
