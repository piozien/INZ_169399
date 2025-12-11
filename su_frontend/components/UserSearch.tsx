'use client';

import { Search, User, Loader2, X } from 'lucide-react';
import { useUserSearch } from '@/hooks/useUserSearch';

interface Props {
    onSelect: (userId: string) => void;
    disabled?: boolean;
}

export default function UserSearch({ onSelect, disabled }: Props) {
    const {
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
    } = useUserSearch(onSelect);

    if (selectedUser) {
        return (
            <div className="bg-inputbg border-secondary/50 animate-in fade-in zoom-in-95 flex items-center justify-between rounded-lg border p-3 duration-200">
                <div className="flex items-center gap-3">
                    <div className="bg-primary/20 text-primary flex h-10 w-10 shrink-0 items-center justify-center rounded-full">
                        <User className="h-5 w-5" />
                    </div>
                    <div className="overflow-hidden">
                        <p className="text-foreground truncate text-sm font-bold">
                            {selectedUser.fullName}
                        </p>
                        <p className="text-txtcolor-300 truncate text-xs">{selectedUser.email}</p>
                    </div>
                </div>
                <button
                    onClick={clearSelection}
                    disabled={disabled}
                    className="text-txtcolor-300 hover:text-error hover:bg-error/10 rounded-full p-2 transition-all"
                    type="button"
                >
                    <X className="h-5 w-5" />
                </button>
            </div>
        );
    }

    return (
        <div ref={wrapperRef} className="relative w-full">
            <div className="group relative">
                <Search className="text-txtcolor-300 group-focus-within:text-primary absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2 transition-colors" />
                <input
                    type="text"
                    className="bg-inputbg text-foreground focus:ring-secondary placeholder-txtcolor-300/50 border-border w-full rounded-lg border py-3 pr-4 pl-10 transition-all focus:ring-2 focus:outline-none"
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
                    <div className="absolute top-1/2 right-3 -translate-y-1/2">
                        <Loader2 className="text-primary h-4 w-4 animate-spin" />
                    </div>
                )}
            </div>

            {isOpen && query && (
                <div className="bg-secondarybg border-border animate-in slide-in-from-top-2 absolute z-[100] mt-2 w-full overflow-hidden rounded-lg border shadow-2xl backdrop-blur-sm">
                    {filteredUsers.length > 0 ? (
                        <div className="custom-scrollbar max-h-60 overflow-y-auto">
                            {filteredUsers.map((user) => (
                                <button
                                    key={user.id}
                                    onClick={() => handleSelect(user)}
                                    className="hover:bg-primary/10 border-border/50 group flex w-full items-center gap-3 border-b p-3 text-left transition-colors last:border-0"
                                    type="button"
                                >
                                    <div className="bg-inputbg text-txtcolor-300 group-hover:bg-primary/20 group-hover:text-primary flex h-8 w-8 shrink-0 items-center justify-center rounded-full transition-colors">
                                        <User className="h-4 w-4" />
                                    </div>
                                    <div className="overflow-hidden">
                                        <p className="text-foreground truncate text-sm font-medium">
                                            {user.fullName}
                                        </p>
                                        <p className="text-txtcolor-300 truncate text-xs">
                                            {user.email}
                                        </p>
                                    </div>
                                </button>
                            ))}
                        </div>
                    ) : (
                        <div className="text-txtcolor-300 p-4 text-center text-sm">
                            Nie znaleziono użytkownika "{query}"
                        </div>
                    )}
                </div>
            )}
        </div>
    );
}
