'use client';

import { use, useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchCouncilById, leaveCouncil } from '@/lib/api/council';
import { CouncilResponseDto } from '@/types/council.types';
import {
    Loader2,
    CalendarDays,
    Users,
    Hash,
    Check,
    Copy,
    ArrowRight,
    PiggyBank,
    PartyPopper,
    Settings,
    Lightbulb,
    LogOut
} from 'lucide-react';
import SchoolRounded from '@/components/icons/SchoolRounded';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/contexts/AuthContext';
import EditCouncilModal from '@/components/council/EditCouncilModal';

export default function CouncilDetailPage({
                                              params,
                                          }: {
    params: Promise<{ id: string }>;
}) {
    const { id } = use(params);
    const router = useRouter();
    const { user } = useAuth();
    const queryClient = useQueryClient();

    const [copied, setCopied] = useState(false);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);

    const {
        data: council,
        isLoading,
        error,
    } = useQuery<CouncilResponseDto>({
        queryKey: ['council', id],
        queryFn: () => fetchCouncilById(id),
        retry: 1,
    });

    const leaveMutation = useMutation({
        mutationFn: () => {
            if (!user?.id) throw new Error("Brak użytkownika");
            return leaveCouncil(id, user.id);
        },
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['userCouncils'] });
            router.push('/dashboard');
        },
        onError: (err) => {
            alert("Nie udało się opuścić samorządu. " + (err instanceof Error ? err.message : ""));
        }
    });

    const handleLeave = () => {
        if (confirm(`Czy na pewno chcesz opuścić samorząd "${council?.name}"? Ta operacja jest nieodwracalna.`)) {
            leaveMutation.mutate();
        }
    };

    const hasPermission = (perm: string) => {
        if (!council?.myPermissions) return false;
        return council.myPermissions.includes('ALL_ACCESS') ||
            council.myPermissions.includes(perm);
    };

    const copyToClipboard = () => {
        if (council?.joinCode) {
            navigator.clipboard.writeText(council.joinCode);
            setCopied(true);
            setTimeout(() => setCopied(false), 2000);
        }
    };

    if (isLoading) {
        return (
            <div className="flex justify-center items-center h-[50vh]">
                <Loader2 className="h-8 w-8 animate-spin text-primary"/>
            </div>
        );
    }

    if (error || !council) {
        return (
            <div className="flex flex-col items-center justify-center h-[50vh] text-txtcolor-300">
                <h2 className="text-2xl font-bold mb-2">Nie znaleziono samorządu</h2>
                <p>Sprawdź, czy masz odpowiednie uprawnienia!</p>
            </div>
        );
    }

    return (
        <div className="p-6 space-y-8 max-w-7xl mx-auto">

            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4 border-b border-border pb-6">
                <div className="flex items-center gap-4">
                    <div className="p-4 bg-secondarybg rounded-2xl border border-border">
                        <SchoolRounded className="h-10 w-10 text-secondary"/>
                    </div>
                    <div>
                        <div className="flex items-center gap-3">
                            <h1 className="text-3xl font-bold text-foreground">{council.name}</h1>
                            <span
                                className={`px-3 py-1 rounded-full text-xs font-medium border ${
                                    council.active
                                        ? 'bg-success/10 text-success border-success/20'
                                        : 'bg-error/10 text-error border-error/20'
                                }`}
                            >
                                {council.active ? 'Aktywny' : 'Archiwalny'}
                            </span>
                        </div>
                        <p className="text-txtcolor-300 mt-1 flex items-center gap-2">
                            <CalendarDays className="h-4 w-4"/>
                            Rok szkolny: <span className="text-foreground font-medium">{council.academicYear}</span>
                        </p>
                    </div>
                </div>

                <div className="flex flex-wrap gap-2">
                    <button
                        onClick={handleLeave}
                        disabled={leaveMutation.isPending}
                        className="flex items-center gap-2 px-4 py-2 bg-error/10 text-error border border-error/20 hover:bg-error hover:text-white rounded-lg text-sm font-medium transition-colors disabled:opacity-50"
                        title="Opuść samorząd"
                    >
                        {leaveMutation.isPending ? <Loader2 className="h-4 w-4 animate-spin"/> : <LogOut className="h-4 w-4"/>}
                        <span className="hidden sm:inline">Opuść</span>
                    </button>

                    {hasPermission('COUNCIL_EDIT') && (
                        <button
                            onClick={() => setIsEditModalOpen(true)}
                            className="flex items-center gap-2 px-4 py-2 bg-secondarybg border border-border hover:border-secondary rounded-lg text-sm font-medium transition-colors text-foreground"
                        >
                            <Settings className="h-4 w-4"/>
                            <span className="hidden sm:inline">Ustawienia</span>
                        </button>
                    )}
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">

                {hasPermission('COUNCIL_JOIN') && (
                    <div
                        className="bg-secondarybg p-6 rounded-xl border border-border flex flex-col justify-between group hover:border-secondary/50 transition-colors">
                        <div>
                            <div className="flex items-center gap-2 text-txtcolor-300 mb-2">
                                <Hash className="h-5 w-5 text-secondary"/>
                                <span className="text-sm font-medium uppercase tracking-wider">Kod dołączenia</span>
                            </div>
                            <p className="text-sm text-txtcolor-300 mb-4">
                                Podaj ten kod uczniom, aby mogli dołączyć do samorządu.
                            </p>
                        </div>
                        <button
                            onClick={copyToClipboard}
                            className="flex items-center justify-between w-full bg-inputbg p-3 rounded-lg border border-border hover:border-secondary group-hover:bg-background transition-all"
                        >
                            <code className="text-xl font-mono font-bold text-primary tracking-widest">
                                {council.joinCode}
                            </code>
                            {copied ? (
                                <Check className="h-5 w-5 text-success"/>
                            ) : (
                                <Copy className="h-5 w-5 text-txtcolor-300 group-hover:text-secondary"/>
                            )}
                        </button>
                    </div>
                )}

                <div className="bg-secondarybg p-6 rounded-xl border border-border flex flex-col justify-between">
                    <div>
                        <div className="flex items-center gap-2 text-txtcolor-300 mb-2">
                            <Users className="h-5 w-5 text-info"/>
                            <span className="text-sm font-medium uppercase tracking-wider">Członkowie</span>
                        </div>
                        <div className="mt-4">
                            <span className="text-4xl font-bold text-foreground">
                                {council.members?.length || 0}
                            </span>
                            <span className="text-txtcolor-300 ml-2">osób</span>
                        </div>
                    </div>
                    <Link
                        href={`/dashboard/council/${id}/members`}
                        className="mt-4 flex items-center text-sm text-secondary font-medium hover:underline"
                    >
                        Zarządzaj członkami <ArrowRight className="h-4 w-4 ml-1"/>
                    </Link>
                </div>

                <div className="bg-secondarybg p-6 rounded-xl border border-border flex flex-col justify-between">
                    <div>
                        <div className="flex items-center gap-2 text-txtcolor-300 mb-2">
                            <CalendarDays className="h-5 w-5 text-warning"/>
                            <span className="text-sm font-medium uppercase tracking-wider">Kadencja</span>
                        </div>
                        <div className="space-y-3 mt-4">
                            <div className="flex justify-between items-center">
                                <span className="text-txtcolor-300 text-sm">Start:</span>
                                <span
                                    className="font-medium">{new Date(council.startDate).toLocaleDateString('pl-PL')}</span>
                            </div>
                            <div className="flex justify-between items-center">
                                <span className="text-txtcolor-300 text-sm">Koniec:</span>
                                <span
                                    className="font-medium">{new Date(council.endDate).toLocaleDateString('pl-PL')}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div>
                <h2 className="text-xl font-semibold mb-4 text-foreground">Szybkie akcje</h2>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">

                    {hasPermission('EVENT_VIEW') && (
                        <Link href={`/dashboard/council/${id}/events`} className="group">
                            <div
                                className="p-6 bg-secondarybg rounded-xl border border-border hover:border-secondary hover:bg-secondary/5 transition-all flex items-center gap-4 h-full">
                                <div
                                    className="p-3 bg-accent/10 rounded-lg text-accent group-hover:scale-110 transition-transform">
                                    <PartyPopper className="h-6 w-6"/>
                                </div>
                                <div>
                                    <h3 className="font-bold text-lg text-foreground">Wydarzenia</h3>
                                    <p className="text-sm text-txtcolor-300">Planuj apele, dyskoteki i zbiórki.</p>
                                </div>
                            </div>
                        </Link>
                    )}

                    {hasPermission('SUGGESTION_VIEW') && (
                        <Link href={`/dashboard/council/${id}/suggestions`} className="group">
                            <div
                                className="p-6 bg-secondarybg rounded-xl border border-border hover:border-secondary hover:bg-secondary/5 transition-all flex items-center gap-4 h-full">
                                <div
                                    className="p-3 bg-warning/10 rounded-lg text-warning group-hover:scale-110 transition-transform">
                                    <Lightbulb className="h-6 w-6"/>
                                </div>
                                <div>
                                    <h3 className="font-bold text-lg text-foreground">Sugestie</h3>
                                    <p className="text-sm text-txtcolor-300">Przeglądaj pomysły uczniów.</p>
                                </div>
                            </div>
                        </Link>
                    )}

                    {!hasPermission('SUGGESTION_VIEW') && (
                        <div
                            className="p-6 bg-secondarybg/50 rounded-xl border border-border flex items-center gap-4 opacity-50 cursor-not-allowed h-full">
                            <div className="p-3 bg-darkgray rounded-lg text-darkgray">
                                <Lightbulb className="h-6 w-6"/>
                            </div>
                            <div>
                                <h3 className="font-bold text-lg text-foreground">Sugestie (Brak dostępu)</h3>
                                <p className="text-sm text-txtcolor-300">Dostęp tylko dla uprawnionych.</p>
                            </div>
                        </div>
                    )}

                    {hasPermission('COUNCIL_BUDGET_VIEW') && (
                        <Link href={`/dashboard/council/${id}/finances`} className="group">
                            <div
                                className="p-6 bg-secondarybg rounded-xl border border-border hover:border-secondary hover:bg-secondary/5 transition-all flex items-center gap-4 h-full">
                                <div
                                    className="p-3 bg-success/10 rounded-lg text-success group-hover:scale-110 transition-transform">
                                    <PiggyBank className="h-6 w-6"/>
                                </div>
                                <div>
                                    <h3 className="font-bold text-lg text-foreground">Budżet i Finanse</h3>
                                    <p className="text-sm text-txtcolor-300">Zarządzaj wydatkami.</p>
                                </div>
                            </div>
                        </Link>
                    )}

                    {!hasPermission('COUNCIL_BUDGET_VIEW') && (
                        <div
                            className="p-6 bg-secondarybg/50 rounded-xl border border-border flex items-center gap-4 opacity-50 cursor-not-allowed h-full">
                            <div className="p-3 bg-darkgray rounded-lg text-darkgray">
                                <PiggyBank className="h-6 w-6"/>
                            </div>
                            <div>
                                <h3 className="font-bold text-lg text-foreground">Finanse (Brak dostępu)</h3>
                                <p className="text-sm text-txtcolor-300">Dostęp tylko dla Skarbnika i Zarządu.</p>
                            </div>
                        </div>
                    )}

                </div>
            </div>

            {council && (
                <EditCouncilModal
                    isOpen={isEditModalOpen}
                    onClose={() => setIsEditModalOpen(false)}
                    council={council}
                />
            )}
        </div>
    );
}