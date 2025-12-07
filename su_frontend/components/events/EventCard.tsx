"use client";

import {MapPin, Clock, ArrowRight} from 'lucide-react';
import {EventResponseDto} from '@/types/event.types';
import {format} from 'date-fns';
import {pl} from 'date-fns/locale';

interface Props {
    event: EventResponseDto;
    onClick: () => void;
    variant?: 'default' | 'admin';
}

export default function EventCard({event, onClick, variant = 'default'}: Props) {
    const now = new Date();
    const startDate = new Date(event.startDate);
    const endDate = new Date(event.endDate);

    const isLive = startDate <= now && endDate >= now;
    const isPast = endDate < now;

    const renderStatusBadge = () => {
        const base = "px-3 py-1 rounded-full text-[10px] font-extrabold uppercase tracking-wider border backdrop-blur-md shadow-sm";

        if (event.status === 'PENDING') {
            return <span className={`${base} bg-warning/10 text-warning border-warning/20`}>Oczekuje</span>;
        }
        if (event.status === 'DRAFT') {
            return <span className={`${base} bg-txtcolor-300/10 text-txtcolor-300 border-txtcolor-300/20`}>Szkic</span>;
        }
        if (event.status === 'REJECTED') {
            return <span className={`${base} bg-error/10 text-error border-error/20`}>Odrzucone</span>;
        }
        if (event.status === 'CANCELLED') {
            return <span className={`${base} bg-error/10 text-error border-error/20`}>Odwołane</span>;
        }

        if (isLive) {
            return (
                <span className={`${base} bg-success/10 text-success border-success/20 animate-pulse`}>
                    Trwa Teraz
                </span>
            );
        }
        if (isPast) {
            return <span className={`${base} border-secondarybg text-txtcolor-300`}>Zakończone</span>;
        }

        return <span className={`${base} bg-secondary/10 text-secondary border-secondary/20`}>Nadchodzi</span>;
    };

    return (
        <div
            onClick={onClick}
            className="group relative flex flex-col h-full bg-secondarybg rounded-2xl border border-secondarybg overflow-hidden cursor-pointer transition-all duration-300 hover:border-secondary/50 hover:shadow-xl hover:shadow-secondary/5 hover:-translate-y-1"
        >
            <div className={`h-1 w-full ${isPast ? 'bg-inputbg' : 'bg-gradient-to-r from-primary to-secondary'}`}/>

            <div className="p-5 flex flex-col flex-1 relative">

                <div className="flex justify-between items-start mb-4">
                    <div className="flex flex-col">
                        <span
                            className="text-4xl font-black text-foreground tracking-tighter group-hover:text-secondary transition-colors duration-300">
                            {format(startDate, 'd', {locale: pl})}
                        </span>
                        <span className="text-xs font-bold text-secondary uppercase tracking-widest">
                            {format(startDate, 'MMMM', {locale: pl})}
                        </span>
                    </div>
                    {renderStatusBadge()}
                </div>

                <h3 className="text-lg font-bold text-foreground leading-snug mb-6 line-clamp-2 group-hover:text-primary transition-colors">
                    {event.title}
                </h3>

                <div className="mt-auto space-y-3">

                    <div className="flex items-center gap-3">
                        <div
                            className="p-2 rounded-lg bg-inputbg text-secondary shrink-0 group-hover:bg-secondary group-hover:text-darkgray transition-colors duration-300">
                            <Clock size={16} strokeWidth={2.5}/>
                        </div>
                        <span
                            className="text-sm font-medium text-txtcolor-300 group-hover:text-foreground transition-colors">
                            {format(startDate, 'HH:mm', {locale: pl})} - {format(endDate, 'HH:mm', {locale: pl})}
                        </span>
                    </div>

                    <div className="flex items-center gap-3">
                        <div
                            className="p-2 rounded-lg bg-inputbg text-secondary shrink-0 group-hover:bg-secondary group-hover:text-darkgray transition-colors duration-300">
                            <MapPin size={16} strokeWidth={2.5}/>
                        </div>
                        <span
                            className="text-sm font-medium text-txtcolor-300 truncate group-hover:text-foreground transition-colors">
                            {event.location || "Online"}
                        </span>
                    </div>
                </div>

                <div
                    className="absolute bottom-5 right-5 opacity-0 transform translate-x-4 group-hover:opacity-100 group-hover:translate-x-0 transition-all duration-300 text-secondary">
                    <ArrowRight size={24}/>
                </div>
            </div>
        </div>
    );
}