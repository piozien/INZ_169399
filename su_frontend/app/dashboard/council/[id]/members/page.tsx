'use client';

import { use, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    fetchCouncilMembers,
    removeMemberFromCouncil,
    updateMemberRole,
    addMemberToCouncil,
    fetchCouncilContext
} from '@/lib/api/council';
import { CouncilMemberDto, CouncilContextDto } from '@/types/council.types';
import { useRouter } from 'next/navigation';
import { Loader2, User, Plus, ShieldAlert } from 'lucide-react';
import MemberCard from '@/components/council/MemberCard';
import EditRoleModal from '@/components/council/EditRoleModal';
import AddMemberModal from '@/components/council/AddMemberModal';

export default function CouncilMembersPage({ params }: { params: Promise<{ id: string }> }) {
    const { id: councilId } = use(params);
    const queryClient = useQueryClient();
    const router = useRouter();

    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [editingMember, setEditingMember] = useState<{ id: string; name: string; role: string } | null>(null);

    const { data: members, isLoading: membersLoading, error } = useQuery<CouncilMemberDto[]>({
        queryKey: ['councilMembers', councilId],
        queryFn: () => fetchCouncilMembers(councilId),
    });

    const { data: context, isLoading: contextLoading } = useQuery<CouncilContextDto>({
        queryKey: ['councilContext', councilId],
        queryFn: () => fetchCouncilContext(councilId),
    });

    const canManage = context?.permissions?.includes('COUNCIL_MEMBER_MANAGE') ||
        context?.permissions?.includes('ALL_ACCESS') || false;

    const removeMutation = useMutation({
        mutationFn: (userId: string) => removeMemberFromCouncil(councilId, userId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['councilMembers', councilId] }),
        onError: (err) => alert(err instanceof Error ? err.message : 'Błąd usuwania'),
    });

    const updateRoleMutation = useMutation({
        mutationFn: ({ userId, newRole }: { userId: string; newRole: string }) =>
            updateMemberRole(councilId, userId, newRole),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['councilMembers', councilId] });
            setIsEditModalOpen(false);
            setEditingMember(null);
        },
        onError: (err) => alert(err instanceof Error ? err.message : 'Błąd edycji roli'),
    });

    const addMutation = useMutation({
        mutationFn: ({ userId, roleCode }: { userId: string; roleCode: string }) =>
            addMemberToCouncil(councilId, userId, roleCode),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['councilMembers', councilId] });
            setIsAddModalOpen(false);
        },
        onError: (err) => alert(err instanceof Error ? err.message : 'Błąd dodawania członka'),
    });

    const handleEditClick = (userId: string) => {
        const member = members?.find(m => m.userId === userId);
        if (member) {
            setEditingMember({ id: member.userId, name: member.userFullName, role: member.role });
            setIsEditModalOpen(true);
        }
    };

    const handleSaveRole = (newRole: string) => {
        if (editingMember) {
            updateRoleMutation.mutate({ userId: editingMember.id, newRole });
        }
    };

    const handleAddMember = (userId: string, roleCode: string) => {
        addMutation.mutate({ userId, roleCode });
    };

    const handleDelete = (userId: string, userName: string) => {
        if (confirm(`Czy na pewno chcesz usunąć ${userName} z samorządu?`)) {
            removeMutation.mutate(userId);
        }
    };

    if (membersLoading || contextLoading) return <div className="flex justify-center items-center h-[50vh]"><Loader2 className="h-8 w-8 animate-spin text-primary" /></div>;

    if (error) {
        return (
            <div className="flex flex-col items-center justify-center h-[50vh] text-error gap-4">
                <ShieldAlert className="h-12 w-12 opacity-50" />
                <p>Nie masz uprawnień do przeglądania listy członków tego samorządu.</p>
            </div>
        );
    }

    return (
        <div className="p-6 space-y-6 max-w-7xl mx-auto animate-in fade-in duration-500">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 border-b border-border pb-6">
                <div>
                    <h1 className="text-2xl font-bold text-foreground">Członkowie Samorządu</h1>
                    <p className="text-txtcolor-300 mt-1">
                        Lista osób należących do bieżącej kadencji ({members?.length || 0})
                    </p>
                </div>
                {canManage && (
                    <button
                        onClick={() => setIsAddModalOpen(true)}
                        className="flex items-center gap-2 bg-primary text-darkgray font-semibold px-4 py-2 rounded-lg hover:opacity-90 transition-opacity shadow-md"
                    >
                        <Plus className="h-4 w-4" /> Dodaj członka
                    </button>
                )}
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
                {members?.map((member) => (
                    <MemberCard
                        key={member.userId}
                        member={member}
                        canManage={canManage}
                        onEdit={handleEditClick}
                        onDelete={handleDelete}
                        onClick={(id) => router.push(`/dashboard/profile/${id}`)}
                    />
                ))}
            </div>

            {members?.length === 0 && (
                <div className="flex flex-col items-center justify-center py-20 text-txtcolor-300 border-2 border-dashed border-border rounded-xl">
                    <User className="h-12 w-12 mb-4 opacity-20" />
                    <p>Brak członków w tym samorządzie.</p>
                </div>
            )}

            {editingMember && (
                <EditRoleModal
                    isOpen={isEditModalOpen}
                    onClose={() => setIsEditModalOpen(false)}
                    onSave={handleSaveRole}
                    isSaving={updateRoleMutation.isPending}
                    currentRole={editingMember.role}
                    memberName={editingMember.name}
                />
            )}

            <AddMemberModal
                isOpen={isAddModalOpen}
                onClose={() => setIsAddModalOpen(false)}
                onAdd={handleAddMember}
                isAdding={addMutation.isPending}
            />
        </div>
    );
}