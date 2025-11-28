export interface CouncilMemberDto {
  councilId: string;
  userId: string;
  userFullName: string;
  userEmail: string;
  role: string;
  roleName: string;
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
  members?: CouncilMemberDto[];
}

export interface CouncilRequestDto {
  name: string;
  academicYear: string;
  startDate: string; // YYYY-MM-DD
  endDate: string;   // YYYY-MM-DD
}