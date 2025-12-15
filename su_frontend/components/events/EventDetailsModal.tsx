'use client';

import {
    X,
    MapPin,
    Users,
    Clock,
    AlignLeft,
    AlertCircle,
    CheckCircle2,
    FileText,
    Ban,
    Trash2,
} from 'lucide-react';
import {EventResponseDto} from '@/types/event.types';
import {format} from 'date-fns';
import {pl} from 'date-fns/locale';
import React from 'react';
import {useAuth} from '@/lib/contexts/AuthContext';
import {toast} from 'sonner';

interface Props {
    event: EventResponseDto;
    onClose: () => void;
    actions?: React.ReactNode;
    onRemoveParticipant?: (eventId: string, userId: string) => void;
}

const getParticipantsLabel = (count: number) => {
    if (count === 1) return '1 osoba zapisała się';
    const units = count % 10;
    const teens = count % 100;
    if (units >= 2 && units <= 4 && (teens < 10 || teens >= 20)) {
        return `${count} osoby zapisały się`;
    }
    return `${count} osób zapisało się`;
};

const StatusBadge = ({status}: { status: string }) => {
    switch (status) {
        case 'APPROVED':
            return <Badge icon={CheckCircle2} label="Zatwierdzone" color="success"/>;
        case 'PENDING':
            return <Badge icon={AlertCircle} label="Oczekuje" color="warning"/>;
        case 'REJECTED':
            return <Badge icon={Ban} label="Odrzucone" color="error"/>;
        case 'DRAFT':
        default:
            return <Badge icon={FileText} label="Szkic" color="txtcolor-300"/>;
    }
};

const Badge = ({icon: Icon, label, color}: any) => {
    const colorClasses: any = {
        success: 'bg-success/20 text-success border-success/30',
        warning: 'bg-warning/20 text-warning border-warning/30',
        error: 'bg-error/20 text-error border-error/30',
        'txtcolor-300': 'bg-txtcolor-300/20 text-txtcolor-300 border-txtcolor-300/30',
    };
    return (
        <span
            className={`inline-flex items-center gap-1.5 rounded-full border px-3 py-1 text-xs font-bold tracking-wider uppercase ${colorClasses[color]}`}
        >
            <Icon className="h-3.5 w-3.5"/> {label}
        </span>
    );
};

export default function EventDetailsModal({
                                              event,
                                              onClose,
                                              actions,
                                              onRemoveParticipant,
                                          }: Props) {
    const {user} = useAuth();

    const handleBackdropClick = (e: React.MouseEvent) => {
        if (e.target === e.currentTarget) onClose();
    };

    const startDate = new Date(event.startDate);
    const endDate = new Date(event.endDate);

    const canRemoveParticipant = (targetUserId: string) => {
        if (!user) return false;

        if (event.createdById === user.id) return true;

        if (user.id === targetUserId) return true;

        return (
            user.permissions?.includes('EVENT_REMOVE_PARTICIPANT') ||
            user.roles?.includes('ADMINISTRATOR')
        );
    };

    const handleRemoveClick = (userId: string, userName: string) => {
        if (!onRemoveParticipant) return;

        const isSelf = userId === user?.id;

        const message = isSelf
            ? 'Czy na pewno chcesz zrezygnować z udziału?'
            : `Czy na pewno usunąć uczestnika ${userName || 'Bez nazwy'}?`;

        const actionLabel = isSelf ? 'Rezygnuję' : 'Usuń';

        toast(message, {
            description: isSelf
                ? 'Stracisz swoje miejsce na liście.'
                : 'Użytkownik zostanie wypisany z wydarzenia.',
            action: {
                label: actionLabel,
                onClick: () => onRemoveParticipant(event.id, userId),
            },
            cancel: {
                label: 'Anuluj',
                onClick: () => {
                },
            },
            duration: 5000,
        });
    };

    return (
        <div
            className="bg-background/80 animate-in fade-in fixed inset-0 z-50 flex items-center justify-center p-4 backdrop-blur-md duration-200"
            onClick={handleBackdropClick}
        >
            <div
                className="bg-background border-secondarybg animate-in zoom-in-95 relative flex max-h-[90vh] w-full max-w-2xl flex-col overflow-hidden rounded-3xl border shadow-2xl duration-200">
                <div className="relative shrink-0">
                    <div className="from-secondary/10 via-background to-background absolute inset-0 bg-gradient-to-br"/>
                    <div className="relative p-6 pt-8 pr-12">
                        <div className="mb-3 flex flex-wrap items-center gap-2">
                            <span
                                className="bg-secondary/10 text-secondary border-secondary/20 inline-block rounded-full border px-3 py-1 text-xs font-bold tracking-wider uppercase">
                                {format(startDate, 'd MMMM yyyy', {locale: pl})}
                            </span>
                            <StatusBadge status={event.status}/>
                        </div>
                        <h2 className="text-foreground text-3xl leading-tight font-black break-words drop-shadow-lg md:text-4xl">
                            {event.title}
                        </h2>
                    </div>
                    <button
                        onClick={onClose}
                        className="bg-inputbg text-txtcolor-300 hover:text-foreground hover:bg-secondarybg hover:border-secondary/20 absolute top-4 right-4 z-10 rounded-full border border-transparent p-2 transition-all"
                    >
                        <X className="h-5 w-5"/>
                    </button>
                </div>

                <div className="custom-scrollbar flex-1 space-y-8 overflow-y-auto p-6">
                    <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
                        <div
                            className="bg-secondarybg border-secondarybg hover:border-secondary/30 flex items-start gap-4 rounded-2xl border p-4 transition-colors">
                            <div
                                className="bg-inputbg text-secondary border-secondary/10 shrink-0 rounded-xl border p-2.5">
                                <Clock className="h-6 w-6"/>
                            </div>
                            <div className="w-full">
                                <p className="text-txtcolor-300 mb-2 text-xs font-bold tracking-wide uppercase">
                                    Czas Trwania
                                </p>
                                <div className="flex flex-col gap-1">
                                    <div className="flex items-center gap-2">
                                        <div className="h-2 w-2 rounded-full bg-success ring-4 ring-success/10"/>
                                        <span className="text-foreground font-semibold text-sm">
                                            {format(startDate, 'dd.MM.yyyy', {locale: pl})}
                                            <span className="text-txtcolor-300 ml-1">
                                                godz.
                                            </span>{' '}
                                            {format(startDate, 'HH:mm')}
                                        </span>
                                    </div>

                                    <div
                                        className="ml-[3.5px] h-3 border-l-2 border-dashed border-secondary/20 my-0.5"/>

                                    <div className="flex items-center gap-2">
                                        <div className="h-2 w-2 rounded-full bg-warning ring-4 ring-warning/10"/>
                                        <span className="text-foreground font-semibold text-sm">
                                            {format(endDate, 'dd.MM.yyyy', {locale: pl})}
                                            <span className="text-txtcolor-300 ml-1">
                                                godz.
                                            </span>{' '}
                                            {format(endDate, 'HH:mm')}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <InfoBox icon={MapPin} label="Lokalizacja">
                            <span className="break-words">{event.location || 'Online'}</span>
                        </InfoBox>

                        <div className="md:col-span-2">
                            <div
                                className="bg-secondarybg border-secondarybg hover:border-secondary/30 flex h-full flex-col gap-4 rounded-2xl border p-4 transition-colors">
                                <div className="flex items-center gap-4">
                                    <div
                                        className="bg-inputbg text-secondary border-secondary/10 shrink-0 rounded-xl border p-2.5">
                                        <Users className="h-6 w-6"/>
                                    </div>
                                    <div className="w-full min-w-0">
                                        <p className="text-txtcolor-300 mb-0.5 text-xs font-bold tracking-wide uppercase">
                                            Uczestnicy
                                        </p>
                                        <div className="flex items-center gap-2">
                                            <span className="font-bold text-foreground text-lg">
                                                {event.participantsCount}
                                                {event.maxParticipants && (
                                                    <span className="text-txtcolor-300 text-sm">
                                                        {' '}
                                                        / {event.maxParticipants}
                                                    </span>
                                                )}
                                            </span>
                                            <span className="text-txtcolor-300 text-sm">
                                                (Zapisanych osób)
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                {event.participants && event.participants.length > 0 && user && (
                                    <div className="mt-2 space-y-2 border-t border-border pt-4">
                                        <p className="text-xs font-bold uppercase text-txtcolor-300">
                                            Lista zapisanych:
                                        </p>
                                        <div className="max-h-48 overflow-y-auto pr-2 space-y-2 custom-scrollbar">
                                            {event.participants.map((p) => (
                                                <div
                                                    key={p.userId}
                                                    className="flex items-center justify-between rounded-lg bg-inputbg/50 border border-transparent hover:border-border px-3 py-2 transition-colors"
                                                >
                                                    <div className="flex flex-col min-w-0">
                                                        <span className="font-bold text-foreground text-sm truncate">
                                                            {p.userFullName || 'Użytkownik'}{' '}
                                                            {p.userId === user.id && '(Ty)'}
                                                        </span>
                                                        <span className="text-xs text-txtcolor-300 truncate">
                                                            {p.userEmail}
                                                        </span>
                                                    </div>

                                                    {canRemoveParticipant(p.userId) &&
                                                        onRemoveParticipant && (
                                                            <button
                                                                onClick={() =>
                                                                    handleRemoveClick(
                                                                        p.userId,
                                                                        p.userFullName || ''
                                                                    )
                                                                }
                                                                className="text-txtcolor-300 hover:text-error hover:bg-error/10 ml-2 rounded-lg p-2 transition-colors"
                                                                title={
                                                                    p.userId === user.id
                                                                        ? 'Zrezygnuj z udziału'
                                                                        : 'Usuń z wydarzenia'
                                                                }
                                                            >
                                                                <Trash2 className="h-4 w-4"/>
                                                            </button>
                                                        )}
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>

                    <div className="space-y-3">
                        <div
                            className="text-secondary flex items-center gap-2 text-sm font-bold tracking-widest uppercase">
                            <AlignLeft className="h-4 w-4"/> Opis Wydarzenia
                        </div>
                        <div
                            className="prose prose-invert text-foreground bg-secondarybg border-secondarybg max-w-none rounded-2xl border p-5 leading-relaxed">
                            {event.description}
                        </div>
                    </div>
                </div>

                <div
                    className="border-secondarybg bg-inputbg/50 flex shrink-0 flex-col items-center justify-between gap-4 border-t p-5 sm:flex-row">
                    <button
                        onClick={onClose}
                        className="text-txtcolor-300 hover:text-foreground px-4 py-2 text-sm font-medium transition-colors"
                    >
                        Anuluj / Zamknij
                    </button>
                    <div className="flex w-full flex-wrap justify-end gap-3 sm:w-auto">
                        {actions}
                    </div>
                </div>
            </div>
        </div>
    );
}

const InfoBox = ({icon: Icon, label, children}: any) => (
    <div
        className="bg-secondarybg border-secondarybg hover:border-secondary/30 flex h-full items-start gap-4 rounded-2xl border p-4 transition-colors">
        <div className="bg-inputbg text-secondary border-secondary/10 shrink-0 rounded-xl border p-2.5">
            <Icon className="h-6 w-6"/>
        </div>
        <div className="w-full min-w-0">
            <p className="text-txtcolor-300 mb-0.5 text-xs font-bold tracking-wide uppercase">
                {label}
            </p>
            <div className="text-foreground font-semibold leading-snug">{children}</div>
        </div>
    </div>
);