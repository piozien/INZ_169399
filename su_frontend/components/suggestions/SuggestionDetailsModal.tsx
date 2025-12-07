'use client';

import {useState} from "react";
import {X, User, VenetianMask, Calendar, Hash, CheckCircle, XCircle, AlertTriangle} from "lucide-react";
import {SuggestionDto} from "@/types/suggestions.types";

interface Props {
    isOpen: boolean;
    onClose: () => void;
    suggestion: SuggestionDto;
    canApprove?: boolean;
    canReject?: boolean;
    onApprove?: (id: string) => void;
    onReject?: (id: string, reason: string) => void;
}

export default function SuggestionDetailsModal({
                                                   isOpen,
                                                   onClose,
                                                   suggestion,
                                                   canApprove,
                                                   canReject,
                                                   onApprove,
                                                   onReject
                                               }: Props) {
    const [isRejecting, setIsRejecting] = useState(false);
    const [rejectionReason, setRejectionReason] = useState("");

    if (!isOpen) return null;

    const statusConfig = {
        PENDING: {color: "text-warning", bg: "bg-warning/10", border: "border-warning/20", label: "Oczekująca"},
        APPROVED: {color: "text-success", bg: "bg-success/10", border: "border-success/20", label: "Zatwierdzona"},
        REJECTED: {color: "text-error", bg: "bg-error/10", border: "border-error/20", label: "Odrzucona"},
    };

    const status = statusConfig[suggestion.status];
    const isAnon = suggestion.anonymous;
    const authorName = isAnon ? "Anonimowy" : (suggestion.fullName || "Uczeń");

    const handleRejectSubmit = () => {
        if (onReject && rejectionReason.trim()) {
            onReject(suggestion.id, rejectionReason);
        }
    };

    const handleApprove = () => {
        if (onApprove) {
            onApprove(suggestion.id);
        }
    };

    return (
        <div
            className="fixed inset-0 z-[60] flex items-center justify-center bg-black/80 backdrop-blur-sm p-4 animate-in fade-in duration-200"
            onClick={onClose}>
            <div
                className="bg-background border border-secondarybg w-full max-w-2xl max-h-[90vh] rounded-2xl shadow-2xl overflow-hidden animate-in zoom-in-95 flex flex-col"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="bg-surface p-5 border-b border-secondarybg flex justify-between items-start gap-4">
                    <div className="flex gap-4 items-center">
                        <div
                            className={`w-12 h-12 rounded-full flex items-center justify-center shrink-0 border border-white/5 ${
                                isAnon
                                    ? 'bg-secondarybg text-txtcolor-300'
                                    : 'bg-gradient-to-br from-primary/80 to-secondary/80 text-darkgray shadow-lg'
                            }`}>
                            {isAnon ? <VenetianMask className="w-6 h-6"/> : <User className="w-6 h-6"/>}
                        </div>
                        <div>
                            <h2 className="text-xl font-bold text-foreground leading-tight">{suggestion.title}</h2>
                            <div className="flex items-center gap-2 text-sm text-txtcolor-300 mt-1">
                                <span className="font-medium">{authorName}</span>
                                <span className="w-1 h-1 rounded-full bg-txtcolor-300/40"/>
                                <span className="flex items-center gap-1">
                                    <Calendar className="w-3 h-3"/>
                                    {new Date(suggestion.createdAt).toLocaleDateString()}
                                </span>
                            </div>
                        </div>
                    </div>
                    <button onClick={onClose}
                            className="text-txtcolor-300 hover:text-foreground p-1 hover:bg-white/5 rounded-lg transition-colors">
                        <X className="w-6 h-6"/>
                    </button>
                </div>

                <div className="p-6 overflow-y-auto custom-scrollbar flex-1">
                    <div className="mb-6">
                        <span
                            className={`px-3 py-1.5 rounded-full text-xs uppercase tracking-wider font-bold border ${status.bg} ${status.color} ${status.border}`}>
                            Status: {status.label}
                        </span>
                    </div>

                    <div className="prose prose-invert max-w-none mb-8">
                        <p className="text-foreground text-base leading-relaxed whitespace-pre-wrap">
                            {suggestion.description}
                        </p>
                    </div>

                    {suggestion.tags && suggestion.tags.length > 0 && (
                        <div className="flex flex-wrap gap-2 pt-4 border-t border-border/50">
                            <Hash className="w-4 h-4 text-txtcolor-300 mt-1"/>
                            {suggestion.tags.map(tag => (
                                <span key={tag}
                                      className="text-xs font-bold text-txtcolor-300 bg-secondarybg/50 border border-border px-2.5 py-1 rounded-md">
                                    {tag}
                                </span>
                            ))}
                        </div>
                    )}

                    {suggestion.rejectionReason && (
                        <div className="mt-6 bg-error/10 border border-error/20 rounded-xl p-4">
                            <h4 className="text-error font-bold text-sm mb-1">Powód odrzucenia:</h4>
                            <p className="text-error/90 text-sm">{suggestion.rejectionReason}</p>
                        </div>
                    )}
                </div>

                {(canApprove || canReject) && suggestion.status === 'PENDING' && (
                    <div className="p-5 border-t border-secondarybg bg-surface/50">
                        {!isRejecting ? (
                            <div className="flex gap-3 justify-end">
                                {canReject && onReject && (
                                    <button
                                        onClick={() => setIsRejecting(true)}
                                        className="px-4 py-2.5 rounded-xl border border-error/30 text-error hover:bg-error hover:text-white transition-all font-bold text-sm flex items-center gap-2"
                                    >
                                        <XCircle className="w-4 h-4"/> Odrzuć
                                    </button>
                                )}
                                {canApprove && onApprove && (
                                    <button
                                        onClick={handleApprove}
                                        className="px-5 py-2.5 rounded-xl bg-success text-darkgray hover:bg-success/90 font-bold text-sm flex items-center gap-2 shadow-lg shadow-success/20"
                                    >
                                        <CheckCircle className="w-4 h-4"/> Zatwierdź
                                    </button>
                                )}
                            </div>
                        ) : (
                            <div className="space-y-3 animate-in slide-in-from-bottom-2 fade-in">
                                <div className="flex items-center gap-2 text-error font-bold text-sm">
                                    <AlertTriangle className="w-4 h-4"/> Podaj powód odrzucenia
                                </div>
                                <textarea
                                    value={rejectionReason}
                                    onChange={(e) => setRejectionReason(e.target.value)}
                                    placeholder="np. Niezgodne z regulaminem szkoły..."
                                    className="w-full bg-inputbg border border-border rounded-xl p-3 text-sm text-foreground focus:outline-none focus:ring-2 focus:ring-error/50 resize-none h-24"
                                    autoFocus
                                />
                                <div className="flex gap-3 justify-end">
                                    <button
                                        onClick={() => {
                                            setIsRejecting(false);
                                            setRejectionReason("");
                                        }}
                                        className="px-4 py-2 rounded-lg text-txtcolor-300 hover:text-foreground text-sm font-medium"
                                    >
                                        Anuluj
                                    </button>
                                    <button
                                        onClick={handleRejectSubmit}
                                        disabled={!rejectionReason.trim()}
                                        className="px-4 py-2 rounded-lg bg-error text-white hover:bg-error/90 disabled:opacity-50 disabled:cursor-not-allowed font-bold text-sm"
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