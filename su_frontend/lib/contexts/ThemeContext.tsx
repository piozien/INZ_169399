'use client';

import { createContext, useContext, ReactNode } from 'react';
import { useThemeLogic, Theme } from '@/hooks/useThemeLogic';

interface ThemeContextType {
    theme: Theme;
    toggleTheme: () => void;
    mounted: boolean;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export function ThemeProvider({ children }: { children: ReactNode }) {
    const themeLogic = useThemeLogic();

    return <ThemeContext.Provider value={themeLogic}>{children}</ThemeContext.Provider>;
}

export function useTheme() {
    const context = useContext(ThemeContext);
    if (context === undefined) {
        throw new Error('useTheme must be used within a ThemeProvider');
    }
    return context;
}
