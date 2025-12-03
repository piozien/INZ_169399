'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { X, UserPlus, Loader2 } from 'lucide-react';
import { fetchAvailableCouncilRoles } from '@/lib/api/council';
import { RoleOptionDto } from '@/types/council.types';
import UserSearch from '@/components/UserSearch';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    onAdd: (userId: string, roleCode: string) => void;
    isAdding: boolean;
}

export default function AddMemberModal({ isOpen, onClose, onAdd, isAdding }: Props) {
    const [userId, setUserId] = useState('');
    const [selectedRole, setSelectedRole] = useState('CZLONEK_SU');

    const { data: roles, isLoading } = useQuery<RoleOptionDto[]>({
        queryKey: ['councilRoles'],
        queryFn: fetchAvailableCouncilRoles,
        enabled: isOpen,
        staleTime: 1000 * 60 * 60 * 24,
    });

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-background backdrop-blur-sm p-4 animate-in fade-in duration-200">
            <div className="w-full max-w-md bg-background border border-border rounded-xl shadow-2xl flex flex-col max-h-[90vh]">

                <div className="flex justify-between items-center p-4 border-b border-border bg-secondarybg rounded-t-xl">
                    <h3 className="font-bold text-lg text-foreground flex items-center gap-2">
                        <UserPlus className="h-5 w-5 text-primary" /> Dodaj członka
                    </h3>
                    <button onClick={onClose} className="text-txtcolor-300 hover:text-foreground transition-colors">
                        <X className="h-5 w-5" />
                    </button>
                </div>
                <div className="p-6 space-y-6 overflow-visible">

                    <div className="relative z-50">
                        <label className="block text-xs font-bold text-txtcolor-300 mb-2 uppercase tracking-wider">
                            Wyszukaj użytkownika
                        </label>
                        <UserSearch
                            onSelect={(id) => setUserId(id)}
                            disabled={isAdding}
                        />
                    </div>

                    {userId && (
                        <div className="space-y-3 animate-in slide-in-from-top-2 fade-in duration-300 relative z-0 pt-2 border-t border-border/50">
                            <label className="block text-xs font-bold text-txtcolor-300 uppercase tracking-wider mb-2">
                                Przypisz rolę
                            </label>

                            {isLoading ? (
                                <div className="flex justify-center py-4">
                                    <Loader2 className="h-5 w-5 animate-spin text-secondary" />
                                </div>
                            ) : (
                                <div className="space-y-2 max-h-[200px] overflow-y-auto pr-2">
                                    {roles?.map((role) => (
                                        <label
                                            key={role.code}
                                            className={`flex items-center justify-between p-3 rounded-lg border cursor-pointer transition-all ${
                                                selectedRole === role.code
                                                    ? 'border-secondary bg-secondary/10'
                                                    : 'border-border hover:border-secondary/50 bg-inputbg'
                                            }`}
                                        >
                                            <div className="flex items-center gap-3">
                                                <input
                                                    type="radio"
                                                    name="newMemberRole"
                                                    value={role.code}
                                                    checked={selectedRole === role.code}
                                                    onChange={(e) => setSelectedRole(e.target.value)}
                                                    className="accent-secondary h-4 w-4 cursor-pointer"
                                                    disabled={isAdding}
                                                />
                                                <span className={selectedRole === role.code ? 'text-foreground font-medium' : 'text-txtcolor-300'}>
                                {role.label}
                            </span>
                                            </div>
                                        </label>
                                    ))}
                                </div>
                            )}
                        </div>
                    )}
                </div>

                <div className="p-4 border-t border-border bg-secondarybg flex justify-end gap-3 rounded-b-xl">
                    <button
                        onClick={onClose}
                        disabled={isAdding}
                        className="px-4 py-2 rounded-lg text-sm font-medium text-txtcolor-300 hover:bg-inputbg transition-colors"
                    >
                        Anuluj
                    </button>
                    <button
                        onClick={() => onAdd(userId, selectedRole)}
                        disabled={isAdding || !userId || !selectedRole}
                        className="px-4 py-2 bg-primary text-darkgray rounded-lg text-sm font-bold flex items-center gap-2 hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                    >
                        {isAdding ? <Loader2 className="h-4 w-4 animate-spin" /> : <UserPlus className="h-4 w-4" />}
                        Dodaj
                    </button>
                </div>
            </div>
        </div>
    );
}