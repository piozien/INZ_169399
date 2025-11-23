import { RoleCode } from './enums.types';

export interface CouncilMemberDto {
  councilId: string;
  userId: string;
  userFullName: string;
  userEmail: string;
  role: RoleCode;
  roleName: string;
}

export interface CouncilDto {
  id: string;
  name: string;
  invitationCode: string;
  createdAt: string;
  members: CouncilMemberDto[];
}

export interface CouncilResponseDto {
  id: string;
  name: string;
  academicYear: string;
  startDate: string;
  endDate: string;
  isActive: boolean;
  joinCode: string;
  createdAt: string;
  members: CouncilMemberDto[];
}

export interface CouncilRequestDto {
  name: string;
  academicYear: string;
  startDate: string;
  endDate: string;
}

