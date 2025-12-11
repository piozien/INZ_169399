'use client';

import { useState } from 'react';
import { Pencil, UserPlus, Loader2, Plus, X } from 'lucide-react';
import { useAuth } from '@/lib/contexts/AuthContext';
import CouncilCard from '@/components/council/CouncilCard';
import CreateCouncilForm from '@/components/council/CreateCouncilForm';
import SchoolRounded from '@/components/icons/SchoolRounded';
import { useCouncilList } from '@/hooks/council/useCouncilList';

type Tab = 'create' | 'join' | null;

export default function CouncilPage() {
    const [activeTab, setActiveTab] = useState<Tab>(null);
    const [joinCode, setJoinCode] = useState('');
    const { user } = useAuth();

    const {
        activeCouncils,
        archiveCouncils,
        hasNoCouncils,
        isLoading,
        joinCouncil,
        isJoining,
        joinError,
    } = useCouncilList();

    const hasCreatePermission = user?.roles?.some((r) =>
        ['ADMINISTRATOR', 'DYREKTOR', 'ZASTEPCA_DYREKTORA'].includes(r)
    );

    const handleJoinSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        await joinCouncil(joinCode);
        if (!joinError) {
            setJoinCode('');
            setActiveTab(null);
        }
    };

    if (isLoading) {
        return (
            <div className="flex min-h-[50vh] items-center justify-center">
                <Loader2 className="text-primary h-8 w-8 animate-spin" />
            </div>
        );
    }

    if (hasNoCouncils) {
        const firstName = user?.fullName?.split(' ')[0] || '';

        return (
            <div className="text-foreground flex min-h-[80vh] flex-col items-center justify-center p-4">
                <div className="mb-12 flex flex-col items-center text-center">
                    <div className="mb-6 flex items-center gap-3">
                        <SchoolRounded className="text-secondary h-10 w-10" />
                        <h1 className="text-xl font-bold tracking-wide uppercase">Samorząd</h1>
                    </div>
                    <h2 className="mb-4 text-4xl font-bold">Witaj, {firstName}!</h2>
                    <p className="text-txtcolor-300 max-w-md">
                        Aby rozpocząć, musisz dołączyć do samorządu lub go utworzyć.
                    </p>
                </div>

                <div className="flex w-full max-w-4xl flex-col items-center">
                    <div className="mb-12 flex w-full max-w-2xl flex-col justify-center gap-6 md:flex-row">
                        {hasCreatePermission && (
                            <button
                                onClick={() =>
                                    setActiveTab(activeTab === 'create' ? null : 'create')
                                }
                                className={`border-border flex flex-1 flex-col items-center justify-center gap-4 rounded-xl border p-8 transition-all duration-300 ${activeTab === 'create' ? 'bg-secondary text-background scale-105 shadow-lg' : 'bg-secondarybg hover:bg-inputbg hover:border-secondary text-foreground'}`}
                            >
                                <Pencil
                                    className={`h-8 w-8 ${activeTab === 'create' ? 'text-background' : 'text-secondary'}`}
                                />
                                <span className="text-lg font-semibold">Stwórz nowy samorząd</span>
                            </button>
                        )}

                        <button
                            onClick={() => setActiveTab(activeTab === 'join' ? null : 'join')}
                            className={`border-border flex max-w-[400px] flex-1 flex-col items-center justify-center gap-4 rounded-xl border p-8 transition-all duration-300 ${activeTab === 'join' ? 'bg-secondary text-background scale-105 shadow-lg' : 'bg-secondarybg hover:bg-inputbg hover:border-secondary text-foreground'}`}
                        >
                            <UserPlus
                                className={`h-8 w-8 ${activeTab === 'join' ? 'text-background' : 'text-secondary'}`}
                            />
                            <span className="text-lg font-semibold">Dołącz kodem</span>
                        </button>
                    </div>

                    <div className="w-full max-w-lg">
                        {activeTab === 'create' && (
                            <div className="animate-in fade-in slide-in-from-top-4 bg-secondarybg border-border rounded-xl border p-6 duration-300">
                                <CreateCouncilForm
                                    onCancel={() => setActiveTab(null)}
                                    onSuccess={() => setActiveTab(null)}
                                />
                            </div>
                        )}

                        {activeTab === 'join' && (
                            <div className="animate-in fade-in slide-in-from-top-4 bg-secondarybg border-border rounded-xl border p-6 duration-300">
                                <h3 className="mb-4 text-center text-lg font-medium">
                                    Wpisz kod od przewodniczącego
                                </h3>
                                <form onSubmit={handleJoinSubmit} className="space-y-4">
                                    <input
                                        type="text"
                                        value={joinCode}
                                        onChange={(e) => setJoinCode(e.target.value)}
                                        className="bg-inputbg text-foreground focus:ring-secondary placeholder-txtcolor-300 border-border w-full rounded-lg border px-4 py-3 focus:ring-2 focus:outline-none"
                                        placeholder="Np. SU2024..."
                                        required
                                    />
                                    {joinError && (
                                        <p className="text-error text-center text-sm">
                                            {joinError}
                                        </p>
                                    )}
                                    <div className="flex gap-3">
                                        <button
                                            type="button"
                                            onClick={() => setActiveTab(null)}
                                            className="hover:bg-inputbg flex-1 rounded-lg py-2 transition-colors"
                                        >
                                            Anuluj
                                        </button>
                                        <button
                                            type="submit"
                                            disabled={isJoining}
                                            className="bg-primary text-darkgray hover:bg-secondary flex-1 rounded-lg py-2 font-bold disabled:opacity-50"
                                        >
                                            {isJoining ? 'Dołączanie...' : 'Dołącz'}
                                        </button>
                                    </div>
                                </form>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="container mx-auto px-4 py-8 pb-24">
            <div className="mb-8 flex flex-col items-start justify-between gap-4 md:flex-row md:items-center">
                <h1 className="text-foreground flex items-center gap-3 text-3xl font-bold">
                    <SchoolRounded className="text-secondary" /> Twoje Samorządy
                </h1>
                {hasCreatePermission && (
                    <button
                        onClick={() => setActiveTab('create')}
                        className="bg-primary text-darkgray hover:bg-secondary flex items-center gap-2 rounded-lg px-4 py-2 font-bold shadow-md transition-all hover:shadow-lg"
                    >
                        <Plus className="h-5 w-5" /> Stwórz Samorząd
                    </button>
                )}
            </div>

            {activeCouncils.length > 0 && (
                <div className="animate-in fade-in slide-in-from-bottom-4 mb-12 duration-500">
                    <h2 className="text-success mb-4 flex items-center gap-2 text-sm font-semibold tracking-wider uppercase">
                        <span className="bg-success inline-block h-2 w-2 rounded-full shadow-[0_0_8px_rgba(34,197,94,0.6)]"></span>{' '}
                        Aktywne Kadencje
                    </h2>
                    <div
                        className={`grid gap-6 ${activeCouncils.length === 1 ? 'max-w-4xl' : 'md:grid-cols-2 lg:grid-cols-2 xl:grid-cols-3'}`}
                    >
                        {activeCouncils.map((council) => (
                            <div key={council.id} className="group relative">
                                <CouncilCard council={council} isActive={true} />
                                {activeCouncils.length > 1 && (
                                    <span className="bg-primary text-darkgray border-background absolute -top-2 -right-2 z-10 rounded-full border-2 px-2 py-1 text-xs font-bold shadow-lg">
                                        {council.academicYear}
                                    </span>
                                )}
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {archiveCouncils.length > 0 && (
                <div className="animate-in fade-in slide-in-from-bottom-8 delay-100 duration-500">
                    <h2 className="text-txtcolor-300 border-border mb-4 flex items-center gap-2 border-t pt-8 text-sm font-semibold tracking-wider uppercase">
                        <span className="bg-txtcolor-300 inline-block h-2 w-2 rounded-full"></span>{' '}
                        Archiwum / Zakończone
                    </h2>
                    <div className="grid gap-6 opacity-80 transition-opacity hover:opacity-100 md:grid-cols-2 lg:grid-cols-3">
                        {archiveCouncils.map((council) => (
                            <CouncilCard key={council.id} council={council} isActive={false} />
                        ))}
                    </div>
                </div>
            )}

            {activeTab === 'create' && (
                <div className="bg-background/80 animate-in fade-in fixed inset-0 z-50 flex items-center justify-center p-4 backdrop-blur-sm duration-200">
                    <div className="bg-secondarybg border-border animate-in zoom-in-95 relative w-full max-w-lg overflow-hidden rounded-xl border p-6 shadow-2xl duration-200">
                        <button
                            onClick={() => setActiveTab(null)}
                            className="text-txtcolor-300 hover:text-foreground absolute top-4 right-4 transition-colors"
                        >
                            <X className="h-6 w-6" />
                        </button>
                        <h2 className="text-foreground mb-6 text-center text-xl font-bold">
                            Stwórz nową kadencję
                        </h2>
                        <CreateCouncilForm
                            onCancel={() => setActiveTab(null)}
                            onSuccess={() => setActiveTab(null)}
                        />
                    </div>
                </div>
            )}
        </div>
    );
}
