'use client';

import { useState } from 'react';
import { Users, Landmark, Inbox, Shield, Lock } from 'lucide-react';
import UsersManager from '@/components/admin/UsersManager';
import PermissionsMatrix from '@/components/admin/PermissionsMatrix';
import CouncilsManager from '@/components/admin/CouncilsManager';
import SuggestionsGlobalManager from '@/components/admin/SuggestionsGlobalManager';

type AdminTab = 'USERS' | 'PERMISSIONS' | 'COUNCILS' | 'SUGGESTIONS';

export default function AdminPage() {
    const [activeTab, setActiveTab] = useState<AdminTab>('USERS');

    return (
        <div className="animate-in fade-in mx-auto max-w-7xl space-y-8 p-6 duration-500 md:p-8">
            <div className="bg-secondarybg/40 border-border relative flex flex-col items-start justify-between gap-6 overflow-hidden rounded-3xl border p-8 md:flex-row md:items-center">
                <div className="bg-primary/5 pointer-events-none absolute top-0 right-0 h-64 w-64 translate-x-1/2 -translate-y-1/2 rounded-full blur-3xl" />

                <div className="relative z-10">
                    <div className="mb-2 flex items-center gap-3">
                        <div className="bg-primary/10 text-primary rounded-xl p-2">
                            <Shield className="h-8 w-8" />
                        </div>
                        <h1 className="text-foreground text-3xl font-black">
                            Panel Administratora
                        </h1>
                    </div>
                    <p className="text-txtcolor-300 max-w-2xl text-lg">
                        Zarządzaj użytkownikami, strukturą samorządów oraz globalnymi ustawieniami
                        systemu.
                    </p>
                </div>

                <div className="relative z-10 hidden flex-col items-end gap-2 md:flex">
                    <span className="bg-background border-border text-txtcolor-300 flex items-center gap-2 rounded-xl border px-4 py-2 text-xs font-bold">
                        <Lock className="h-3 w-3" /> Dostęp zastrzeżony
                    </span>
                </div>
            </div>

            <div className="border-border flex flex-col gap-4 overflow-x-auto border-b pb-1 sm:flex-row">
                <TabButton
                    active={activeTab === 'USERS'}
                    onClick={() => setActiveTab('USERS')}
                    icon={Users}
                    label="Użytkownicy"
                    desc="Role i konta"
                />
                <TabButton
                    active={activeTab === 'PERMISSIONS'}
                    onClick={() => setActiveTab('PERMISSIONS')}
                    icon={Shield}
                    label="Macierz Uprawnień"
                    desc="Globalne dostępy"
                />
                <TabButton
                    active={activeTab === 'COUNCILS'}
                    onClick={() => setActiveTab('COUNCILS')}
                    icon={Landmark}
                    label="Samorządy"
                    desc="Struktura szkoły"
                />
                <TabButton
                    active={activeTab === 'SUGGESTIONS'}
                    onClick={() => setActiveTab('SUGGESTIONS')}
                    icon={Inbox}
                    label="Sugestie"
                    desc="Moderacja globalna"
                />
            </div>

            <div className="min-h-[400px]">
                {activeTab === 'USERS' && (
                    <div className="animate-in slide-in-from-bottom-2 fade-in duration-300">
                        <UsersManager />
                    </div>
                )}

                {activeTab === 'PERMISSIONS' && (
                    <div className="animate-in slide-in-from-bottom-2 fade-in duration-300">
                        <PermissionsMatrix />
                    </div>
                )}

                {activeTab === 'COUNCILS' && (
                    <div className="animate-in slide-in-from-bottom-2 fade-in duration-300">
                        <CouncilsManager />
                    </div>
                )}

                {activeTab === 'SUGGESTIONS' && (
                    <div className="animate-in slide-in-from-bottom-2 fade-in duration-300">
                        <SuggestionsGlobalManager />
                    </div>
                )}
            </div>
        </div>
    );
}

const TabButton = ({ active, onClick, icon: Icon, label, desc }: any) => (
    <button
        onClick={onClick}
        className={`group flex w-full min-w-[200px] shrink-0 items-center gap-4 rounded-xl border p-4 text-left transition-all duration-200 sm:w-auto ${
            active
                ? 'bg-secondarybg border-primary/50 ring-primary/20 shadow-md ring-1'
                : 'hover:bg-secondarybg/50 hover:border-border text-txtcolor-300 hover:text-foreground border-transparent bg-transparent'
        }`}
    >
        <div
            className={`rounded-lg p-2 transition-colors ${active ? 'bg-primary text-darkgray' : 'bg-inputbg text-txtcolor-300 group-hover:text-foreground'}`}
        >
            <Icon className="h-5 w-5" />
        </div>
        <div>
            <div className={`text-sm font-bold ${active ? 'text-foreground' : ''}`}>{label}</div>
            <div className={`text-xs ${active ? 'text-txtcolor-300' : 'text-txtcolor-300/50'}`}>
                {desc}
            </div>
        </div>
    </button>
);
