import { useState, useEffect, useRef, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchAllUsers } from '@/lib/api/user';
import { UserDto } from '@/types/user.types';

export const useUserSearch = (onSelect: (userId: string) => void) => {
    const [query, setQuery] = useState('');
    const [isOpen, setIsOpen] = useState(false);
    const [selectedUser, setSelectedUser] = useState<UserDto | null>(null);
    const wrapperRef = useRef<HTMLDivElement>(null);

    const { data: users, isLoading } = useQuery({
        queryKey: ['users_search'],
        queryFn: fetchAllUsers,
        staleTime: 1000 * 60 * 5,
    });

    const filteredUsers = useMemo(() => {
        if (!query || !users) return [];
        const search = query.toLowerCase();
        return users.filter(
            (user) =>
                user.fullName?.toLowerCase().includes(search) ||
                user.email?.toLowerCase().includes(search)
        );
    }, [users, query]);

    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (wrapperRef.current && !wrapperRef.current.contains(event.target as Node)) {
                setIsOpen(false);
            }
        }

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const handleSelect = (user: UserDto) => {
        setSelectedUser(user);
        onSelect(user.id);
        setIsOpen(false);
        setQuery('');
    };

    const clearSelection = () => {
        setSelectedUser(null);
        onSelect('');
    };

    return {
        query,
        setQuery,
        isOpen,
        setIsOpen,
        selectedUser,
        filteredUsers,
        isLoading,
        wrapperRef,
        handleSelect,
        clearSelection,
    };
};
