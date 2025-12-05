"use client";

import { X, MapPin, Users, Clock, AlignLeft } from 'lucide-react';
import { EventResponseDto } from '@/types/event.types';
import { format } from 'date-fns';
import { pl } from 'date-fns/locale';

interface Props {
    event: EventResponseDto;
    onClose: () => void;
    actions?: React.ReactNode;
}

export default function EventDetailsModal({ event, onClose, actions }: Props) {
    const handleBackdropClick = (e: React.MouseEvent) => {
        if (e.target === e.currentTarget) onClose();
    };

    const startDate = new Date(event.startDate);
    const endDate = new Date(event.endDate);

    return (
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-md p-4 animate-in fade-in duration-200"
            onClick={handleBackdropClick}
        >
            <div className="bg-background border border-secondarybg w-full max-w-2xl max-h-[90vh] rounded-3xl shadow-2xl flex flex-col relative overflow-hidden animate-in zoom-in-95 duration-200">
                
                <div className="relative shrink-0">
                    <div className="absolute inset-0 bg-gradient-to-br from-secondary/10 via-background to-background" />

                    <div className="relative p-6 pt-8 pr-12">
                         <span className="inline-block px-3 py-1 rounded-full bg-secondary/10 text-secondary text-xs font-bold uppercase tracking-wider mb-3 border border-secondary/20">
                            {format(startDate, 'd MMMM yyyy', { locale: pl })}
                        </span>
                        <h2 className="text-3xl md:text-4xl font-black text-foreground leading-tight drop-shadow-lg">
                            {event.title}
                        </h2>
                    </div>
                    
                    <button
                        onClick={onClose}
                        className="absolute top-4 right-4 p-2 bg-inputbg text-txtcolor-300 hover:text-foreground hover:bg-secondarybg rounded-full transition-all z-10 border border-transparent hover:border-secondary/20"
                    >
                        <X className="w-5 h-5" />
                    </button>
                </div>

                <div className="p-6 overflow-y-auto custom-scrollbar flex-1 space-y-8">

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">

                        <div className="bg-secondarybg p-4 rounded-2xl border border-secondarybg flex items-start gap-4 hover:border-secondary/30 transition-colors">
                            <div className="p-2.5 bg-inputbg rounded-xl text-secondary border border-secondary/10">
                                <Clock className="w-6 h-6" />
                            </div>
                            <div>
                                <p className="text-xs text-txtcolor-300 font-bold uppercase tracking-wide">Czas Trwania</p>
                                <p className="text-foreground font-semibold mt-0.5">
                                    {format(startDate, 'HH:mm', { locale: pl })} - {format(endDate, 'HH:mm', { locale: pl })}
                                </p>
                            </div>
                        </div>

                        <div className="bg-secondarybg p-4 rounded-2xl border border-secondarybg flex items-start gap-4 hover:border-secondary/30 transition-colors">
                            <div className="p-2.5 bg-inputbg rounded-xl text-secondary border border-secondary/10">
                                <MapPin className="w-6 h-6" />
                            </div>
                            <div>
                                <p className="text-xs text-txtcolor-300 font-bold uppercase tracking-wide">Lokalizacja</p>
                                <p className="text-foreground font-semibold mt-0.5">
                                    {event.location || "Online"}
                                </p>
                            </div>
                        </div>

                        <div className="bg-secondarybg p-4 rounded-2xl border border-secondarybg flex items-start gap-4 md:col-span-2 hover:border-secondary/30 transition-colors">
                            <div className="p-2.5 bg-inputbg rounded-xl text-secondary border border-secondary/10">
                                <Users className="w-6 h-6" />
                            </div>
                            <div>
                                <p className="text-xs text-txtcolor-300 font-bold uppercase tracking-wide">Uczestnicy</p>
                                <p className="text-foreground font-semibold mt-0.5">
                                    {event.participants?.length || 0} osób zapisało się na to wydarzenie
                                </p>
                            </div>
                        </div>
                    </div>

                    <div className="space-y-3">
                        <div className="flex items-center gap-2 text-secondary font-bold text-sm uppercase tracking-widest">
                            <AlignLeft className="w-4 h-4" />
                            Opis Wydarzenia
                        </div>
                        <div className="prose prose-invert max-w-none text-txtcolor-300 leading-relaxed bg-secondarybg p-5 rounded-2xl border border-secondarybg">
                            {event.description}
                        </div>
                    </div>
                </div>

                <div className="p-5 border-t border-secondarybg bg-inputbg/50 flex flex-col sm:flex-row items-center justify-between gap-4 shrink-0">

                    <button
                        onClick={onClose}
                        className="text-sm font-medium text-txtcolor-300 hover:text-foreground transition-colors px-4 py-2"
                    >
                        Anuluj / Zamknij
                    </button>
                    <div className="w-full sm:w-auto flex justify-end gap-3">
                        {actions}
                    </div>
                </div>
            </div>
        </div>
    );
}