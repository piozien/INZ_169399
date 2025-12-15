'use client';

import { useState } from 'react';
import {
    X,
    User,
    VenetianMask,
    Calendar,
    Hash,
    CheckCircle,
    XCircle,
    AlertTriangle,
} from 'lucide-react';
import { SuggestionDto } from '@/types/suggestions.types';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    suggestion: SuggestionDto;
    canApprove?: boolean;
    canReject?: boolean;
    onApprove?: (id: string) => void;
    onReject?: (id: string, reason: string) => void;
}

const STATUS_CONFIG = {
    PENDING: {
        color: 'text-warning',
        bg: 'bg-warning/10',
        border: 'border-warning/20',
        label: 'Oczekująca',
    },
    APPROVED: {
        color: 'text-success',
        bg: 'bg-success/10',
        border: 'border-success/20',
        label: 'Zatwierdzona',
    },
    REJECTED: {
        color: 'text-error',
        bg: 'bg-error/10',
        border: 'border-error/20',
        label: 'Odrzucona',
    },
};

export default function SuggestionDetailsModal({
    isOpen,
    onClose,
    suggestion,
    canApprove,
    canReject,
    onApprove,
    onReject,
}: Props) {
    const [isRejecting, setIsRejecting] = useState(false);
    const [rejectionReason, setRejectionReason] = useState('');

    if (!isOpen) return null;

    const status = STATUS_CONFIG[suggestion.status];
    const isAnon = suggestion.anonymous;
    const authorName = isAnon ? 'Anonimowy' : suggestion.fullName || 'Uczeń';

    const handleRejectSubmit = () => {
        if (onReject && rejectionReason.trim()) {
            onReject(suggestion.id, rejectionReason);
        }
    };

    return (
        <div
            className="bg-bacground/80 animate-in fade-in fixed inset-0 z-[60] flex items-center justify-center p-4 backdrop-blur-sm duration-200"
            onClick={onClose}
        >
            <div
                className="bg-background border-secondarybg animate-in zoom-in-95 flex max-h-[90vh] w-full max-w-2xl flex-col overflow-hidden rounded-2xl border shadow-2xl"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="bg-surface border-secondarybg flex items-start justify-between gap-4 border-b p-5">
                    <div className="flex items-center gap-4">
                        <Avatar isAnon={isAnon} />
                        <div>
                            <h2 className="text-foreground text-xl leading-tight font-bold">
                                {suggestion.title}
                            </h2>
                            <div className="text-txtcolor-300 mt-1 flex items-center gap-2 text-sm">
                                <span className="font-medium">{authorName}</span>
                                <span className="bg-txtcolor-300/40 h-1 w-1 rounded-full" />
                                <span className="flex items-center gap-1">
                                    <Calendar className="h-3 w-3" />
                                    {new Date(suggestion.createdAt).toLocaleDateString()}
                                </span>
                            </div>
                        </div>
                    </div>
                    <button
                        onClick={onClose}
                        className="text-txtcolor-300 hover:text-foreground rounded-lg p-1 transition-colors hover:bg-white/5"
                    >
                        <X className="h-6 w-6" />
                    </button>
                </div>

                <div className="custom-scrollbar flex-1 overflow-y-auto p-6">
                    <div className="mb-6">
                        <span
                            className={`rounded-full border px-3 py-1.5 text-xs font-bold tracking-wider uppercase ${status.bg} ${status.color} ${status.border}`}
                        >
                            Status: {status.label}
                        </span>
                    </div>

                    <div className="prose prose-invert mb-8 max-w-none">
                        <p className="text-foreground text-base leading-relaxed whitespace-pre-wrap">
                            {suggestion.description}
                        </p>
                    </div>

                    {suggestion.tags && suggestion.tags.length > 0 && (
                        <div className="border-border/50 flex flex-wrap gap-2 border-t pt-4">
                            <Hash className="text-txtcolor-300 mt-1 h-4 w-4" />
                            {suggestion.tags.map((tag) => (
                                <span
                                    key={tag}
                                    className="text-txtcolor-300 bg-secondarybg/50 border-border rounded-md border px-2.5 py-1 text-xs font-bold"
                                >
                                    {tag}
                                </span>
                            ))}
                        </div>
                    )}

                    {suggestion.rejectionReason && (
                        <div className="bg-error/10 border-error/20 mt-6 rounded-xl border p-4">
                            <h4 className="text-error mb-1 text-sm font-bold">Powód odrzucenia:</h4>
                            <p className="text-error/90 text-sm">{suggestion.rejectionReason}</p>
                        </div>
                    )}
                </div>

                {(canApprove || canReject) && suggestion.status === 'PENDING' && (
                    <div className="border-secondarybg bg-surface/50 border-t p-5">
                        {!isRejecting ? (
                            <div className="flex justify-end gap-3">
                                {canReject && onReject && (
                                    <ActionButton
                                        onClick={() => setIsRejecting(true)}
                                        icon={XCircle}
                                        label="Odrzuć"
                                        variant="error"
                                    />
                                )}
                                {canApprove && onApprove && (
                                    <ActionButton
                                        onClick={() => onApprove(suggestion.id)}
                                        icon={CheckCircle}
                                        label="Zatwierdź"
                                        variant="success"
                                    />
                                )}
                            </div>
                        ) : (
                            <div className="animate-in slide-in-from-bottom-2 fade-in space-y-3">
                                <div className="text-error flex items-center gap-2 text-sm font-bold">
                                    <AlertTriangle className="h-4 w-4" /> Podaj powód odrzucenia
                                </div>
                                <textarea
                                    value={rejectionReason}
                                    onChange={(e) => setRejectionReason(e.target.value)}
                                    placeholder="np. Niezgodne z regulaminem szkoły..."
                                    className="bg-inputbg border-border text-foreground focus:ring-error/50 h-24 w-full resize-none rounded-xl border p-3 text-sm focus:ring-2 focus:outline-none"
                                    autoFocus
                                />
                                <div className="flex justify-end gap-3">
                                    <button
                                        onClick={() => {
                                            setIsRejecting(false);
                                            setRejectionReason('');
                                        }}
                                        className="text-txtcolor-300 hover:text-foreground rounded-lg px-4 py-2 text-sm font-medium"
                                    >
                                        Anuluj
                                    </button>
                                    <button
                                        onClick={handleRejectSubmit}
                                        disabled={!rejectionReason.trim()}
                                        className="bg-error text-darkgray hover:bg-error/90 rounded-lg px-4 py-2 text-sm font-bold disabled:cursor-not-allowed disabled:opacity-50"
                                    >
                                        Potwierdź odrzucenie
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
}

const Avatar = ({ isAnon }: { isAnon: boolean }) => (
    <div
        className={`border-foreground/5 flex h-12 w-12 shrink-0 items-center justify-center rounded-full border ${isAnon ? 'bg-secondarybg text-txtcolor-300' : 'from-primary/80 to-secondary/80 text-darkgray bg-gradient-to-br shadow-lg'}`}
    >
        {isAnon ? <VenetianMask className="h-6 w-6" /> : <User className="h-6 w-6" />}
    </div>
);

const ActionButton = ({ onClick, icon: Icon, label, variant }: any) => {
    const styles =
        variant === 'error'
            ? 'border border-error/30 text-error hover:bg-error hover:text-foreground'
            : 'bg-success text-darkgray hover:bg-success/90 shadow-lg shadow-success/20';

    return (
        <button
            onClick={onClick}
            className={`flex items-center gap-2 rounded-xl px-4 py-2.5 text-sm font-bold transition-all ${styles}`}
        >
            <Icon className="h-4 w-4" /> {label}
        </button>
    );
};
