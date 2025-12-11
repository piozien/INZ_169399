import { useState, useEffect, useCallback } from 'react';

export type Theme = 'light' | 'dark';

export const useThemeLogic = () => {
    const [theme, setTheme] = useState<Theme>('light');
    const [mounted, setMounted] = useState(false);

    const updateDOM = useCallback((newTheme: Theme) => {
        if (typeof window === 'undefined') return;

        const root = document.documentElement;
        if (newTheme === 'dark') {
            root.classList.add('dark');
            root.classList.remove('light');
        } else {
            root.classList.add('light');
            root.classList.remove('dark');
        }
        localStorage.setItem('theme', newTheme);
    }, []);

    useEffect(() => {
        setMounted(true);
        const savedTheme = localStorage.getItem('theme') as Theme | null;
        const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

        const initialTheme = savedTheme || (prefersDark ? 'dark' : 'light');

        setTheme(initialTheme);
        updateDOM(initialTheme);
    }, [updateDOM]);

    const toggleTheme = () => {
        const newTheme = theme === 'dark' ? 'light' : 'dark';
        setTheme(newTheme);
        updateDOM(newTheme);
    };

    return {
        theme,
        toggleTheme,
        mounted,
    };
};
