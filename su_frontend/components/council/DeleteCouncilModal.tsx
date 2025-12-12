'use client';

import { AlertTriangle, Loader2, X } from 'lucide-react';

interface DeleteCouncilModalProps {
    isOpen: boolean;
    onClose: () => void;
    onConfirm: () => void;
    isDeleting: boolean;
    councilName: string;
}

export default function DeleteCouncilModal({
                                               isOpen,
                                               onClose,
                                               onConfirm,
                                               isDeleting,
                                               councilName,
                                           }: DeleteCouncilModalProps) {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="w-full max-w-md overflow-hidden rounded-xl border border-border bg-secondarybg shadow-2xl animate-in zoom-in-95">
                <div className="relative p-6">
                    <button
                        onClick={onClose}
                        disabled={isDeleting}
                        className="absolute right-4 top-4 text-txtcolor-300 transition-colors hover:text-foreground"
                    >
                        <X className="h-6 w-6" />
                    </button>

                    <div className="flex flex-col items-center text-center">
                        <div className="mb-4 rounded-full bg-error/10 p-3 text-error">
                            <AlertTriangle className="h-8 w-8" />
                        </div>

                        <h3 className="mb-2 text-xl font-bold text-foreground">
                            Usuwanie samorządu
                        </h3>

                        <p className="mb-6 text-txtcolor-300">
                            Czy na pewno chcesz trwale usunąć samorząd <br />
                            <span className="font-bold text-foreground">"{councilName}"</span>?
                            <br />
                            <span className="mt-2 block text-sm text-error">
                                Tej operacji nie można cofnąć.
                            </span>
                        </p>

                        <div className="flex w-full gap-3">
                            <button
                                onClick={onClose}
                                disabled={isDeleting}
                                className="flex-1 rounded-lg border border-border bg-transparent py-2.5 font-medium text-foreground transition-colors hover:bg-inputbg"
                            >
                                Anuluj
                            </button>
                            <button
                                onClick={onConfirm}
                                disabled={isDeleting}
                                className="flex flex-1 items-center justify-center gap-2 rounded-lg bg-error py-2.5 font-medium foreground transition-colors hover:bg-error/90 disabled:opacity-70"
                            >
                                {isDeleting ? (
                                    <>
                                        <Loader2 className="h-4 w-4 animate-spin" />
                                        Usuwanie...
                                    </>
                                ) : (
                                    'Usuń trwale'
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}