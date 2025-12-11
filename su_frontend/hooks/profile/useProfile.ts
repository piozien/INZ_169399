import { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchUserProfileData } from '@/lib/api/dashboard';
import { useAuth } from '@/lib/contexts/AuthContext';
import { isAfter, isBefore, parseISO } from 'date-fns';

export const useProfile = (profileId: string) => {
    const { user: authUser, logout } = useAuth();

    const isOwnProfile = authUser?.id === profileId;

    const [activeTab, setActiveTab] = useState<'profile' | 'activity' | 'settings'>('profile');
    const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);

    const {
        data: user,
        isLoading,
        error,
    } = useQuery({
        queryKey: ['userProfileData', profileId],
        queryFn: () => fetchUserProfileData(profileId),
        enabled: !!profileId,
    });

    const { activeCouncils, oldCouncils, currentEvents, archivedEvents } = useMemo(() => {
        if (!user)
            return { activeCouncils: [], oldCouncils: [], currentEvents: [], archivedEvents: [] };

        const now = new Date();

        const activeCouncils = user.memberships.filter((m: any) => m.active);
        const oldCouncils = user.memberships.filter((m: any) => !m.active);

        const currentEvents = user.userEvents
            .filter((e: any) => isAfter(parseISO(e.endDate), now))
            .sort(
                (a: any, b: any) =>
                    new Date(a.startDate).getTime() - new Date(b.startDate).getTime()
            );

        const archivedEvents = user.userEvents
            .filter((e: any) => isBefore(parseISO(e.endDate), now))
            .sort(
                (a: any, b: any) => new Date(b.endDate).getTime() - new Date(a.endDate).getTime()
            );

        return { activeCouncils, oldCouncils, currentEvents, archivedEvents };
    }, [user]);

    return {
        user,
        activeCouncils,
        oldCouncils,
        currentEvents,
        archivedEvents,
        isLoading,
        error,

        isOwnProfile,
        logout,

        activeTab,
        setActiveTab,
        isPasswordModalOpen,
        openPasswordModal: () => setIsPasswordModalOpen(true),
        closePasswordModal: () => setIsPasswordModalOpen(false),
    };
};
