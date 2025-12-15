'use client';

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
    Megaphone,
} from 'lucide-react';
import SchoolRounded from '@/components/icons/SchoolRounded';
import { useDashboard } from '@/hooks/dashboard/useDashboard';

export default function DashboardPage() {
    const {
        summary,
        isLoading,
        joinCode,
        setJoinCode,
        handleJoin,
        isJoining,
        firstName,
        isMember,
    } = useDashboard();

    if (isLoading) {
        return (
            <div className="flex h-[60vh] items-center justify-center">
                <Loader2 className="text-primary h-10 w-10 animate-spin" />
            </div>
        );
    }

    return (
        <div className="animate-in fade-in mx-auto max-w-7xl space-y-8 p-6 duration-500 md:p-8">
            <div className="bg-secondarybg/40 border-border relative flex flex-col items-center justify-between gap-6 overflow-hidden rounded-3xl border p-8 md:flex-row">
                <div className="bg-primary/5 pointer-events-none absolute top-0 right-0 h-64 w-64 translate-x-1/2 -translate-y-1/2 rounded-full blur-3xl" />
                <div className="relative z-10">
                    <h1 className="text-foreground mb-3 text-3xl font-black md:text-4xl">
                        Cześć,{' '}
                        <span className="from-primary to-secondary bg-gradient-to-r bg-clip-text text-transparent">
                            {firstName}
                        </span>
                        ! 👋
                    </h1>
                    <p className="text-txtcolor-300 max-w-xl text-lg leading-relaxed">
                        Witaj w centrum dowodzenia Samorządu Uczniowskiego.
                    </p>
                </div>
                <div className="bg-background border-border hidden rotate-3 rounded-2xl border p-5 shadow-sm transition-transform duration-300 hover:rotate-0 md:flex">
                    <SchoolRounded className="text-primary h-16 w-16" />
                </div>
            </div>

            {isMember ? (
                <div className="space-y-5">
                    <div className="mb-2 flex items-center gap-3 px-1">
                        <div className="bg-success/10 rounded-lg p-1.5">
                            <ShieldCheck className="text-success h-5 w-5" />
                        </div>
                        <h2 className="text-foreground text-xl font-bold tracking-wide uppercase">
                            Panel Zarządu{' '}
                            <span className="text-txtcolor-300 ml-2 text-sm font-medium normal-case">
                                ({summary?.activeCouncilName})
                            </span>
                        </h2>
                    </div>

                    <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
                        <StatCard
                            title="Finanse"
                            icon={PiggyBank}
                            value={`${summary?.budgetBalance?.toFixed(2)} PLN`}
                            valueColor={
                                (summary?.budgetBalance || 0) >= 0
                                    ? 'text-foreground'
                                    : 'text-error'
                            }
                            linkHref={`/dashboard/council/${summary?.activeCouncilId}/finances`}
                            linkText="Przejdź do budżetu"
                            theme="success"
                        />
                        <StatCard
                            title="Sugestie"
                            icon={Inbox}
                            value={summary?.pendingSuggestionsCount}
                            subValue="oczekujących"
                            linkHref={`/dashboard/council/${summary?.activeCouncilId}/suggestions`}
                            linkText="Podejmij decyzje"
                            theme="warning"
                        />
                        <StatCard
                            title="Wydarzenia"
                            icon={CalendarDays}
                            value={summary?.upcomingEventsCount}
                            subValue="oczekujących"
                            linkHref={`/dashboard/council/${summary?.activeCouncilId}/events`}
                            linkText="Zarządzaj kalendarzem"
                            theme="info"
                        />
                    </div>

                    <div className="flex justify-end pt-2">
                        <Link
                            href={`/dashboard/council/${summary?.activeCouncilId}`}
                            className="bg-primary text-darkgray hover:bg-secondary shadow-primary/10 flex items-center gap-2 rounded-xl px-6 py-3 font-bold shadow-lg transition-all hover:scale-[1.02]"
                        >
                            Wejdź do Samorządu <ArrowRight className="h-5 w-5" />
                        </Link>
                    </div>
                </div>
            ) : (
                <div className="bg-secondarybg/30 border-border relative overflow-hidden rounded-3xl border border-dashed p-10 text-center">
                    <div className="relative z-10 mx-auto max-w-lg">
                        <div className="bg-background border-border mx-auto mb-6 flex h-16 w-16 items-center justify-center rounded-full border shadow-sm">
                            <UserPlus className="text-secondary h-8 w-8" />
                        </div>
                        <h2 className="text-foreground text-2xl font-bold">
                            Nie jesteś członkiem Samorządu?
                        </h2>
                        <p className="text-txtcolor-300 mt-3 leading-relaxed">
                            Jeśli zostałeś wybrany, wpisz kod dostępu poniżej.
                        </p>

                        <form
                            onSubmit={handleJoin}
                            className="mt-8 flex flex-col gap-3 sm:flex-row"
                        >
                            <input
                                type="text"
                                placeholder="Wpisz kod (np. SU-2025)"
                                value={joinCode}
                                onChange={(e) => setJoinCode(e.target.value)}
                                className="bg-background border-border text-foreground focus:ring-primary flex-1 rounded-xl border px-5 py-3 focus:ring-2 focus:outline-none"
                            />
                            <button
                                type="submit"
                                disabled={isJoining || !joinCode}
                                className="bg-secondary text-background flex items-center justify-center gap-2 rounded-xl px-6 py-3 font-bold hover:opacity-90 disabled:opacity-50"
                            >
                                {isJoining ? (
                                    <Loader2 className="h-5 w-5 animate-spin" />
                                ) : (
                                    'Dołącz'
                                )}
                            </button>
                        </form>
                    </div>
                </div>
            )}

            <div className="pt-4">
                <h2 className="text-txtcolor-300 mb-4 flex items-center gap-2 px-1 text-lg font-bold tracking-wide uppercase">
                    <UserPlus className="h-5 w-5" /> Twoja Aktywność
                </h2>
                <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                    <ActivityCard icon={Lightbulb} title="Twoje Pomysły" theme="warning">
                        <p className="text-txtcolor-300 mt-1 mb-4 text-sm leading-relaxed">
                            Zgłosiłeś łącznie{' '}
                            <strong className="text-foreground">
                                {summary?.myTotalSuggestionsCount || 0}
                            </strong>{' '}
                            sugestii.
                            <br />
                            Aktualnie oczekujacych na rozpatrzenie:{' '}
                            <span className="text-warning font-bold">
                                {summary?.myPendingSuggestionsCount || 0}
                            </span>{' '}
                        </p>
                    </ActivityCard>

                    <ActivityCard icon={Megaphone} title="Wydarzenia Szkolne" theme="info">
                        <p className="text-txtcolor-300 mt-1 mb-4 text-sm leading-relaxed">
                            Sprawdź co dzieje się w szkole i bierz udział w życiu społeczności.
                        </p>
                        <Link
                            href="/dashboard/events"
                            className="text-info bg-info/5 hover:bg-info/10 inline-flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-bold transition-colors"
                        >
                            <CalendarDays className="h-4 w-4" /> Przeglądaj wydarzenia
                        </Link>
                    </ActivityCard>
                </div>
            </div>
        </div>
    );
}

const StatCard = ({
    title,
    icon: Icon,
    value,
    valueColor,
    subValue,
    linkHref,
    linkText,
    theme,
}: any) => {
    const themeClasses: any = {
        success: { text: 'text-success', border: 'hover:border-success/40' },
        warning: { text: 'text-warning', border: 'hover:border-warning/40', bg: 'bg-warning/10' },
        info: { text: 'text-info', border: 'hover:border-info/40', bg: 'bg-info/10' },
    };
    const style = themeClasses[theme];

    return (
        <div
            className={`bg-background border-border rounded-2xl border p-6 shadow-sm ${style.border} group relative flex h-48 flex-col justify-between overflow-hidden transition-all hover:shadow-md`}
        >
            <div className="absolute top-0 right-0 transform p-4 opacity-5 transition-opacity duration-500 group-hover:scale-110 group-hover:opacity-10">
                <Icon className={`h-24 w-24 ${style.text}`} />
            </div>
            <div>
                <div className="text-txtcolor-300 mb-1 flex items-center gap-2 text-xs font-bold tracking-wider uppercase">
                    <Icon className="h-4 w-4" /> {title}
                </div>
                <div className="mt-2 flex items-baseline gap-2">
                    <p className={`text-4xl font-black ${valueColor || 'text-foreground'}`}>
                        {value}
                    </p>
                    {subValue && (
                        <span
                            className={`text-sm font-medium ${style.text} ${style.bg} rounded-md px-2 py-0.5`}
                        >
                            {subValue}
                        </span>
                    )}
                </div>
            </div>
            <Link
                href={linkHref}
                className={`inline-flex items-center gap-2 text-sm font-bold ${style.text} w-fit transition-transform hover:translate-x-1`}
            >
                {linkText} <ArrowRight className="h-4 w-4" />
            </Link>
        </div>
    );
};

const ActivityCard = ({ icon: Icon, title, theme, children }: any) => {
    const colors: any = { warning: 'text-warning bg-warning/10', info: 'text-info bg-info/10' };
    const borderColors: any = { warning: 'hover:border-warning/30', info: 'hover:border-info/30' };

    return (
        <div
            className={`bg-background border-border flex items-start gap-5 rounded-2xl border p-6 shadow-sm ${borderColors[theme]} transition-colors`}
        >
            <div className={`shrink-0 rounded-xl p-3 ${colors[theme]}`}>
                <Icon className="h-8 w-8" />
            </div>
            <div className="flex-1">
                <h3 className="text-foreground text-lg font-bold">{title}</h3>
                {children}
            </div>
        </div>
    );
};
