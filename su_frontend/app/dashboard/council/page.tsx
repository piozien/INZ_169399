'use client';

import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { GraduationCap, Pencil, UserPlus, Loader2 } from 'lucide-react';
import { fetchUserCouncils, joinCouncilByCode } from '@/lib/api/council';
import { useAuth } from '@/lib/contexts/AuthContext';
import { CouncilResponseDto } from '@/types/council.types';
import CouncilCard from '@/components/council/CouncilCard';
import CreateCouncilForm from '@/components/council/CreateCouncilForm';
import { ApiError } from '@/types/error.types';

type Tab = 'create' | 'join' | null;

export default function CouncilPage() {
    const [activeTab, setActiveTab] = useState<Tab>(null);
    const [joinCode, setJoinCode] = useState('');
    const [joinError, setJoinError] = useState<string | null>(null);

    const queryClient = useQueryClient();
    const { user } = useAuth();

    const {
        data: councils,
        isLoading: councilsLoading,
        error: councilsError,
    } = useQuery<CouncilResponseDto[]>({
        queryKey: ['userCouncils'],
        queryFn: fetchUserCouncils,
        retry: false,
    });

    const hasCreatePermission =
        user?.roles?.includes('ADMINISTRATOR') ||
        user?.roles?.includes('DYREKTOR') ||
        user?.roles?.includes('ZASTEPCA_DYREKTORA');

    const joinMutation = useMutation({
        mutationFn: joinCouncilByCode,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['userCouncils'] });
            setJoinCode('');
            setJoinError(null);
            setActiveTab(null);
        },
        onError: (err) => {
            if (err instanceof ApiError) {
                setJoinError(err.message);
            } else {
                setJoinError('Wystąpił błąd podczas dołączania.');
            }
        },
    });

    const handleJoinSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setJoinError(null);
        if (joinCode.trim()) {
            joinMutation.mutate(joinCode.trim());
        }
    };

    if (councilsLoading) {
        return (
            <div className="flex justify-center items-center min-h-[50vh]">
                <Loader2 className="h-8 w-8 animate-spin text-primary" />
            </div>
        );
    }

    const hasNoCouncils = !councils || councils.length === 0;

    if (hasNoCouncils) {
        const firstName = user?.fullName?.split(' ')[0] || '';

        return (
            <div className="min-h-[80vh] flex flex-col items-center justify-center p-4 text-foreground">
                <div className="flex flex-col items-center text-center mb-12">
                    <div className="flex items-center gap-3 mb-6">
                        <GraduationCap className="text-secondary h-10 w-10" />
                        <h1 className="text-xl font-bold uppercase tracking-wide">
                            Samorząd
                        </h1>
                    </div>

                    <h2 className="text-4xl font-bold mb-4">Witaj, {firstName}!</h2>
                    <p className="text-txtcolor-300 max-w-md">
                        Aby rozpocząć, musisz dołączyć do samorządu lub go utworzyć.
                    </p>
                </div>

                <div className="w-full max-w-4xl flex flex-col items-center">
                    <div className="flex flex-col md:flex-row gap-6 justify-center w-full max-w-2xl mb-12">
                        {hasCreatePermission && (
                            <button
                                onClick={() =>
                                    setActiveTab(activeTab === 'create' ? null : 'create')
                                }
                                className={`
                  flex-1 p-8 rounded-xl flex flex-col items-center justify-center gap-4 transition-all duration-300 border border-border
                  ${
                                    activeTab === 'create'
                                        ? 'bg-secondary text-background shadow-lg scale-105'
                                        : 'bg-secondarybg hover:bg-inputbg hover:border-secondary text-foreground'
                                }
                `}
                            >
                                <Pencil className={`h-8 w-8 ${activeTab === 'create' ? 'text-background' : 'text-secondary'}`} />
                                <span className="text-lg font-semibold">Stwórz nowy samorząd</span>
                            </button>
                        )}

                        <button
                            onClick={() => setActiveTab(activeTab === 'join' ? null : 'join')}
                            className={`
                 max-w-[400px] flex-1 p-8 rounded-xl flex flex-col items-center justify-center gap-4 transition-all duration-300 border border-border
                ${
                                activeTab === 'join'
                                    ? 'bg-secondary text-background shadow-lg scale-105'
                                    : 'bg-secondarybg hover:bg-inputbg hover:border-secondary text-foreground'
                            }
              `}
                        >
                            <UserPlus className={`h-8 w-8 ${activeTab === 'join' ? 'text-background' : 'text-secondary'}`} />
                            <span className="text-lg font-semibold">Dołącz kodem</span>
                        </button>
                    </div>
                    <div className="w-full max-w-lg">
                        {activeTab === 'create' && (
                            <div className="animate-in fade-in slide-in-from-top-4 duration-300 bg-secondarybg p-6 rounded-xl border border-border">
                                <CreateCouncilForm
                                    onCancel={() => setActiveTab(null)}
                                    onSuccess={() => {
                                        setActiveTab(null);
                                        queryClient.invalidateQueries({ queryKey: ['userCouncils'] });
                                    }}
                                />
                            </div>
                        )}

                        {activeTab === 'join' && (
                            <div className="animate-in fade-in slide-in-from-top-4 duration-300 bg-secondarybg p-6 rounded-xl border border-border">
                                <h3 className="text-lg font-medium mb-4 text-center">Wpisz kod od przewodniczącego</h3>
                                <form onSubmit={handleJoinSubmit} className="space-y-4">
                                    <input
                                        type="text"
                                        value={joinCode}
                                        onChange={(e) => setJoinCode(e.target.value)}
                                        className="w-full bg-inputbg text-foreground rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-secondary placeholder-txtcolor-300 border border-border"
                                        placeholder="Np. SU2024..."
                                        required
                                    />
                                    {joinError && (
                                        <p className="text-error text-sm text-center">{joinError}</p>
                                    )}
                                    <div className="flex gap-3">
                                        <button
                                            type="button"
                                            onClick={() => setActiveTab(null)}
                                            className="flex-1 py-2 rounded-lg hover:bg-inputbg transition-colors"
                                        >
                                            Anuluj
                                        </button>
                                        <button
                                            type="submit"
                                            disabled={joinMutation.isPending}
                                            className="flex-1 bg-primary text-darkgray font-bold py-2 rounded-lg hover:bg-secondary disabled:opacity-50"
                                        >
                                            {joinMutation.isPending ? 'Dołączanie...' : 'Dołącz'}
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

    const activeCouncils = councils?.filter((c) => c.isActive) || [];

    const archiveCouncils = councils
        ?.filter((c) => !c.isActive)
        .sort((a, b) => new Date(b.endDate).getTime() - new Date(a.endDate).getTime()) || [];

    return (
        <div className="container mx-auto px-4 py-8 pb-24">
            <h1 className="text-3xl font-bold mb-8 text-foreground flex items-center gap-3">
                <GraduationCap className="text-secondary" />
                Twoje Samorządy
            </h1>

            {activeCouncils.length > 0 && (
                <div className="mb-12 animate-in fade-in slide-in-from-bottom-4 duration-500">
                    <h2 className="text-sm font-semibold text-success uppercase mb-4 tracking-wider flex items-center gap-2">
                        <span className="w-2 h-2 rounded-full bg-success inline-block shadow-[0_0_8px_rgba(34,197,94,0.6)]"></span>
                        Aktywne Kadencje
                    </h2>

                    <div className={`grid gap-6 ${activeCouncils.length === 1 ? 'max-w-4xl' : 'md:grid-cols-2 lg:grid-cols-2 xl:grid-cols-3'}`}>
                        {activeCouncils.map((council) => (
                            <div key={council.id} className="relative group">
                                <CouncilCard council={council} isActive={true} />

                                {activeCouncils.length > 1 && (
                                    <span className="absolute -top-2 -right-2 bg-primary text-darkgray text-xs font-bold px-2 py-1 rounded-full shadow-lg border-2 border-background z-10">
                           {council.academicYear}
                       </span>
                                )}
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {archiveCouncils.length > 0 && (
                <div className="animate-in fade-in slide-in-from-bottom-8 duration-500 delay-100">
                    <h2 className="text-sm font-semibold text-txtcolor-300 uppercase mb-4 tracking-wider flex items-center gap-2 border-t border-border pt-8">
                        <span className="w-2 h-2 rounded-full bg-txtcolor-300 inline-block"></span>
                        Archiwum / Zakończone
                    </h2>
                    <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3 opacity-80 hover:opacity-100 transition-opacity">
                        {archiveCouncils.map((council) => (
                            <CouncilCard
                                key={council.id}
                                council={council}
                                isActive={false}
                            />
                        ))}
                    </div>
                </div>
            )}
        </div>
    );
}