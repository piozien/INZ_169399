export interface DashboardSummaryDto {
    councilMember: boolean;
    activeCouncilId?: string;
    activeCouncilName?: string;

    budgetBalance?: number;
    pendingSuggestionsCount?: number;
    upcomingEventsCount?: number;

    myTotalSuggestionsCount: number;
    myPendingSuggestionsCount: number;
}
export interface MembershipDto {
    councilId: string;
    councilName: string;
    userRole: string;
    active: boolean;
    startDate: string;
    endDate: string;
}

export interface UserEventDto {
    eventId: string;
    title: string;
    startDate: string;
    endDate: string;
}

export interface UserProfileDataDto {
    id: string;
    email: string;
    fullName: string;
    status: string;
    globalRoles: string[];

    totalSuggestionsCount: number;
    pendingSuggestionsCount: number;
    approvedSuggestionsCount: number;

    memberships: MembershipDto[];
    userEvents: UserEventDto[];
}
