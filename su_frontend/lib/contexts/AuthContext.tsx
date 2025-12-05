'use client';

import { createContext, useContext, useEffect, useState, ReactNode } from 'react';
import { UserDto } from '@/types/user.types';
import { apiFetch } from '@/lib/api/httpClient';
import { fetchMyPermissions } from '@/lib/api/permissions';
import { useRouter } from 'next/navigation';
import { LoginRequestDto, MicrosoftLoginRequest } from '@/types/auth.types';
import { useQueryClient } from '@tanstack/react-query';

interface AuthContextType {
    user: UserDto | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (data: LoginRequestDto) => Promise<void>;
    logout: () => Promise<void>;
    loginWithMicrosoft: (data: MicrosoftLoginRequest) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<UserDto | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const router = useRouter();
    const queryClient = useQueryClient();

    useEffect(() => {
        checkAuth();
    }, []);

    const loadPermissionsBackground = async () => {
        try {
            const permsData = await fetchMyPermissions();
            setUser((prevUser) => {
                if (!prevUser) return null;
                return {
                    ...prevUser,
                    permissions: permsData.permissions,
                    roles: permsData.roles
                };
            });
        } catch (error) {
            console.error("Nie udało się pobrać uprawnień w tle", error);
        }
    };

    const checkAuth = async () => {
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
    };

    const login = async (data: LoginRequestDto) => {
        queryClient.clear();

        const userData = await apiFetch<UserDto>('/auth/login', {
            method: 'POST',
            body: JSON.stringify(data),
        });

        setUser(userData);
        loadPermissionsBackground();
    };

    const loginWithMicrosoft = async (data: MicrosoftLoginRequest) => {
        queryClient.clear();

        const userData = await apiFetch<UserDto>('/auth/microsoft', {
            method: 'POST',
            body: JSON.stringify(data),
        });

        setUser(userData);
        loadPermissionsBackground();
    };

    const logout = async () => {
        try {
            await apiFetch('/auth/logout', { method: 'POST' });
        } catch (e) {
            console.warn("Logout API failed", e);
        } finally {
            setUser(null);
            queryClient.removeQueries();
            queryClient.clear();

            router.push('/login');
        }
    };

    const isAuthenticated = user !== null;

    return (
        <AuthContext.Provider
            value={{ user, isAuthenticated, isLoading, login, logout, loginWithMicrosoft }}
        >
            {!isLoading && children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within AuthProvider');
    return context;
};