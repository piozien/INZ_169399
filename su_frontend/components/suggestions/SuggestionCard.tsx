'use client';

import {SuggestionDto} from '@/types/suggestions.types';
import {Check, X, Trash2, Edit, User, VenetianMask} from 'lucide-react';
import {format} from 'date-fns';
import {pl} from 'date-fns/locale';
import {toast} from 'sonner';

interface Props {
    suggestion: SuggestionDto;
    canApprove: boolean;
    canReject: boolean;
    canDelete: boolean;
    canEdit?: boolean;
    onApprove: (id: string) => void;
    onReject: (id: string) => void;
    onDelete: (id: string) => void;
    onEdit?: () => void;
    onClick?: () => void;
}

const STATUS_STYLES = {
    PENDING: {
        color: 'text-warning',
        bg: 'bg-warning/10',
        border: 'border-warning/20',
        label: 'Oczekująca',
        bar: 'bg-warning',
    },
    APPROVED: {
        color: 'text-success',
        bg: 'bg-success/10',
        border: 'border-success/20',
        label: 'Zatwierdzona',
        bar: 'bg-success',
    },
    REJECTED: {
        color: 'text-error',
        bg: 'bg-error/10',
        border: 'border-error/20',
        label: 'Odrzucona',
        bar: 'bg-error',
    },
};

export default function SuggestionCard({
                                           suggestion,
                                           canApprove,
                                           canReject,
                                           canDelete,
                                           canEdit = false,
                                           onApprove,
                                           onReject,
                                           onDelete,
                                           onEdit,
                                           onClick,
                                       }: Props) {
    const status = STATUS_STYLES[suggestion.status] || STATUS_STYLES.PENDING;
    const isAnon = suggestion.anonymous;
    const authorName = isAnon ? 'Anonimowy' : suggestion.fullName || 'Uczeń';

    const handleDeleteClick = () => {
        toast('Czy na pewno chcesz usunąć tę sugestię?', {
            description: 'Operacja jest nieodwracalna.',
            action: {
                label: 'Usuń',
                onClick: () => onDelete(suggestion.id),
            },
            cancel: {
                label: 'Anuluj',
                onClick: () => {
                },
            },
        });
    };

    return (
        <div
            onClick={onClick}
            className={`bg-secondarybg/20 border-border hover:border-secondary/40 group relative flex flex-col overflow-hidden rounded-2xl border p-5 shadow-sm backdrop-blur-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-xl ${onClick ? 'cursor-pointer' : ''}`}
        >
            <div className={`absolute top-0 bottom-0 left-0 w-1 ${status.bar} opacity-60`}/>

            <div className="mb-3 flex items-start justify-between gap-4 pl-2">
                <div className="flex items-center gap-3 overflow-hidden">
                    <div
                        className={`flex h-10 w-10 shrink-0 items-center justify-center rounded-full border border-white/5 shadow-inner ${isAnon ? 'bg-secondarybg text-txtcolor-300' : 'from-primary/80 to-secondary/80 text-darkgray bg-gradient-to-br'}`}
                    >
                        {isAnon ? (
                            <VenetianMask className="h-5 w-5"/>
                        ) : (
                            <User className="h-5 w-5"/>
                        )}
                    </div>

                    <div className="min-w-0">
                        <h3 className="text-foreground group-hover:text-primary truncate pr-2 text-lg leading-tight font-bold transition-colors">
                            {suggestion.title}
                        </h3>
                        <div className="text-txtcolor-300 mt-0.5 flex items-center gap-1.5 text-xs">
                            <span className="max-w-[150px] truncate font-medium" title={authorName}>
                                {authorName}
                            </span>
                            <span className="bg-txtcolor-300/40 h-1 w-1 shrink-0 rounded-full"/>
                            <span className="shrink-0">
                                {format(new Date(suggestion.createdAt), 'dd.MM.yyyy', {
                                    locale: pl,
                                })}
                            </span>
                        </div>
                    </div>
                </div>

                <span
                    className={`shrink-0 rounded-full border px-2.5 py-1 text-[10px] font-bold tracking-wider uppercase ${status.bg} ${status.color} ${status.border}`}
                >
                    {status.label}
                </span>
            </div>

            <div className="mb-4 flex-grow pl-2">
                <p className="text-txtcolor-300 line-clamp-3 text-sm leading-relaxed">
                    {suggestion.description}
                </p>
            </div>

            <div
                className="border-border/50 mt-auto flex flex-col items-start justify-between gap-4 border-t pt-4 pl-2 sm:flex-row sm:items-center">
                <div className="flex flex-wrap gap-2">
                    {(suggestion.tags || []).map((tag) => (
                        <span
                            key={tag}
                            className="text-txtcolor-300 bg-background/40 border-border/60 rounded-md border px-2 py-0.5 text-[10px] font-bold tracking-wide uppercase"
                        >
                            #{tag}
                        </span>
                    ))}
                </div>

                <div
                    className="flex gap-2 self-end sm:self-auto"
                    onClick={(e) => e.stopPropagation()}
                >
                    {canEdit && onEdit && suggestion.status === 'PENDING' && (
                        <ActionButton
                            onClick={onEdit}
                            icon={Edit}
                            title="Edytuj"
                            variant="default"
                        />
                    )}

                    {canApprove && suggestion.status === 'PENDING' && (
                        <ActionButton
                            onClick={() => onApprove(suggestion.id)}
                            icon={Check}
                            title="Zatwierdź"
                            variant="success"
                        />
                    )}

                    {canReject && suggestion.status === 'PENDING' && (
                        <ActionButton
                            onClick={() => onReject(suggestion.id)}
                            icon={X}
                            title="Odrzuć"
                            variant="error"
                        />
                    )}

                    {canDelete && (
                        <ActionButton
                            onClick={handleDeleteClick}
                            icon={Trash2}
                            title="Usuń"
                            variant="delete"
                        />
                    )}
                </div>
            </div>

            {suggestion.rejectionReason && (
                <div
                    className="text-error bg-error/5 border-error/10 animate-in slide-in-from-top-1 mx-2 mt-3 flex items-start gap-2 rounded-xl border p-3 text-xs">
                    <span className="font-bold whitespace-nowrap">Powód:</span>
                    <span className="leading-snug opacity-90">{suggestion.rejectionReason}</span>
                </div>
            )}
        </div>
    );
}

const ActionButton = ({onClick, icon: Icon, title, variant}: any) => {
    const variants: any = {
        default: 'text-txtcolor-300 hover:text-primary hover:border-primary',
        success: 'text-success hover:bg-success hover:text-darkgray hover:border-success',
        error: 'text-error hover:bg-error hover:text-white hover:border-error',
        delete: 'text-txtcolor-300 hover:text-error hover:border-error',
    };

    return (
        <button
            onClick={onClick}
            className={`bg-background border-border rounded-xl border p-2 shadow-sm transition-all ${variants[variant]}`}
            title={title}
        >
            <Icon className="h-4 w-4"/>
        </button>
    );
};