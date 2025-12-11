'use client';

import { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { Search, MoreHorizontal, UserCheck, ExternalLink } from 'lucide-react';
import { useAdminUsers } from '@/hooks/admin/useAdminUsers';
import UserDetailsModal from './UserDetailsModal';
import UserStatusBadge from './UserStatusBadge';

export default function UsersManager() {
    const router = useRouter();
    const {
        users,
        isLoading,
        availableRoles,
        searchQuery,
        setSearchQuery,
        roleFilter,
        setRoleFilter,
        statusFilter,
        setStatusFilter,
        unblockUser,
    } = useAdminUsers();

    const [selectedUserId, setSelectedUserId] = useState<string | null>(null);

    const selectedUser = useMemo(() => {
        return users.find((u) => u.id === selectedUserId) || null;
    }, [users, selectedUserId]);

    if (isLoading) {
        return (
            <div className="flex h-64 items-center justify-center">
                <div className="border-primary h-8 w-8 animate-spin rounded-full border-4 border-t-transparent" />
            </div>
        );
    }

    const handleRowClick = (userId: string) => {
        router.push(`/dashboard/profile/${userId}`);
    };

    return (
        <div className="space-y-6">
            <div className="bg-secondarybg/30 border-border grid grid-cols-1 gap-4 rounded-xl border p-4 md:grid-cols-4">
                <div className="relative md:col-span-2">
                    <Search className="text-txtcolor-300 absolute top-1/2 left-3 h-4 w-4 -translate-y-1/2" />
                    <input
                        type="text"
                        placeholder="Szukaj po nazwisku lub email..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        className="bg-inputbg border-border focus:ring-primary w-full rounded-lg border py-2 pr-4 pl-10 text-sm transition-all focus:ring-2 focus:outline-none"
                    />
                </div>
                <div>
                    <select
                        value={roleFilter}
                        onChange={(e) => setRoleFilter(e.target.value)}
                        className="bg-inputbg border-border w-full cursor-pointer rounded-lg border px-3 py-2 text-sm"
                    >
                        <option value="ALL">Wszystkie Role</option>
                        {availableRoles.map((role) => (
                            <option key={role} value={role}>
                                {role}
                            </option>
                        ))}
                    </select>
                </div>
                <div>
                    <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value as any)}
                        className="bg-inputbg border-border w-full cursor-pointer rounded-lg border px-3 py-2 text-sm"
                    >
                        <option value="ALL">Wszystkie Statusy</option>
                        <option value="CONFIRMED">Aktywni</option>
                        <option value="PENDING">Oczekujący</option>
                        <option value="BLOCKED">Zablokowani</option>
                    </select>
                </div>
            </div>

            <div className="bg-background border-border overflow-hidden rounded-xl border shadow-sm">
                <div className="overflow-x-auto">
                    <table className="w-full text-left text-sm">
                        <thead className="bg-secondarybg text-txtcolor-300 border-border border-b text-xs font-bold uppercase">
                            <tr>
                                <th className="px-6 py-4 whitespace-nowrap">Użytkownik</th>
                                <th className="px-6 py-4 whitespace-nowrap">Role Globalne</th>
                                <th className="px-6 py-4 whitespace-nowrap">Status</th>
                                <th className="px-6 py-4 text-right whitespace-nowrap">Akcje</th>
                            </tr>
                        </thead>
                        <tbody className="divide-border divide-y">
                            {users.map((user) => (
                                <tr
                                    key={user.id}
                                    onClick={() => handleRowClick(user.id)}
                                    className="hover:bg-secondarybg/40 group cursor-pointer transition-colors"
                                >
                                    <td className="px-6 py-4">
                                        <div className="text-foreground flex items-center gap-2 font-bold">
                                            {user.fullName}
                                            <ExternalLink className="text-txtcolor-300 h-3 w-3 opacity-0 transition-opacity group-hover:opacity-100" />
                                        </div>
                                        <div className="text-txtcolor-300 text-xs">
                                            {user.email}
                                        </div>
                                    </td>
                                    <td className="px-6 py-4">
                                        <div className="flex flex-wrap gap-1.5">
                                            {user.roles.length > 0 ? (
                                                user.roles.map((role) => (
                                                    <span
                                                        key={role}
                                                        className="bg-primary/5 text-primary border-primary/20 rounded-md border px-2 py-0.5 text-[10px] font-bold"
                                                    >
                                                        {role}
                                                    </span>
                                                ))
                                            ) : (
                                                <span className="text-txtcolor-300 text-xs">-</span>
                                            )}
                                        </div>
                                    </td>
                                    <td className="px-6 py-4">
                                        <UserStatusBadge status={user.status} />
                                    </td>
                                    <td className="px-6 py-4 text-right">
                                        <div
                                            className="flex items-center justify-end gap-2"
                                            onClick={(e) => e.stopPropagation()}
                                        >
                                            {user.status === 'BLOCKED' && (
                                                <button
                                                    onClick={() => unblockUser(user.id)}
                                                    className="text-success hover:bg-success/10 hover:border-success/20 mr-2 flex items-center gap-1 rounded border border-transparent px-2 py-1 text-xs font-bold transition-colors"
                                                >
                                                    <UserCheck className="h-3.5 w-3.5" /> Odblokuj
                                                </button>
                                            )}

                                            <button
                                                onClick={() => setSelectedUserId(user.id)}
                                                className="bg-background border-border text-txtcolor-300 hover:text-primary hover:border-primary rounded-lg border p-2 shadow-sm transition-all"
                                                title="Zarządzaj (Admin Modal)"
                                            >
                                                <MoreHorizontal className="h-4 w-4" />
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>

            {selectedUser && (
                <UserDetailsModal
                    isOpen={!!selectedUser}
                    onClose={() => setSelectedUserId(null)}
                    user={selectedUser}
                />
            )}
        </div>
    );
}
