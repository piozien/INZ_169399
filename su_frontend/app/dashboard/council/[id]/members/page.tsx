'use client';

import { use } from 'react';
import { useRouter } from 'next/navigation';
import { Loader2, User, Plus, ShieldAlert, ArrowLeft } from 'lucide-react';
import MemberCard from '@/components/council/MemberCard';
import EditRoleModal from '@/components/council/EditRoleModal';
import AddMemberModal from '@/components/council/AddMemberModal';
import { useCouncilMembers } from '@/hooks/council/members/useCouncilMembers';

export default function CouncilMembersPage({ params }: { params: Promise<{ id: string }> }) {
    const { id: councilId } = use(params);
    const router = useRouter();

    const {
        members,
        isLoading,
        error,
        canManage,
        isAddModalOpen,
        openAddModal,
        closeAddModal,
        addMember,
        isAdding,
        isEditModalOpen,
        closeEditModal,
        openEditModal,
        saveRole,
        editingMember,
        isSaving,
        removeMember,
    } = useCouncilMembers(councilId);

    const handleDelete = (userId: string, userName: string) => {
        if (confirm(`Czy na pewno chcesz usunąć ${userName} z samorządu?`)) {
            removeMember(userId);
        }
    };

    if (isLoading)
        return (
            <div className="flex h-[50vh] items-center justify-center">
                <Loader2 className="text-primary h-8 w-8 animate-spin" />
            </div>
        );

    if (error) {
        return (
            <div className="text-error flex h-[50vh] flex-col items-center justify-center gap-4">
                <ShieldAlert className="h-12 w-12 opacity-50" />
                <p>Nie masz uprawnień do przeglądania listy członków tego samorządu.</p>
            </div>
        );
    }

    return (
        <div className="animate-in fade-in mx-auto max-w-7xl space-y-6 p-6 duration-500">
            <div className="border-border flex flex-col items-start justify-between gap-4 border-b pb-6 sm:flex-row sm:items-center">
                <div className="flex items-center gap-3">
                    <button
                        onClick={() => router.push(`/dashboard/council/${councilId}`)}
                        className="text-txtcolor-300 hover:text-foreground hover:bg-secondarybg -ml-2 rounded-xl p-2 transition-colors"
                        title="Powrót do samorządu"
                    >
                        <ArrowLeft className="h-6 w-6" />
                    </button>
                    <div>
                        <h1 className="text-foreground text-2xl font-bold">Członkowie Samorządu</h1>
                        <p className="text-txtcolor-300 mt-1">
                            Lista osób należących do bieżącej kadencji ({members.length || 0})
                        </p>
                    </div>
                </div>

                {canManage && (
                    <button
                        onClick={openAddModal}
                        className="bg-primary text-darkgray flex items-center gap-2 rounded-lg px-4 py-2 font-semibold shadow-md transition-opacity hover:opacity-90"
                    >
                        <Plus className="h-4 w-4" /> Dodaj członka
                    </button>
                )}
            </div>
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                {members.map((member) => (
                    <MemberCard
                        key={member.userId}
                        member={member}
                        canManage={canManage}
                        onEdit={openEditModal}
                        onDelete={handleDelete}
                        onClick={(id) => router.push(`/dashboard/profile/${id}`)}
                    />
                ))}
            </div>

            {members.length === 0 && (
                <div className="text-txtcolor-300 border-border flex flex-col items-center justify-center rounded-xl border-2 border-dashed py-20">
                    <User className="mb-4 h-12 w-12 opacity-20" />
                    <p>Brak członków w tym samorządzie.</p>
                </div>
            )}

            {editingMember && (
                <EditRoleModal
                    isOpen={isEditModalOpen}
                    onClose={closeEditModal}
                    onSave={saveRole}
                    isSaving={isSaving}
                    currentRole={editingMember.role}
                    memberName={editingMember.name}
                />
            )}

            <AddMemberModal
                isOpen={isAddModalOpen}
                onClose={closeAddModal}
                onAdd={addMember}
                isAdding={isAdding}
            />
        </div>
    );
}
