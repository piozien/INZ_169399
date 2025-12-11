'use client';

import { X, UserPlus, Loader2 } from 'lucide-react';
import UserSearch from '@/components/UserSearch';
import { useAddMemberModal } from '@/hooks/council/members/useAddMemberModal';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    onAdd: (userId: string, roleCode: string) => void;
    isAdding: boolean;
}

export default function AddMemberModal({ isOpen, onClose, onAdd, isAdding }: Props) {
    const {
        userId,
        setUserId,
        selectedRole,
        setSelectedRole,
        roles,
        isLoadingRoles,
        handleSubmit,
    } = useAddMemberModal(isOpen, onAdd);

    if (!isOpen) return null;

    return (
        <div className="bg-background animate-in fade-in fixed inset-0 z-50 flex items-center justify-center p-4 backdrop-blur-sm duration-200">
            <div className="bg-background border-border flex max-h-[90vh] w-full max-w-md flex-col rounded-xl border shadow-2xl">
                <div className="border-border bg-secondarybg flex items-center justify-between rounded-t-xl border-b p-4">
                    <h3 className="text-foreground flex items-center gap-2 text-lg font-bold">
                        <UserPlus className="text-primary h-5 w-5" /> Dodaj członka
                    </h3>
                    <button
                        onClick={onClose}
                        className="text-txtcolor-300 hover:text-foreground transition-colors"
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <div className="space-y-6 overflow-visible p-6">
                    <div className="relative z-50">
                        <label className="text-txtcolor-300 mb-2 block text-xs font-bold tracking-wider uppercase">
                            Wyszukaj użytkownika
                        </label>
                        <UserSearch onSelect={(id) => setUserId(id)} disabled={isAdding} />
                    </div>

                    {userId && (
                        <div className="animate-in slide-in-from-top-2 fade-in border-border/50 relative z-0 space-y-3 border-t pt-2 duration-300">
                            <label className="text-txtcolor-300 mb-2 block text-xs font-bold tracking-wider uppercase">
                                Przypisz rolę
                            </label>

                            {isLoadingRoles ? (
                                <div className="flex justify-center py-4">
                                    <Loader2 className="text-secondary h-5 w-5 animate-spin" />
                                </div>
                            ) : (
                                <div className="max-h-[200px] space-y-2 overflow-y-auto pr-2">
                                    {roles?.map((role) => (
                                        <label
                                            key={role.code}
                                            className={`flex cursor-pointer items-center justify-between rounded-lg border p-3 transition-all ${
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
                                                    onChange={(e) =>
                                                        setSelectedRole(e.target.value)
                                                    }
                                                    className="accent-secondary h-4 w-4 cursor-pointer"
                                                    disabled={isAdding}
                                                />
                                                <span
                                                    className={
                                                        selectedRole === role.code
                                                            ? 'text-foreground font-medium'
                                                            : 'text-txtcolor-300'
                                                    }
                                                >
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

                <div className="border-border bg-secondarybg flex justify-end gap-3 rounded-b-xl border-t p-4">
                    <button
                        onClick={onClose}
                        disabled={isAdding}
                        className="text-txtcolor-300 hover:bg-inputbg rounded-lg px-4 py-2 text-sm font-medium transition-colors"
                    >
                        Anuluj
                    </button>
                    <button
                        onClick={handleSubmit}
                        disabled={isAdding || !userId || !selectedRole}
                        className="bg-primary text-darkgray flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-bold transition-all hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-50"
                    >
                        {isAdding ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                            <UserPlus className="h-4 w-4" />
                        )}
                        Dodaj
                    </button>
                </div>
            </div>
        </div>
    );
}
