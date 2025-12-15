'use client';

import { useState } from 'react';
import { Check, X, Loader2, Lock, ShieldAlert, Globe, Landmark } from 'lucide-react';
import { usePermissions } from '@/hooks/admin/usePermissions';

const ROLE_NAMES: Record<string, string> = {
    'ADMINISTRATOR': 'Administrator',
    'DYREKTOR': 'Dyrektor',
    'ZASTEPCA_DYREKTORA': 'Wicedyrektor',
    'NAUCZYCIEL': 'Nauczyciel',
    'UCZEN': 'Uczeń',
    'BYLY_UCZEN': 'Były Uczeń',

    'PRZEWODNICZACY_SU': 'Przew. SU',
    'ZASTEPCA_SU': 'Zastępca SU',
    'CZLONEK_SU': 'Członek SU',
    'OPIEKUN_SU': 'Opiekun SU',
    'SKARBNIK_SU': 'Skarbnik SU',
    'BYLY_CZLONEK_SU': 'Były Czł. SU',
};

const formatRole = (role: string) => {
    if (ROLE_NAMES[role]) return ROLE_NAMES[role];

    return role
        .replace(/_/g, ' ')
        .toLowerCase()
        .replace(/\b\w/g, (l) => l.toUpperCase());
};

export default function PermissionsMatrix() {
    const { matrix, isLoading, togglePermission } = usePermissions();
    const [activeTab, setActiveTab] = useState<'GLOBAL' | 'SU'>('GLOBAL');

    if (isLoading || !matrix)
        return (
            <div className="flex justify-center p-10">
                <Loader2 className="text-primary h-8 w-8 animate-spin" />
            </div>
        );

    const suRoleKeywords = ['_SU'];
    const allRoles = Object.keys(matrix);

    const visibleRoles = allRoles.filter((role) => {
        const isSu = suRoleKeywords.some((keyword) => role.includes(keyword));
        return activeTab === 'SU' ? isSu : !isSu;
    });

    const allPermissions = Array.from(new Set(Object.values(matrix).flat())).sort();

    return (
        <div className="space-y-6">
            <div className="bg-warning/10 border-warning/20 flex items-start gap-3 rounded-xl border p-4">
                <ShieldAlert className="text-warning h-6 w-6 shrink-0" />
                <div>
                    <h3 className="text-warning text-sm font-bold uppercase">
                        Zarządzanie Uprawnieniami
                    </h3>
                    <p className="text-txtcolor-300 text-sm leading-relaxed">
                        Zmiany są aplikowane natychmiastowo. Pamiętaj, że odebranie uprawnień może
                        zablokować dostęp do kluczowych funkcji.
                    </p>
                </div>
            </div>

            <div className="bg-inputbg border-border flex w-full rounded-xl border p-1 sm:w-fit">
                <button
                    onClick={() => setActiveTab('GLOBAL')}
                    className={`flex flex-1 items-center justify-center gap-2 rounded-lg px-6 py-2.5 text-sm font-bold transition-all sm:flex-none ${
                        activeTab === 'GLOBAL'
                            ? 'bg-background text-foreground ring-border shadow-sm ring-1'
                            : 'text-txtcolor-300 hover:text-foreground'
                    }`}
                >
                    <Globe className="h-4 w-4" /> Role Globalne
                </button>
                <button
                    onClick={() => setActiveTab('SU')}
                    className={`flex flex-1 items-center justify-center gap-2 rounded-lg px-6 py-2.5 text-sm font-bold transition-all sm:flex-none ${
                        activeTab === 'SU'
                            ? 'bg-background text-foreground ring-border shadow-sm ring-1'
                            : 'text-txtcolor-300 hover:text-foreground'
                    }`}
                >
                    <Landmark className="h-4 w-4" /> Role Samorządu (SU)
                </button>
            </div>

            <div className="bg-background border-border flex flex-col overflow-hidden rounded-xl border shadow-sm">
                <div className="custom-scrollbar overflow-x-auto">
                    <table className="w-full border-collapse text-left text-sm">
                        <thead className="bg-secondarybg text-txtcolor-300 sticky top-0 z-20 text-xs font-bold uppercase">
                        <tr>
                            <th className="border-border bg-secondarybg sticky left-0 z-30 min-w-[220px] border-b px-4 py-4 shadow-[2px_0_5px_-2px_rgba(0,0,0,0.1)]">
                                Uprawnienie
                            </th>
                            {visibleRoles.map((role) => (
                                <th
                                    key={role}
                                    className="border-border border-border/50 min-w-[100px] border-b border-l px-2 py-4 text-center align-middle"
                                >
                                    <div className="flex flex-col items-center justify-center h-full">
                                        <span className="whitespace-normal leading-tight">
                                                {formatRole(role)}
                                            </span>
                                    </div>
                                </th>
                            ))}
                        </tr>
                        </thead>
                        <tbody className="divide-border divide-y">
                        {allPermissions.map((perm) => (
                            <tr
                                key={perm}
                                className="hover:bg-secondarybg/30 group transition-colors"
                            >
                                <td className="text-foreground bg-background group-hover:bg-secondarybg/30 border-border sticky left-0 z-20 border-r px-4 py-3 font-mono text-[11px] font-bold shadow-[2px_0_5px_-2px_rgba(0,0,0,0.1)]">
                                    {perm}
                                </td>

                                {visibleRoles.map((role) => {
                                    const hasPermission = matrix[role].includes(perm);
                                    const isCritical =
                                        role === 'ADMINISTRATOR' && perm === 'ROLE_MANAGE';

                                    return (
                                        <td
                                            key={role}
                                            className="border-border/50 border-l px-4 py-3 text-center"
                                        >
                                            <button
                                                onClick={() =>
                                                    !isCritical &&
                                                    togglePermission({
                                                        role,
                                                        perm,
                                                        hasPerm: hasPermission,
                                                    })
                                                }
                                                disabled={isCritical}
                                                className={`mx-auto flex h-9 w-9 items-center justify-center rounded-lg transition-all 
                                                        ${isCritical ? 'bg-secondarybg cursor-not-allowed opacity-50' : ''} 
                                                        ${!isCritical && hasPermission ? 'bg-success text-darkgray scale-100 shadow-sm hover:opacity-90' : ''} 
                                                        ${!isCritical && !hasPermission ? 'bg-inputbg text-txtcolor-300 hover:bg-secondarybg hover:text-foreground scale-90 hover:scale-100' : ''} 
                                                    `}
                                                title={
                                                    isCritical
                                                        ? 'Wymagane przez system'
                                                        : hasPermission
                                                            ? 'Kliknij, aby odebrać'
                                                            : 'Kliknij, aby nadać'
                                                }
                                            >
                                                {isCritical ? (
                                                    <Lock className="text-txtcolor-300 h-4 w-4" />
                                                ) : hasPermission ? (
                                                    <Check className="h-5 w-5" />
                                                ) : (
                                                    <X className="h-4 w-4 opacity-20" />
                                                )}
                                            </button>
                                        </td>
                                    );
                                })}
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>
            </div>

            <p className="text-txtcolor-300 text-center text-xs italic">
                Przewijaj tabelę w poziomie, aby zobaczyć więcej ról.
            </p>
        </div>
    );
}