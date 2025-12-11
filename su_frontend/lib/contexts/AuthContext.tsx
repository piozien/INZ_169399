'use client';

import { createContext, useContext, ReactNode } from 'react';
import { UserDto } from '@/types/user.types';
import { LoginRequestDto, MicrosoftLoginRequest } from '@/types/auth.types';
import { useAuthLogic } from '@/hooks/auth/useAuthLogic';

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
    const auth = useAuthLogic();

    return <AuthContext.Provider value={auth}>{!auth.isLoading && children}</AuthContext.Provider>;
}

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within AuthProvider');
    return context;
};
