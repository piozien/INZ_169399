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

export default function MemberCard({ member, canManage, onEdit, onDelete, onClick }: Props) {

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

    return (
        <div
            onClick={() => onClick(member.userId)}
            className="group relative bg-secondarybg border border-border rounded-xl p-5 transition-all duration-300 hover:border-secondary cursor-pointer flex flex-col items-center text-center pb-12 lg:pb-5"
        >
            <div className="w-16 h-16 rounded-full bg-background flex items-center justify-center mb-4 border-2 border-border group-hover:border-secondary/50 transition-colors shadow-lg">
                <UserIcon className="w-8 h-8 text-txtcolor-300 group-hover:text-secondary transition-colors" />
            </div>

            <h3 className="text-lg font-bold text-foreground mb-1 line-clamp-1">
                {member.userFullName}
            </h3>
            <p className="text-xs text-txtcolor-300 mb-4 line-clamp-1">{member.userEmail}</p>

            <span className={`px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wide border ${getRoleStyle(member.role)}`}>
                {member.roleName || member.role}
            </span>

            {canManage && (
                <div className={`
                    absolute 
                    bottom-3 right-3 flex gap-2 opacity-100 translate-y-0
                    
                    lg:top-3 lg:bottom-auto lg:opacity-0 lg:group-hover:opacity-100 lg:transform lg:translate-y-[-5px] lg:group-hover:translate-y-0
                    
                    transition-all duration-200
                `}>
                    <button
                        onClick={(e) => { e.stopPropagation(); onEdit(member.userId); }}
                        className="p-2 bg-background rounded-lg border border-border text-txtcolor-300 hover:text-primary hover:border-primary transition-colors shadow-sm"
                        title="Edytuj rolę"
                    >
                        <Edit className="w-4 h-4" />
                    </button>
                    <button
                        onClick={(e) => { e.stopPropagation(); onDelete(member.userId, member.userFullName); }}
                        className="p-2 bg-background rounded-lg border border-border text-txtcolor-300 hover:text-error hover:border-error transition-colors shadow-sm"
                        title="Usuń z samorządu"
                    >
                        <Trash2 className="w-4 h-4" />
                    </button>
                </div>
            )}
        </div>
    );
}