'use client';

import {SuggestionDto} from "@/types/suggestions.types";
import {Check, X, Trash2, Edit, User, VenetianMask} from "lucide-react";

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
                                           onClick
                                       }: Props) {
    const statusConfig = {
        PENDING: {
            color: "text-warning",
            bg: "bg-warning/10",
            border: "border-warning/20",
            label: "Oczekująca",
            bar: "bg-warning"
        },
        APPROVED: {
            color: "text-success",
            bg: "bg-success/10",
            border: "border-success/20",
            label: "Zatwierdzona",
            bar: "bg-success"
        },
        REJECTED: {
            color: "text-error",
            bg: "bg-error/10",
            border: "border-error/20",
            label: "Odrzucona",
            bar: "bg-error"
        },
    };

    const status = statusConfig[suggestion.status];
    const isAnon = suggestion.anonymous;
    const authorName = isAnon ? "Anonimowy" : (suggestion.fullName || "Uczeń");

    return (
        <div
            onClick={onClick}
            className={`bg-secondarybg/20 backdrop-blur-sm border border-border rounded-2xl p-5 flex flex-col shadow-sm hover:shadow-xl hover:border-secondary/40 hover:-translate-y-1 transition-all duration-300 group relative overflow-hidden ${onClick ? 'cursor-pointer' : ''}`}
        >

            <div className={`absolute left-0 top-0 bottom-0 w-1 ${status.bar} opacity-60`}/>

            <div className="flex justify-between items-start gap-4 mb-3 pl-2">
                <div className="flex gap-3 items-center overflow-hidden">
                    <div
                        className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 shadow-inner border border-white/5 ${
                            isAnon
                                ? 'bg-secondarybg text-txtcolor-300'
                                : 'bg-gradient-to-br from-primary/80 to-secondary/80 text-darkgray'
                        }`}>
                        {isAnon ? <VenetianMask className="w-5 h-5"/> : <User className="w-5 h-5"/>}
                    </div>

                    <div className="min-w-0">
                        <h3 className="font-bold text-foreground text-lg leading-tight group-hover:text-primary transition-colors truncate pr-2">
                            {suggestion.title}
                        </h3>
                        <div className="flex items-center gap-1.5 text-xs text-txtcolor-300 mt-0.5">
                            <span className="font-medium truncate max-w-[150px]" title={authorName}>
                                {authorName}
                            </span>
                            <span className="w-1 h-1 rounded-full bg-txtcolor-300/40 shrink-0"/>
                            <span className="shrink-0">{new Date(suggestion.createdAt).toLocaleDateString()}</span>
                        </div>
                    </div>
                </div>

                <span
                    className={`px-2.5 py-1 rounded-full text-[10px] uppercase tracking-wider font-bold border shrink-0 ${status.bg} ${status.color} ${status.border}`}>
                    {status.label}
                </span>
            </div>

            <div className="pl-2 mb-4 flex-grow">
                <p className="text-txtcolor-300 text-sm leading-relaxed line-clamp-3">
                    {suggestion.description}
                </p>
            </div>

            <div
                className="mt-auto pl-2 pt-4 border-t border-border/50 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div className="flex flex-wrap gap-2">
                    {(suggestion.tags || []).map(tag => (
                        <span key={tag}
                              className="text-[10px] uppercase font-bold text-txtcolor-300 bg-background/40 border border-border/60 px-2 py-0.5 rounded-md tracking-wide">
                            #{tag}
                        </span>
                    ))}
                </div>

                <div className="flex gap-2 self-end sm:self-auto" onClick={(e) => e.stopPropagation()}>
                    {canEdit && onEdit && suggestion.status === 'PENDING' && (
                        <button
                            onClick={onEdit}
                            className="p-2 rounded-xl bg-background border border-border text-txtcolor-300 hover:text-primary hover:border-primary transition-all shadow-sm"
                            title="Edytuj"
                        >
                            <Edit className="w-4 h-4"/>
                        </button>
                    )}

                    {canApprove && suggestion.status === 'PENDING' && (
                        <button
                            onClick={() => onApprove(suggestion.id)}
                            className="p-2 rounded-xl bg-background border border-border text-success hover:bg-success hover:text-darkgray hover:border-success transition-all shadow-sm"
                            title="Zatwierdź"
                        >
                            <Check className="w-4 h-4"/>
                        </button>
                    )}

                    {canReject && suggestion.status === 'PENDING' && (
                        <button
                            onClick={() => onReject(suggestion.id)}
                            className="p-2 rounded-xl bg-background border border-border text-error hover:bg-error hover:text-white hover:border-error transition-all shadow-sm"
                            title="Odrzuć"
                        >
                            <X className="w-4 h-4"/>
                        </button>
                    )}

                    {canDelete && (
                        <button
                            onClick={() => {
                                if (confirm("Czy na pewno chcesz usunąć tę sugestię?")) onDelete(suggestion.id)
                            }}
                            className="p-2 rounded-xl bg-background border border-border text-txtcolor-300 hover:text-error hover:border-error transition-all shadow-sm"
                            title="Usuń"
                        >
                            <Trash2 className="w-4 h-4"/>
                        </button>
                    )}
                </div>
            </div>

            {suggestion.rejectionReason && (
                <div
                    className="mx-2 mt-3 text-xs text-error bg-error/5 p-3 rounded-xl border border-error/10 flex gap-2 items-start animate-in slide-in-from-top-1">
                    <span className="font-bold whitespace-nowrap">Powód:</span>
                    <span className="opacity-90 leading-snug">{suggestion.rejectionReason}</span>
                </div>
            )}
        </div>
    );
}