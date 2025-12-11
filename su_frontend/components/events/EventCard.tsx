'use client';

import { MapPin, Clock, ArrowRight } from 'lucide-react';
import { EventResponseDto } from '@/types/event.types';
import { format } from 'date-fns';
import { pl } from 'date-fns/locale';

interface Props {
    event: EventResponseDto;
    onClick: () => void;
    variant?: 'default' | 'admin';
}

export default function EventCard({ event, onClick, variant = 'default' }: Props) {
    const now = new Date();
    const startDate = new Date(event.startDate);
    const endDate = new Date(event.endDate);

    const isLive = startDate <= now && endDate >= now;
    const isPast = endDate < now;

    const renderStatusBadge = () => {
        const base =
            'px-3 py-1 rounded-full text-[10px] font-extrabold uppercase tracking-wider border backdrop-blur-md shadow-sm';

        if (event.status === 'PENDING') {
            return (
                <span className={`${base} bg-warning/10 text-warning border-warning/20`}>
                    Oczekuje
                </span>
            );
        }
        if (event.status === 'DRAFT') {
            return (
                <span
                    className={`${base} bg-txtcolor-300/10 text-txtcolor-300 border-txtcolor-300/20`}
                >
                    Szkic
                </span>
            );
        }
        if (event.status === 'REJECTED') {
            return (
                <span className={`${base} bg-error/10 text-error border-error/20`}>Odrzucone</span>
            );
        }
        if (event.status === 'CANCELLED') {
            return (
                <span className={`${base} bg-error/10 text-error border-error/20`}>Odwołane</span>
            );
        }

        if (isLive) {
            return (
                <span
                    className={`${base} bg-success/10 text-success border-success/20 animate-pulse`}
                >
                    Trwa Teraz
                </span>
            );
        }
        if (isPast) {
            return (
                <span className={`${base} border-secondarybg text-txtcolor-300`}>Zakończone</span>
            );
        }

        return (
            <span className={`${base} bg-secondary/10 text-secondary border-secondary/20`}>
                Nadchodzi
            </span>
        );
    };

    return (
        <div
            onClick={onClick}
            className="group bg-secondarybg border-secondarybg hover:border-secondary/50 hover:shadow-secondary/5 relative flex h-full cursor-pointer flex-col overflow-hidden rounded-2xl border transition-all duration-300 hover:-translate-y-1 hover:shadow-xl"
        >
            <div
                className={`h-1 w-full ${isPast ? 'bg-inputbg' : 'from-primary to-secondary bg-gradient-to-r'}`}
            />

            <div className="relative flex flex-1 flex-col p-5">
                <div className="mb-4 flex items-start justify-between">
                    <div className="flex flex-col">
                        <span className="text-foreground group-hover:text-secondary text-4xl font-black tracking-tighter transition-colors duration-300">
                            {format(startDate, 'd', { locale: pl })}
                        </span>
                        <span className="text-secondary text-xs font-bold tracking-widest uppercase">
                            {format(startDate, 'MMMM', { locale: pl })}
                        </span>
                    </div>
                    {renderStatusBadge()}
                </div>

                <h3 className="text-foreground group-hover:text-primary mb-6 line-clamp-2 text-lg leading-snug font-bold transition-colors">
                    {event.title}
                </h3>

                <div className="mt-auto space-y-3">
                    <div className="flex items-center gap-3">
                        <div className="bg-inputbg text-secondary group-hover:bg-secondary group-hover:text-darkgray shrink-0 rounded-lg p-2 transition-colors duration-300">
                            <Clock size={16} strokeWidth={2.5} />
                        </div>
                        <span className="text-txtcolor-300 group-hover:text-foreground text-sm font-medium transition-colors">
                            {format(startDate, 'HH:mm', { locale: pl })} -{' '}
                            {format(endDate, 'HH:mm', { locale: pl })}
                        </span>
                    </div>

                    <div className="flex items-center gap-3">
                        <div className="bg-inputbg text-secondary group-hover:bg-secondary group-hover:text-darkgray shrink-0 rounded-lg p-2 transition-colors duration-300">
                            <MapPin size={16} strokeWidth={2.5} />
                        </div>
                        <span className="text-txtcolor-300 group-hover:text-foreground truncate text-sm font-medium transition-colors">
                            {event.location || 'Online'}
                        </span>
                    </div>
                </div>

                <div className="text-secondary absolute right-5 bottom-5 translate-x-4 transform opacity-0 transition-all duration-300 group-hover:translate-x-0 group-hover:opacity-100">
                    <ArrowRight size={24} />
                </div>
            </div>
        </div>
    );
}
