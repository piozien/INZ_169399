'use client';

import {use} from 'react';
import Link from 'next/link';
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
    LogOut,
    Trash2
} from 'lucide-react';
import SchoolRounded from '@/components/icons/SchoolRounded';
import EditCouncilModal from '@/components/council/EditCouncilModal';
import DeleteCouncilModal from '@/components/council/DeleteCouncilModal';
import {useCouncilDetails} from '@/hooks/council/useCouncilDetails';

export default function CouncilDetailPage({params}: { params: Promise<{ id: string }> }) {
    const {id} = use(params);

    const {
        council,
        isLoading,
        error,
        handleLeave,
        isLeaving,
        hasPermission,
        copyJoinCode,
        isCopied,
        isEditModalOpen,
        openEditModal,
        closeEditModal,
        removeCouncil,
        isDeleting,
        isDeleteModalOpen,
        openDeleteModal,
        closeDeleteModal
    } = useCouncilDetails(id);

    if (isLoading) {
        return (
            <div className="flex h-[50vh] items-center justify-center">
                <Loader2 className="text-primary h-8 w-8 animate-spin"/>
            </div>
        );
    }

    if (error || !council) {
        return (
            <div className="text-txtcolor-300 flex h-[50vh] flex-col items-center justify-center">
                <h2 className="mb-2 text-2xl font-bold">Nie znaleziono samorządu</h2>
                <p>Sprawdź, czy masz odpowiednie uprawnienia!</p>
            </div>
        );
    }

    return (
        <div className="mx-auto max-w-7xl space-y-8 p-6">
            <div
                className="border-border flex flex-col items-start justify-between gap-4 border-b pb-6 md:flex-row md:items-center">
                <div className="flex items-center gap-4">
                    <div className="bg-secondarybg border-border rounded-2xl border p-4">
                        <SchoolRounded className="text-secondary h-10 w-10"/>
                    </div>
                    <div>
                        <div className="flex items-center gap-3">
                            <h1 className="text-foreground text-3xl font-bold">{council.name}</h1>
                            <span
                                className={`rounded-full border px-3 py-1 text-xs font-medium ${council.active ? 'bg-success/10 text-success border-success/20' : 'bg-error/10 text-error border-error/20'}`}
                            >
                                {council.active ? 'Aktywny' : 'Archiwalny'}
                            </span>
                        </div>
                        <p className="text-txtcolor-300 mt-1 flex items-center gap-2">
                            <CalendarDays className="h-4 w-4"/>
                            Rok szkolny:{' '}
                            <span className="text-foreground font-medium">
                                {council.academicYear}
                            </span>
                        </p>
                    </div>
                </div>

                <div className="flex flex-wrap gap-2">
                    <button
                        onClick={handleLeave}
                        disabled={isLeaving}
                        className="bg-error/10 text-error border-error/20 hover:bg-error flex items-center gap-2 rounded-lg border px-4 py-2 text-sm font-medium transition-colors hover:text-white disabled:opacity-50"
                        title="Opuść samorząd"
                    >
                        {isLeaving ? (
                            <Loader2 className="h-4 w-4 animate-spin"/>
                        ) : (
                            <LogOut className="h-4 w-4"/>
                        )}
                        <span className="hidden sm:inline">Opuść</span>
                    </button>

                    {hasPermission('COUNCIL_DELETE') && (
                        <button
                            onClick={openDeleteModal}
                            className="bg-error/10 text-error border-error/20 hover:bg-error flex items-center gap-2 rounded-lg border px-4 py-2 text-sm font-medium transition-colors hover:text-foreground"
                        >
                            <Trash2 className="h-4 w-4"/>
                            <span className="hidden sm:inline">Usuń</span>
                        </button>
                    )}
                    {hasPermission('COUNCIL_EDIT') && (
                        <button
                            onClick={openEditModal}
                            className="bg-secondarybg border-border hover:border-secondary text-foreground flex items-center gap-2 rounded-lg border px-4 py-2 text-sm font-medium transition-colors"
                        >
                            <Settings className="h-4 w-4"/>
                            <span className="hidden sm:inline">Ustawienia</span>
                        </button>
                    )}
                </div>
            </div>

            <div className="grid grid-cols-1 gap-6 md:grid-cols-3">
                {hasPermission('COUNCIL_JOIN') && (
                    <div
                        className="bg-secondarybg border-border group hover:border-secondary/50 flex flex-col justify-between rounded-xl border p-6 transition-colors">
                        <div>
                            <div className="text-txtcolor-300 mb-2 flex items-center gap-2">
                                <Hash className="text-secondary h-5 w-5"/>
                                <span className="text-sm font-medium tracking-wider uppercase">
                                    Kod dołączenia
                                </span>
                            </div>
                            <p className="text-txtcolor-300 mb-4 text-sm">
                                Podaj ten kod uczniom, aby mogli dołączyć do samorządu.
                            </p>
                        </div>
                        <button
                            onClick={copyJoinCode}
                            className="bg-inputbg border-border hover:border-secondary group-hover:bg-background flex w-full items-center justify-between rounded-lg border p-3 transition-all"
                        >
                            <code className="text-primary font-mono text-xl font-bold tracking-widest">
                                {council.joinCode}
                            </code>
                            {isCopied ? (
                                <Check className="text-success h-5 w-5"/>
                            ) : (
                                <Copy className="text-txtcolor-300 group-hover:text-secondary h-5 w-5"/>
                            )}
                        </button>
                    </div>
                )}

                <div className="bg-secondarybg border-border flex flex-col justify-between rounded-xl border p-6">
                    <div>
                        <div className="text-txtcolor-300 mb-2 flex items-center gap-2">
                            <Users className="text-info h-5 w-5"/>
                            <span className="text-sm font-medium tracking-wider uppercase">
                                Członkowie
                            </span>
                        </div>
                        <div className="mt-4">
                            <span className="text-foreground text-4xl font-bold">
                                {council.members?.length || 0}
                            </span>
                            <span className="text-txtcolor-300 ml-2">osób</span>
                        </div>
                    </div>
                    <Link
                        href={`/dashboard/council/${id}/members`}
                        className="text-secondary mt-4 flex items-center text-sm font-medium hover:underline"
                    >
                        Zarządzaj członkami <ArrowRight className="ml-1 h-4 w-4"/>
                    </Link>
                </div>

                <div className="bg-secondarybg border-border flex flex-col justify-between rounded-xl border p-6">
                    <div>
                        <div className="text-txtcolor-300 mb-2 flex items-center gap-2">
                            <CalendarDays className="text-warning h-5 w-5"/>
                            <span className="text-sm font-medium tracking-wider uppercase">
                                Kadencja
                            </span>
                        </div>
                        <div className="mt-4 space-y-3">
                            <div className="flex items-center justify-between">
                                <span className="text-txtcolor-300 text-sm">Start:</span>
                                <span className="font-medium">
                                    {new Date(council.startDate).toLocaleDateString('pl-PL')}
                                </span>
                            </div>
                            <div className="flex items-center justify-between">
                                <span className="text-txtcolor-300 text-sm">Koniec:</span>
                                <span className="font-medium">
                                    {new Date(council.endDate).toLocaleDateString('pl-PL')}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div>
                <h2 className="text-foreground mb-4 text-xl font-semibold">Szybkie akcje</h2>
                <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
                    {hasPermission('EVENT_VIEW') && (
                        <ActionCard
                            href={`/dashboard/council/${id}/events`}
                            icon={PartyPopper}
                            title="Wydarzenia"
                            desc="Planuj apele, dyskoteki i zbiórki."
                            color="accent"
                        />
                    )}

                    {hasPermission('SUGGESTION_VIEW') ? (
                        <ActionCard
                            href={`/dashboard/council/${id}/suggestions`}
                            icon={Lightbulb}
                            title="Sugestie"
                            desc="Przeglądaj pomysły uczniów."
                            color="warning"
                        />
                    ) : (
                        <DisabledCard
                            icon={Lightbulb}
                            title="Sugestie (Brak dostępu)"
                            desc="Dostęp tylko dla uprawnionych."
                        />
                    )}

                    {hasPermission('COUNCIL_BUDGET_VIEW') ? (
                        <ActionCard
                            href={`/dashboard/council/${id}/finances`}
                            icon={PiggyBank}
                            title="Budżet i Finanse"
                            desc="Zarządzaj wydatkami."
                            color="success"
                        />
                    ) : (
                        <DisabledCard
                            icon={PiggyBank}
                            title="Finanse (Brak dostępu)"
                            desc="Dostęp tylko dla Skarbnika i Zarządu."
                        />
                    )}
                </div>
            </div>

            {council && (
                <>
                    <EditCouncilModal
                        isOpen={isEditModalOpen}
                        onClose={closeEditModal}
                        council={council}
                    />

                    <DeleteCouncilModal
                        isOpen={isDeleteModalOpen}
                        onClose={closeDeleteModal}
                        onConfirm={removeCouncil}
                        isDeleting={isDeleting}
                        councilName={council.name}
                    />
                </>
            )}
        </div>
    );
}

const ActionCard = ({href, icon: Icon, title, desc, color}: any) => {
    const colors: any = {
        accent: 'bg-accent/10 text-accent',
        warning: 'bg-warning/10 text-warning',
        success: 'bg-success/10 text-success',
    };
    return (
        <Link href={href} className="group">
            <div
                className="bg-secondarybg border-border hover:border-secondary hover:bg-secondary/5 flex h-full items-center gap-4 rounded-xl border p-6 transition-all">
                <div
                    className={`rounded-lg p-3 transition-transform group-hover:scale-110 ${colors[color]}`}
                >
                    <Icon className="h-6 w-6"/>
                </div>
                <div>
                    <h3 className="text-foreground text-lg font-bold">{title}</h3>
                    <p className="text-txtcolor-300 text-sm">{desc}</p>
                </div>
            </div>
        </Link>
    );
};

const DisabledCard = ({icon: Icon, title, desc}: any) => (
    <div
        className="bg-secondarybg/50 border-border flex h-full cursor-not-allowed items-center gap-4 rounded-xl border p-6 opacity-50">
        <div className="bg-darkgray text-darkgray rounded-lg p-3">
            <Icon className="h-6 w-6"/>
        </div>
        <div>
            <h3 className="text-foreground text-lg font-bold">{title}</h3>
            <p className="text-txtcolor-300 text-sm">{desc}</p>
        </div>
    </div>
);
