'use client';

import { X, Save, Loader2, AlertCircle } from 'lucide-react';
import { useEditRoleModal } from '@/hooks/council/members/useEditRoleModal';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    onSave: (roleCode: string) => void;
    isSaving: boolean;
    currentRole: string;
    memberName: string;
}

export default function EditRoleModal({
    isOpen,
    onClose,
    onSave,
    isSaving,
    currentRole,
    memberName,
}: Props) {
    const { selectedRole, setSelectedRole, roles, isLoading, error, handleSave } = useEditRoleModal(
        isOpen,
        currentRole,
        onSave
    );

    if (!isOpen) return null;

    return (
        <div className="bg-background/80 animate-in fade-in fixed inset-0 z-50 flex items-center justify-center p-4 backdrop-blur-sm duration-200">
            <div className="bg-background border-border animate-in zoom-in-95 w-full max-w-md overflow-hidden rounded-xl border shadow-2xl duration-200">
                <div className="border-border bg-secondarybg flex items-center justify-between border-b p-4">
                    <h3 className="text-foreground text-lg font-bold">Zmień rolę</h3>
                    <button
                        onClick={onClose}
                        className="text-txtcolor-300 hover:text-foreground transition-colors"
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <div className="p-6">
                    <p className="text-txtcolor-300 mb-4 text-sm">
                        Wybierz nową funkcję dla użytkownika{' '}
                        <span className="text-primary font-semibold">{memberName}</span>.
                    </p>

                    {isLoading && (
                        <div className="text-secondary flex justify-center py-8">
                            <Loader2 className="h-8 w-8 animate-spin" />
                        </div>
                    )}

                    {error && (
                        <div className="text-error bg-error/10 border-error/20 flex items-center gap-2 rounded-lg border p-3">
                            <AlertCircle className="h-5 w-5" />
                            <span className="text-sm">Nie udało się pobrać listy ról.</span>
                        </div>
                    )}

                    {roles && (
                        <div className="scrollbar-thin max-h-[300px] space-y-2 overflow-y-auto pr-2">
                            {roles.map((role) => (
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
                                            name="role"
                                            value={role.code}
                                            checked={selectedRole === role.code}
                                            onChange={(e) => setSelectedRole(e.target.value)}
                                            className="accent-secondary h-4 w-4 cursor-pointer"
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

                <div className="border-border bg-secondarybg flex justify-end gap-3 border-t p-4">
                    <button
                        onClick={onClose}
                        className="text-txtcolor-300 hover:bg-inputbg rounded-lg px-4 py-2 text-sm font-medium transition-colors"
                    >
                        Anuluj
                    </button>
                    <button
                        onClick={handleSave}
                        disabled={isSaving || selectedRole === currentRole || isLoading || !!error}
                        className="bg-primary text-darkgray flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-bold transition-all hover:opacity-90 disabled:opacity-50"
                    >
                        {isSaving ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                            <Save className="h-4 w-4" />
                        )}
                        Zapisz
                    </button>
                </div>
            </div>
        </div>
    );
}
