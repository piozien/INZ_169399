import { apiFetch } from './httpClient';
import { DashboardSummaryDto, UserProfileDataDto } from '@/types/dashboard.types';

export const fetchDashboardSummary = async (): Promise<DashboardSummaryDto> => {
    return apiFetch<DashboardSummaryDto>('/dashboard/summary');
};
export const fetchUserProfileData = async (userId: string) => {
    return apiFetch<UserProfileDataDto>(`/dashboard/profile/${userId}`);
};
