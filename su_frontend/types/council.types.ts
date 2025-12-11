export interface RoleOptionDto {
    code: string;
    label: string;
}

export interface CouncilMemberDto {
    councilId: string;
    userId: string;
    userFullName: string;
    userEmail: string;
    role: string;
    roleName: string;
}

export interface CouncilContextDto {
    isMember: boolean;
    role: string | null;
    permissions: string[];
}

export interface CouncilResponseDto {
    id: string;
    name: string;
    academicYear: string;
    startDate: string; // ISO String
    endDate: string; // ISO String
    active: boolean;
    defaultCouncil: boolean;
    joinCode: string;
    createdAt: string;
    members?: CouncilMemberDto[];
    myPermissions?: string[];
}

export interface CouncilRequestDto {
    name: string;
    academicYear: string;
    startDate: string;
    endDate: string;
    active?: boolean;
    defaultCouncil?: boolean;
}
