'use client';

import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { X, Save, Loader2, AlertCircle } from 'lucide-react';
import { fetchAvailableCouncilRoles } from '@/lib/api/council';
import { RoleOptionDto } from '@/types/council.types';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    onSave: (roleCode: string) => void;
    isSaving: boolean;
    currentRole: string;
    memberName: string;
}

export default function EditRoleModal({ isOpen, onClose, onSave, isSaving, currentRole, memberName }: Props) {
    const [selectedRole, setSelectedRole] = useState(currentRole);

    const { data: roles, isLoading, error } = useQuery<RoleOptionDto[]>({
        queryKey: ['councilRoles'],
        queryFn: fetchAvailableCouncilRoles,
        enabled: isOpen,
        staleTime: 1000 * 60 * 60 * 24, //24h
    });

    useEffect(() => {
        if (isOpen) {
            setSelectedRole(currentRole);
        }
    }, [isOpen, currentRole]);

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-bacground/80 backdrop-blur-sm p-4 animate-in fade-in duration-200">
            <div className="w-full max-w-md bg-background border border-border rounded-xl shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">

                <div className="flex justify-between items-center p-4 border-b border-border bg-secondarybg">
                    <h3 className="font-bold text-lg text-foreground">Zmień rolę</h3>
                    <button onClick={onClose} className="text-txtcolor-300 hover:text-foreground transition-colors">
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <div className="p-6">
                    <p className="text-sm text-txtcolor-300 mb-4">
                        Wybierz nową funkcję dla użytkownika <span className="text-primary font-semibold">{memberName}</span>.
                    </p>

                    {isLoading && (
                        <div className="flex justify-center py-8 text-secondary">
                            <Loader2 className="h-8 w-8 animate-spin" />
                        </div>
                    )}

                    {error && (
                        <div className="flex items-center gap-2 text-error bg-error/10 p-3 rounded-lg border border-error/20">
                            <AlertCircle className="h-5 w-5" />
                            <span className="text-sm">Nie udało się pobrać listy ról.</span>
                        </div>
                    )}

                    {roles && (
                        <div className="space-y-2 max-h-[300px] overflow-y-auto pr-2 scrollbar-thin">
                            {roles.map((role) => (
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
                                            name="role"
                                            value={role.code}
                                            checked={selectedRole === role.code}
                                            onChange={(e) => setSelectedRole(e.target.value)}
                                            className="accent-secondary h-4 w-4 cursor-pointer"
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

                <div className="p-4 border-t border-border bg-secondarybg flex justify-end gap-3">
                    <button
                        onClick={onClose}
                        className="px-4 py-2 rounded-lg text-sm font-medium text-txtcolor-300 hover:bg-inputbg transition-colors"
                    >
                        Anuluj
                    </button>
                    <button
                        onClick={() => onSave(selectedRole)}
                        disabled={isSaving || selectedRole === currentRole || isLoading || !!error}
                        className="px-4 py-2 bg-primary text-darkgray rounded-lg text-sm font-bold flex items-center gap-2 hover:opacity-90 disabled:opacity-50 transition-all"
                    >
                        {isSaving ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
                        Zapisz
                    </button>
                </div>
            </div>
        </div>
    );
}