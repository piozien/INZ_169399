import { useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useQueryClient } from '@tanstack/react-query';
import { apiFetch } from '@/lib/api/httpClient';
import { fetchMyPermissions } from '@/lib/api/permissions';
import { UserDto } from '@/types/user.types';
import { LoginRequestDto, MicrosoftLoginRequest } from '@/types/auth.types';

export const useAuthLogic = () => {
    const [user, setUser] = useState<UserDto | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const router = useRouter();
    const queryClient = useQueryClient();

    const loadPermissionsBackground = useCallback(async () => {
        try {
            const permsData = await fetchMyPermissions();
            setUser((prevUser) => {
                if (!prevUser) return null;
                return {
                    ...prevUser,
                    permissions: permsData.permissions,
                    roles: permsData.roles,
                };
            });
        } catch (error) {
            console.error('Nie udało się pobrać uprawnień w tle', error);
        }
    }, []);

    const checkAuth = useCallback(async () => {
        try {
            const userData = await apiFetch<UserDto>('/users/me');
            setUser(userData);
            setIsLoading(false);

            await loadPermissionsBackground();
        } catch (error) {
            setUser(null);
            setIsLoading(false);
            queryClient.clear();
        }
    }, [loadPermissionsBackground, queryClient]);

    useEffect(() => {
        checkAuth();
    }, [checkAuth]);

    const login = async (data: LoginRequestDto) => {
        queryClient.clear();
        const userData = await apiFetch<UserDto>('/auth/login', {
            method: 'POST',
            body: JSON.stringify(data),
        });
        setUser(userData);
        await loadPermissionsBackground();
    };

    const loginWithMicrosoft = async (data: MicrosoftLoginRequest) => {
        queryClient.clear();
        const userData = await apiFetch<UserDto>('/auth/microsoft', {
            method: 'POST',
            body: JSON.stringify(data),
        });
        setUser(userData);
        await loadPermissionsBackground();
    };

    const logout = async () => {
        try {
            await apiFetch('/auth/logout', { method: 'POST' });
        } catch (e) {
            console.warn('Logout API failed', e);
        } finally {
            setUser(null);
            queryClient.removeQueries();
            queryClient.clear();
            router.push('/login');
        }
    };

    return {
        user,
        isAuthenticated: user !== null,
        isLoading,
        login,
        loginWithMicrosoft,
        logout,
    };
};
