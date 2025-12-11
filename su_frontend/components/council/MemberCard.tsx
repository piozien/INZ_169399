'use client';

import { User as UserIcon, Edit, Trash2 } from 'lucide-react';
import { CouncilMemberDto } from '@/types/council.types';

interface Props {
    member: CouncilMemberDto;
    canManage: boolean;
    onEdit: (userId: string) => void;
    onDelete: (userId: string, userName: string) => void;
    onClick: (userId: string) => void;
}

const getRoleStyle = (roleCode: string) => {
    switch (roleCode) {
        case 'PRZEWODNICZACY_SU':
            return 'bg-warning/10 text-warning border-warning/20';
        case 'ZASTEPCA_SU':
            return 'bg-secondary/10 text-secondary border-secondary/20';
        case 'SKARBNIK_SU':
            return 'bg-success/10 text-success border-success/20';
        case 'OPIEKUN_SU':
            return 'bg-accent/10 text-accent border-accent/20';
        case 'BYLY_CZLONEK_SU':
            return 'bg-zinc/10 text-txtcolor-300 border-zinc/20';
        default:
            return 'bg-info/10 text-info border-info/20';
    }
};

export default function MemberCard({ member, canManage, onEdit, onDelete, onClick }: Props) {
    return (
        <div
            onClick={() => onClick(member.userId)}
            className="group bg-secondarybg border-border hover:border-secondary relative flex cursor-pointer flex-col items-center rounded-xl border p-5 pb-12 text-center transition-all duration-300 lg:pb-5"
        >
            <div className="bg-background border-border group-hover:border-secondary/50 mb-4 flex h-16 w-16 items-center justify-center rounded-full border-2 shadow-lg transition-colors">
                <UserIcon className="text-txtcolor-300 group-hover:text-secondary h-8 w-8 transition-colors" />
            </div>

            <h3 className="text-foreground mb-1 line-clamp-1 text-lg font-bold">
                {member.userFullName}
            </h3>
            <p className="text-txtcolor-300 mb-4 line-clamp-1 text-xs">{member.userEmail}</p>

            <span
                className={`rounded-full border px-3 py-1 text-xs font-bold tracking-wide uppercase ${getRoleStyle(member.role)}`}
            >
                {member.roleName || member.role}
            </span>

            {canManage && (
                <div
                    className={`absolute right-3 bottom-3 flex translate-y-0 gap-2 opacity-100 transition-all duration-200 lg:top-3 lg:bottom-auto lg:translate-y-[-5px] lg:transform lg:opacity-0 lg:group-hover:translate-y-0 lg:group-hover:opacity-100`}
                >
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            onEdit(member.userId);
                        }}
                        className="bg-background border-border text-txtcolor-300 hover:text-primary hover:border-primary rounded-lg border p-2 shadow-sm transition-colors"
                        title="Edytuj rolę"
                    >
                        <Edit className="h-4 w-4" />
                    </button>
                    <button
                        onClick={(e) => {
                            e.stopPropagation();
                            onDelete(member.userId, member.userFullName);
                        }}
                        className="bg-background border-border text-txtcolor-300 hover:text-error hover:border-error rounded-lg border p-2 shadow-sm transition-colors"
                        title="Usuń z samorządu"
                    >
                        <Trash2 className="h-4 w-4" />
                    </button>
                </div>
            )}
        </div>
    );
}
