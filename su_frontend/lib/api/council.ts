import { apiFetch } from "./httpClient";
import {
    CouncilRequestDto,
    CouncilResponseDto,
    CouncilMemberDto,
    CouncilContextDto,
    RoleOptionDto
} from "@/types/council.types";

export const fetchUserCouncils = async (): Promise<CouncilResponseDto[]> => {
    return apiFetch<CouncilResponseDto[]>("/councils");
};

export const joinCouncilByCode = async (joinCode: string): Promise<CouncilResponseDto> => {
    return apiFetch<CouncilResponseDto>(`/councils/join/${joinCode}`, {
        method: "POST",
    });
};

export const createCouncil = async (data: CouncilRequestDto): Promise<CouncilResponseDto> => {
    return apiFetch<CouncilResponseDto>("/councils", {
        method: "POST",
        body: JSON.stringify(data),
    });
};

export const updateCouncil = async (id: string, data: CouncilRequestDto): Promise<CouncilResponseDto> => {
    return apiFetch<CouncilResponseDto>(`/councils/${id}`, {
        method: 'PUT',
        body: JSON.stringify(data),
    });
};

export const addMemberToCouncil = async (councilId: string, userId: string, roleCode: string): Promise<CouncilMemberDto> => {
    const params = new URLSearchParams({ userId, roleCode });
    return apiFetch<CouncilMemberDto>(`/councils/${councilId}/members?${params.toString()}`, {
        method: 'POST',
    });
};

export const fetchAvailableCouncilRoles = async (): Promise<RoleOptionDto[]> => {
    return apiFetch<RoleOptionDto[]>("/councils/roles");
};

export const fetchCouncilById = async (id: string): Promise<CouncilResponseDto> => {
    return apiFetch<CouncilResponseDto>(`/councils/${id}`);
};

export const fetchCouncilMembers = async (councilId: string): Promise<CouncilMemberDto[]> => {
    return apiFetch<CouncilMemberDto[]>(`/councils/${councilId}/members`);
};

export const updateMemberRole = async (councilId: string, userId: string, roleCode: string): Promise<void> => {
    const params = new URLSearchParams({ roleCode });
    return apiFetch<void>(`/councils/${councilId}/members/${userId}/role?${params.toString()}`, {
        method: 'PUT',
    });
};
export const leaveCouncil = async (councilId: string, userId: string): Promise<void> => {
    return apiFetch<void>(`/councils/${councilId}/members/${userId}`, {
        method: 'DELETE',
    });
};


export const removeMemberFromCouncil = async (councilId: string, userId: string): Promise<void> => {
    return apiFetch<void>(`/councils/${councilId}/members/${userId}`, {
        method: 'DELETE',
    });
};

export const fetchCouncilContext = async (councilId: string): Promise<CouncilContextDto> => {
    return apiFetch<CouncilContextDto>(`/councils/${councilId}/my-context`);
};