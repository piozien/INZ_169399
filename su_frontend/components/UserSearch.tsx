'use client';

import { useState, useEffect, useRef } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Search, User, Loader2, X } from 'lucide-react';
import { fetchAllUsers } from '@/lib/api/user';
import { UserDto } from '@/types/user.types';

interface Props {
    onSelect: (userId: string) => void;
    disabled?: boolean;
}

export default function UserSearch({ onSelect, disabled }: Props) {
    const [query, setQuery] = useState('');
    const [isOpen, setIsOpen] = useState(false);
    const [selectedUser, setSelectedUser] = useState<UserDto | null>(null);
    const wrapperRef = useRef<HTMLDivElement>(null);

    const { data: users, isLoading } = useQuery({
        queryKey: ['users_search'],
        queryFn: fetchAllUsers,
        staleTime: 1000 * 60 * 5, //5 min
    });

    const filteredUsers = users?.filter((user) => {
        if (!query) return false;
        const search = query.toLowerCase();
        return (
            user.fullName?.toLowerCase().includes(search) ||
            user.email?.toLowerCase().includes(search)
        );
    });

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

    if (selectedUser) {
        return (
            <div className="flex items-center justify-between p-3 bg-inputbg border border-secondary/50 rounded-lg animate-in fade-in zoom-in-95 duration-200">
                <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center text-primary shrink-0">
                        <User className="h-5 w-5" />
                    </div>
                    <div className="overflow-hidden">
                        <p className="text-sm font-bold text-foreground truncate">{selectedUser.fullName}</p>
                        <p className="text-xs text-txtcolor-300 truncate">{selectedUser.email}</p>
                    </div>
                </div>
                <button
                    onClick={clearSelection}
                    disabled={disabled}
                    className="p-2 text-txtcolor-300 hover:text-error hover:bg-error/10 rounded-full transition-all"
                >
                    <X className="h-5 w-5" />
                </button>
            </div>
        );
    }

    return (
        <div ref={wrapperRef} className="relative w-full">
            <div className="relative group">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-txtcolor-300 group-focus-within:text-primary transition-colors" />
                <input
                    type="text"
                    className="w-full bg-inputbg text-foreground rounded-lg pl-10 pr-4 py-3 focus:outline-none focus:ring-2 focus:ring-secondary placeholder-txtcolor-300/50 border border-border transition-all"
                    placeholder="Wpisz imię, nazwisko lub email..."
                    value={query}
                    onChange={(e) => {
                        setQuery(e.target.value);
                        setIsOpen(true);
                    }}
                    onFocus={() => setIsOpen(true)}
                    disabled={disabled}
                />
                {isLoading && (
                    <div className="absolute right-3 top-1/2 -translate-y-1/2">
                        <Loader2 className="h-4 w-4 animate-spin text-primary" />
                    </div>
                )}
            </div>

            {isOpen && query && (
                <div className="absolute z-[100] w-full mt-2 bg-secondarybg border border-border rounded-lg shadow-2xl overflow-hidden backdrop-blur-sm">
                    {filteredUsers && filteredUsers.length > 0 ? (
                        <div className="max-h-60 overflow-y-auto">
                            {filteredUsers.map((user) => (
                                <button
                                    key={user.id}
                                    onClick={() => handleSelect(user)}
                                    className="w-full flex items-center gap-3 p-3 hover:bg-primary/10 text-left transition-colors border-b border-border/50 last:border-0"
                                >
                                    <div className="w-8 h-8 rounded-full bg-inputbg flex items-center justify-center text-txtcolor-300 shrink-0">
                                        <User className="h-4 w-4" />
                                    </div>
                                    <div className="overflow-hidden">
                                        <p className="text-sm font-medium text-foreground truncate">{user.fullName}</p>
                                        <p className="text-xs text-txtcolor-300 truncate">{user.email}</p>
                                    </div>
                                </button>
                            ))}
                        </div>
                    ) : (
                        <div className="p-4 text-center text-sm text-txtcolor-300">
                            Nie znaleziono użytkownika "{query}"
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}