'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { fetchDashboardSummary } from '@/lib/api/dashboard';
import { joinCouncilByCode } from '@/lib/api/council';
import { useAuth } from '@/lib/contexts/AuthContext';
import Link from 'next/link';
import {
    PiggyBank,
    Inbox,
    CalendarDays,
    ArrowRight,
    UserPlus,
    Loader2,
    Lightbulb,
    ShieldCheck,
    Megaphone
} from 'lucide-react';
import SchoolRounded from '@/components/icons/SchoolRounded';

export default function DashboardPage() {
    const { user } = useAuth();
    const queryClient = useQueryClient();
    const [joinCode, setJoinCode] = useState('');

    const { data: summary, isLoading, error } = useQuery({
        queryKey: ['dashboardSummary'],
        queryFn: fetchDashboardSummary,
    });

    const joinMutation = useMutation({
        mutationFn: joinCouncilByCode,
        onSuccess: () => {
            alert("Sukces! Dołączono do samorządu.");
            queryClient.invalidateQueries({ queryKey: ['dashboardSummary'] });
            setJoinCode('');
        },
        onError: (err) => alert("Błąd dołączania: " + (err instanceof Error ? err.message : "Nieznany błąd"))
    });

    const handleJoin = (e: React.FormEvent) => {
        e.preventDefault();
        if (joinCode.trim()) joinMutation.mutate(joinCode.trim());
    };

    if (isLoading) {
        return (
            <div className="flex justify-center items-center h-[60vh]">
                <Loader2 className="w-10 h-10 text-primary animate-spin" />
            </div>
        );
    }

    const firstName = user?.fullName?.split(' ')[0] || 'Uczniu';

    return (
        <div className="p-6 md:p-8 max-w-7xl mx-auto space-y-8 animate-in fade-in duration-500">

            <div className="bg-secondarybg/40 border border-border rounded-3xl p-8 flex flex-col md:flex-row justify-between items-center gap-6 relative overflow-hidden">
                <div className="absolute top-0 right-0 w-64 h-64 bg-primary/5 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2 pointer-events-none" />

                <div className="relative z-10">
                    <h1 className="text-3xl md:text-4xl font-black text-foreground mb-3">
                        Cześć, <span className="text-transparent bg-clip-text bg-gradient-to-r from-primary to-secondary">{firstName}</span>! 👋
                    </h1>
                    <p className="text-txtcolor-300 text-lg max-w-xl leading-relaxed">
                        Witaj w centrum dowodzenia Samorządu Uczniowskiego.
                        Sprawdź co dzieje się w szkole lub zarządzaj swoimi działaniami.
                    </p>
                </div>
                <div className="hidden md:flex p-5 bg-background rounded-2xl border border-border shadow-sm rotate-3 hover:rotate-0 transition-transform duration-300">
                    <SchoolRounded className="w-16 h-16 text-primary" />
                </div>
            </div>

            {summary?.councilMember && summary.activeCouncilId ? (
                <div className="space-y-5">
                    <div className="flex items-center gap-3 mb-2 px-1">
                        <div className="p-1.5 bg-success/10 rounded-lg">
                            <ShieldCheck className="w-5 h-5 text-success" />
                        </div>
                        <h2 className="text-xl font-bold uppercase tracking-wide text-foreground">
                            Panel Zarządu <span className="text-txtcolor-300 text-sm font-medium normal-case ml-2">({summary.activeCouncilName})</span>
                        </h2>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div className="bg-background border border-border rounded-2xl p-6 shadow-sm hover:border-success/40 hover:shadow-md transition-all group relative overflow-hidden flex flex-col justify-between h-48">
                            <div className="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity transform group-hover:scale-110 duration-500">
                                <PiggyBank className="w-24 h-24 text-success" />
                            </div>

                            <div>
                                <div className="flex items-center gap-2 text-txtcolor-300 text-xs font-bold uppercase tracking-wider mb-1">
                                    <PiggyBank className="w-4 h-4" /> Finanse
                                </div>
                                <p className={`text-4xl font-black mt-2 tracking-tight ${(summary.budgetBalance || 0) >= 0 ? 'text-foreground' : 'text-error'}`}>
                                    {summary.budgetBalance?.toFixed(2)} <span className="text-lg text-txtcolor-300 font-medium">PLN</span>
                                </p>
                            </div>

                            <Link
                                href={`/dashboard/council/${summary.activeCouncilId}/finances`}
                                className="inline-flex items-center gap-2 text-sm font-bold text-success hover:translate-x-1 transition-transform w-fit"
                            >
                                Przejdź do budżetu <ArrowRight className="w-4 h-4" />
                            </Link>
                        </div>
                        <div className="bg-background border border-border rounded-2xl p-6 shadow-sm hover:border-warning/40 hover:shadow-md transition-all group relative overflow-hidden flex flex-col justify-between h-48">
                            <div className="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity transform group-hover:scale-110 duration-500">
                                <Inbox className="w-24 h-24 text-warning" />
                            </div>

                            <div>
                                <div className="flex items-center gap-2 text-txtcolor-300 text-xs font-bold uppercase tracking-wider mb-1">
                                    <Inbox className="w-4 h-4" /> Sugestie
                                </div>
                                <div className="flex items-baseline gap-2 mt-2">
                                    <p className="text-4xl font-black text-foreground">
                                        {summary.pendingSuggestionsCount}
                                    </p>
                                    <span className="text-sm font-medium text-warning bg-warning/10 px-2 py-0.5 rounded-md">oczekujących</span>
                                </div>
                            </div>

                            <Link
                                href={`/dashboard/council/${summary.activeCouncilId}/suggestions`}
                                className="inline-flex items-center gap-2 text-sm font-bold text-warning hover:translate-x-1 transition-transform w-fit"
                            >
                                Podejmij decyzje <ArrowRight className="w-4 h-4" />
                            </Link>
                        </div>

                        <div className="bg-background border border-border rounded-2xl p-6 shadow-sm hover:border-info/40 hover:shadow-md transition-all group relative overflow-hidden flex flex-col justify-between h-48">
                            <div className="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity transform group-hover:scale-110 duration-500">
                                <CalendarDays className="w-24 h-24 text-info" />
                            </div>

                            <div>
                                <div className="flex items-center gap-2 text-txtcolor-300 text-xs font-bold uppercase tracking-wider mb-1">
                                    <CalendarDays className="w-4 h-4" /> Wydarzenia
                                </div>
                                <div className="flex items-baseline gap-2 mt-2">
                                    <p className="text-4xl font-black text-foreground">
                                        {summary.upcomingEventsCount}
                                    </p>
                                    <span className="text-sm font-medium text-info bg-info/10 px-2 py-0.5 rounded-md">nadchodzących</span>
                                </div>
                            </div>

                            <Link
                                href={`/dashboard/council/${summary.activeCouncilId}/events`}
                                className="inline-flex items-center gap-2 text-sm font-bold text-info hover:translate-x-1 transition-transform w-fit"
                            >
                                Zarządzaj kalendarzem <ArrowRight className="w-4 h-4" />
                            </Link>
                        </div>
                    </div>

                    <div className="flex justify-end pt-2">
                        <Link
                            href={`/dashboard/council/${summary.activeCouncilId}`}
                            className="bg-primary text-darkgray px-6 py-3 rounded-xl font-bold hover:bg-secondary transition-all shadow-lg shadow-primary/10 flex items-center gap-2 hover:scale-[1.02] active:scale-[0.98]"
                        >
                            Wejdź do Samorządu <ArrowRight className="w-5 h-5" />
                        </Link>
                    </div>
                </div>
            ) : (
                <div className="bg-secondarybg/30 border border-dashed border-border rounded-3xl p-10 text-center relative overflow-hidden">
                    <div className="relative z-10 max-w-lg mx-auto">
                        <div className="w-16 h-16 bg-background rounded-full flex items-center justify-center border border-border shadow-sm mx-auto mb-6">
                            <UserPlus className="w-8 h-8 text-secondary" />
                        </div>
                        <h2 className="text-2xl font-bold text-foreground">Nie jesteś członkiem Samorządu?</h2>
                        <p className="text-txtcolor-300 mt-3 leading-relaxed">
                            Jeśli zostałeś wybrany do rady samorządu, poproś przewodniczącego o <strong>kod dostępu</strong> i wpisz go poniżej, aby odblokować panel zarządzania.
                        </p>

                        <form onSubmit={handleJoin} className="mt-8 flex flex-col sm:flex-row gap-3">
                            <input
                                type="text"
                                placeholder="Wpisz kod (np. SU-2025)"
                                value={joinCode}
                                onChange={(e) => setJoinCode(e.target.value)}
                                className="flex-1 bg-background border border-border rounded-xl px-5 py-3 text-foreground focus:outline-none focus:ring-2 focus:ring-primary transition-all placeholder:text-txtcolor-300/50"
                            />
                            <button
                                type="submit"
                                disabled={joinMutation.isPending || !joinCode}
                                className="bg-secondary text-background font-bold px-6 py-3 rounded-xl hover:opacity-90 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-lg shadow-secondary/20 flex items-center justify-center gap-2"
                            >
                                {joinMutation.isPending ? <Loader2 className="w-5 h-5 animate-spin" /> : 'Dołącz'}
                            </button>
                        </form>
                    </div>
                </div>
            )}
            <div className="pt-4">
                <h2 className="text-lg font-bold uppercase tracking-wide text-txtcolor-300 mb-4 px-1 flex items-center gap-2">
                    <UserPlus className="w-5 h-5" /> Twoja Aktywność
                </h2>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="bg-background border border-border rounded-2xl p-6 flex items-start gap-5 shadow-sm hover:border-warning/30 transition-colors">
                        <div className="p-3 bg-warning/10 rounded-xl text-warning shrink-0">
                            <Lightbulb className="w-8 h-8" />
                        </div>
                        <div className="flex-1">
                            <h3 className="font-bold text-lg text-foreground">Twoje Pomysły</h3>
                            <p className="text-txtcolor-300 text-sm mt-1 mb-4 leading-relaxed">
                                Zgłosiłeś łącznie <strong className="text-foreground">{summary?.myTotalSuggestionsCount || 0}</strong> sugestii.
                                <br/>
                                Aktualnie <span className="text-warning font-bold">{summary?.myPendingSuggestionsCount || 0}</span> czeka na rozpatrzenie przez samorząd.
                            </p>
                        </div>
                    </div>

                    <div className="bg-background border border-border rounded-2xl p-6 flex items-start gap-5 shadow-sm hover:border-info/30 transition-colors">
                        <div className="p-3 bg-info/10 rounded-xl text-info shrink-0">
                            <Megaphone className="w-8 h-8" />
                        </div>
                        <div className="flex-1">
                            <h3 className="font-bold text-lg text-foreground">Wydarzenia Szkolne</h3>
                            <p className="text-txtcolor-300 text-sm mt-1 mb-4 leading-relaxed">
                                Sprawdź co dzieje się w szkole, zapisz się na nadchodzące akcje i bierz udział w życiu społeczności.
                            </p>
                            <Link href="/dashboard/events" className="text-sm font-bold text-info bg-info/5 px-4 py-2 rounded-lg hover:bg-info/10 transition-colors inline-flex items-center gap-2">
                                <CalendarDays className="w-4 h-4" /> Przeglądaj wydarzenia
                            </Link>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}