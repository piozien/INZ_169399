'use client';

import {
  createContext,
  useContext,
  useEffect,
  useState,
  ReactNode,
} from 'react';
import { UserDto } from '@/types/user.types';
import { apiFetch } from '@/lib/api/httpClient';
import { useRouter } from 'next/navigation';
import { ApiError } from '@/types/error.types';
import { LoginRequestDto, MicrosoftLoginRequest } from '@/types/auth.types';

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

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    try {
      const userData = await apiFetch<UserDto>('/users/me');
      setUser(userData);
    } catch (error) {
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (data: LoginRequestDto) => {
    const user = await apiFetch<UserDto>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    setUser(user);
  };

  const loginWithMicrosoft = async (data: MicrosoftLoginRequest) => {
    const user = await apiFetch<UserDto>('/auth/microsoft', {
      method: 'POST',
      body: JSON.stringify(data),
    });
    setUser(user);
  };

  const logout = async () => {
    try {
      await apiFetch('/auth/logout', { method: 'POST' });
    } catch (e) {
      console.warn('Logout API call failed', e);
    } finally {
      setUser(null);
      router.push('/login');
    }
  };

  const isAuthenticated = user !== null;

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated,
        isLoading,
        login,
        logout,
        loginWithMicrosoft,
      }}
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
