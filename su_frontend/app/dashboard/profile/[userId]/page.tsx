'use client';

import { useState, use } from 'react';
import { useMutation } from '@tanstack/react-query';
import { changePassword } from '@/lib/api/user';
import {
    User,
    Lock,
    Users,
    ChevronDown,
    CalendarDays,
    Lightbulb,
    LogOut,
    Loader2,
    Settings,
    Shield,
    GraduationCap,
    BadgeCheck,
    X,
    Eye,
    EyeOff,
    Check,
} from 'lucide-react';
import { format, parseISO } from 'date-fns';
import { pl } from 'date-fns/locale';
import { useProfile } from '@/hooks/profile/useProfile';

export default function ProfilePage({ params }: { params: Promise<{ userId: string }> }) {
    const { userId } = use(params);

    const {
        user,
        isLoading,
        error,
        isOwnProfile,
        logout,
        activeCouncils,
        oldCouncils,
        currentEvents,
        archivedEvents,
        activeTab,
        setActiveTab,
        isPasswordModalOpen,
        openPasswordModal,
        closePasswordModal,
    } = useProfile(userId);

    if (isLoading)
        return (
            <div className="flex h-[60vh] items-center justify-center">
                <Loader2 className="text-primary h-10 w-10 animate-spin" />
            </div>
        );

    if (error || !user)
        return (
            <div className="text-error p-8 text-center font-bold">
                Nie udało się załadować profilu. Sprawdź czy użytkownik istnieje.
            </div>
        );

    return (
        <div className="animate-in fade-in mx-auto max-w-7xl space-y-8 p-6 duration-500 md:p-8">
            <div className="bg-secondarybg/40 border-border relative flex flex-col items-start justify-between gap-6 overflow-hidden rounded-3xl border p-8 md:flex-row md:items-center">
                <div className="bg-primary/5 pointer-events-none absolute top-0 right-0 h-64 w-64 translate-x-1/2 -translate-y-1/2 rounded-full blur-3xl" />
                <div className="relative z-10">
                    <h1 className="text-foreground mb-3 text-3xl font-black md:text-4xl">
                        {isOwnProfile ? 'Twój Profil' : `Profil Użytkownika`}
                    </h1>
                    <p className="text-txtcolor-300 text-lg">
                        {isOwnProfile
                            ? 'Zarządzaj swoim kontem i sprawdzaj aktywność.'
                            : 'Podgląd informacji o użytkowniku i jego historii.'}
                    </p>
                </div>
                <div className="bg-background border-border relative z-10 hidden rotate-3 rounded-2xl border p-5 shadow-sm transition-transform hover:rotate-0 md:flex">
                    <User className="text-primary h-16 w-16" />
                </div>
            </div>

            <div className="grid grid-cols-1 gap-8 lg:grid-cols-4">
                <div className="space-y-4 lg:col-span-1">
                    <div className="bg-background border-border sticky top-6 rounded-2xl border p-3 shadow-sm">
                        <p className="text-txtcolor-300 px-4 py-3 text-xs font-bold tracking-widest uppercase">
                            Menu
                        </p>
                        <NavButton
                            active={activeTab === 'profile'}
                            onClick={() => setActiveTab('profile')}
                            icon={User}
                            label="Dane Podstawowe"
                        />
                        <NavButton
                            active={activeTab === 'activity'}
                            onClick={() => setActiveTab('activity')}
                            icon={CalendarDays}
                            label="Historia i Aktywność"
                        />
                        {isOwnProfile && (
                            <>
                                <NavButton
                                    active={activeTab === 'settings'}
                                    onClick={() => setActiveTab('settings')}
                                    icon={Settings}
                                    label="Bezpieczeństwo"
                                />
                                <div className="bg-border mx-2 my-2 h-px" />
                                <button
                                    onClick={logout}
                                    className="text-error hover:bg-error/10 flex w-full items-center gap-3 rounded-xl px-4 py-3 text-left font-bold transition-colors"
                                >
                                    <LogOut className="h-5 w-5" /> Wyloguj
                                </button>
                            </>
                        )}
                    </div>
                </div>

                <div className="lg:col-span-3">
                    {activeTab === 'profile' && (
                        <ProfileTab
                            user={user}
                            activeCouncils={activeCouncils}
                            oldCouncils={oldCouncils}
                        />
                    )}
                    {activeTab === 'activity' && (
                        <ActivityTab
                            user={user}
                            currentEvents={currentEvents}
                            archivedEvents={archivedEvents}
                        />
                    )}
                    {activeTab === 'settings' && isOwnProfile && (
                        <SettingsTab onOpenPasswordModal={openPasswordModal} />
                    )}
                </div>
            </div>

            {isOwnProfile && (
                <PasswordModal isOpen={isPasswordModalOpen} onClose={closePasswordModal} />
            )}
        </div>
    );
}

const NavButton = ({ active, onClick, icon: Icon, label }: any) => (
    <button
        onClick={onClick}
        className={`mb-1 flex w-full items-center gap-3 rounded-xl px-4 py-3 text-left font-bold transition-all ${active ? 'bg-primary text-darkgray shadow-primary/20 shadow-lg' : 'text-txtcolor-300 hover:text-foreground hover:bg-secondarybg'}`}
    >
        <Icon className="h-5 w-5" /> {label}
    </button>
);

const ProfileTab = ({ user, activeCouncils, oldCouncils }: any) => {
    const [isOldExpanded, setIsOldExpanded] = useState(false);

    return (
        <div className="animate-in fade-in slide-in-from-bottom-4 space-y-6 duration-500">
            <h2 className="text-foreground flex items-center gap-2 text-xl font-bold tracking-wide uppercase">
                <User className="text-primary h-5 w-5" /> Dane Osobowe
            </h2>

            <div className="grid grid-cols-1 gap-6 md:grid-cols-2">
                <InfoCard label="Imię i Nazwisko" value={user.fullName} />
                <InfoCard label="Adres E-mail" value={user.email} />
            </div>

            <div className="bg-background border-border rounded-2xl border p-5 shadow-sm">
                <p className="text-txtcolor-300 mb-2 text-xs font-bold tracking-wider uppercase">
                    Role Systemowe
                </p>
                <div className="flex flex-wrap gap-2">
                    {user.globalRoles.map((role: string) => (
                        <GlobalRoleBadge key={role} role={role} />
                    ))}
                </div>
            </div>

            <h2 className="text-foreground flex items-center gap-2 pt-4 text-xl font-bold tracking-wide uppercase">
                <Users className="text-info h-5 w-5" /> Samorządy
            </h2>

            <div className="bg-background border-border rounded-2xl border p-6 shadow-sm">
                <h3 className="text-txtcolor-300 mb-4 text-sm font-bold tracking-wider uppercase">
                    Aktywne
                </h3>
                <div className="space-y-3">
                    {activeCouncils.length > 0 ? (
                        activeCouncils.map((c: any) => (
                            <CouncilRow key={c.councilId} council={c} isActive={true} />
                        ))
                    ) : (
                        <p className="text-txtcolor-300 text-sm italic">
                            Brak aktywnych członkostw.
                        </p>
                    )}
                </div>

                {oldCouncils.length > 0 && (
                    <div className="border-border mt-6 border-t pt-4">
                        <button
                            onClick={() => setIsOldExpanded(!isOldExpanded)}
                            className="text-txtcolor-300 hover:text-foreground flex w-full items-center gap-2 text-sm font-bold tracking-wider uppercase"
                        >
                            Archiwum ({oldCouncils.length}){' '}
                            <ChevronDown
                                className={`h-4 w-4 transition-transform ${isOldExpanded ? 'rotate-180' : ''}`}
                            />
                        </button>
                        {isOldExpanded && (
                            <div className="animate-in slide-in-from-top-2 mt-4 space-y-3">
                                {oldCouncils.map((c: any) => (
                                    <CouncilRow key={c.councilId} council={c} isActive={false} />
                                ))}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

const ActivityTab = ({ user, currentEvents, archivedEvents }: any) => {
    const [isArchiveExpanded, setIsArchiveExpanded] = useState(false);

    return (
        <div className="animate-in fade-in slide-in-from-bottom-4 space-y-6 duration-500">
            <div className="bg-background border-border rounded-2xl border p-6 shadow-sm">
                <div className="mb-4 flex items-center gap-3">
                    <div className="bg-warning/10 rounded-lg p-2">
                        <Lightbulb className="text-warning h-5 w-5" />
                    </div>
                    <h3 className="text-foreground text-lg font-bold">Sugestie Użytkownika</h3>
                </div>
                <div className="grid grid-cols-3 gap-4 text-center">
                    <StatBox
                        value={user.totalSuggestionsCount}
                        label="Łącznie"
                        color="text-foreground"
                        bg="bg-secondarybg/50"
                    />
                    <StatBox
                        value={user.pendingSuggestionsCount}
                        label="Oczekujące"
                        color="text-warning"
                        bg="bg-warning/5"
                    />
                    <StatBox
                        value={user.approvedSuggestionsCount}
                        label="Przyjęte"
                        color="text-success"
                        bg="bg-success/5"
                    />
                </div>
            </div>

            <div className="bg-background border-border rounded-2xl border p-6 shadow-sm">
                <div className="mb-4 flex items-center gap-3">
                    <div className="bg-info/10 rounded-lg p-2">
                        <CalendarDays className="text-info h-5 w-5" />
                    </div>
                    <h3 className="text-foreground text-lg font-bold">Wydarzenia</h3>
                </div>

                <div className="space-y-2">
                    <h4 className="text-info mb-3 text-xs font-bold tracking-wider uppercase">
                        Aktualne i Nadchodzące
                    </h4>
                    {currentEvents.length > 0 ? (
                        currentEvents.map((e: any) => (
                            <EventRow key={e.eventId} event={e} type="current" />
                        ))
                    ) : (
                        <p className="text-txtcolor-300 p-2 text-xs italic">
                            Brak nadchodzących wydarzeń.
                        </p>
                    )}
                </div>

                {archivedEvents.length > 0 && (
                    <div className="border-border mt-6 border-t pt-4">
                        <button
                            onClick={() => setIsArchiveExpanded(!isArchiveExpanded)}
                            className="text-txtcolor-300 hover:text-foreground flex w-full items-center gap-2 text-xs font-bold tracking-wider uppercase"
                        >
                            Historia ({archivedEvents.length}){' '}
                            <ChevronDown
                                className={`h-4 w-4 transition-transform ${isArchiveExpanded ? 'rotate-180' : ''}`}
                            />
                        </button>
                        {isArchiveExpanded && (
                            <div className="animate-in slide-in-from-top-2 mt-3 space-y-2">
                                {archivedEvents.map((e: any) => (
                                    <EventRow key={e.eventId} event={e} type="archive" />
                                ))}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    );
};

const SettingsTab = ({ onOpenPasswordModal }: any) => (
    <div className="animate-in fade-in slide-in-from-bottom-4 space-y-6 duration-500">
        <h2 className="text-foreground flex items-center gap-2 text-xl font-bold tracking-wide uppercase">
            <Shield className="text-primary h-5 w-5" /> Bezpieczeństwo
        </h2>
        <div className="bg-background border-border hover:border-primary/30 flex items-center justify-between rounded-2xl border p-6 shadow-sm transition-colors">
            <div className="flex items-center gap-4">
                <div className="bg-secondarybg text-foreground rounded-xl p-3">
                    <Lock className="h-6 w-6" />
                </div>
                <div>
                    <h3 className="text-foreground font-bold">Hasło</h3>
                    <p className="text-txtcolor-300 text-sm">Zmień hasło do swojego konta.</p>
                </div>
            </div>
            <button
                onClick={onOpenPasswordModal}
                className="bg-secondarybg border-border text-foreground hover:bg-border rounded-xl border px-5 py-2.5 text-sm font-bold transition-colors"
            >
                Zmień
            </button>
        </div>
    </div>
);

const PasswordModal = ({ isOpen, onClose }: any) => {
    const [form, setForm] = useState({ oldPassword: '', newPassword: '', confirmPassword: '' });
    const [showPassword, setShowPassword] = useState(false);

    const changePasswordMutation = useMutation({
        mutationFn: changePassword,
        onSuccess: () => {
            alert('Hasło zmienione!');
            onClose();
            setForm({ oldPassword: '', newPassword: '', confirmPassword: '' });
        },
        onError: (err) => alert('Błąd: ' + (err instanceof Error ? err.message : 'Nieznany błąd')),
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (form.newPassword !== form.confirmPassword) return alert('Hasła nie są identyczne.');
        if (form.newPassword.length < 6) return alert('Minimum 6 znaków.');
        changePasswordMutation.mutate({
            oldPassword: form.oldPassword,
            newPassword: form.newPassword,
        });
    };

    if (!isOpen) return null;

    return (
        <div className="bg-background/50 animate-in fade-in fixed inset-0 z-50 flex items-center justify-center p-4 backdrop-blur-sm duration-200">
            <div className="bg-background border-border animate-in zoom-in-95 w-full max-w-md rounded-2xl border p-6 shadow-2xl duration-200">
                <div className="mb-6 flex items-center justify-between">
                    <h3 className="text-foreground text-xl font-bold">Zmiana Hasła</h3>
                    <button onClick={onClose}>
                        <X className="text-txtcolor-300 hover:text-foreground h-6 w-6" />
                    </button>
                </div>
                <form onSubmit={handleSubmit} className="space-y-4">
                    {['oldPassword', 'newPassword', 'confirmPassword'].map((field) => (
                        <div key={field}>
                            <label className="text-txtcolor-300 mb-1 block text-sm font-bold">
                                {field === 'oldPassword'
                                    ? 'Stare hasło'
                                    : field === 'newPassword'
                                      ? 'Nowe hasło'
                                      : 'Powtórz hasło'}
                            </label>
                            <div className="relative">
                                <input
                                    type={showPassword ? 'text' : 'password'}
                                    value={(form as any)[field]}
                                    onChange={(e) => setForm({ ...form, [field]: e.target.value })}
                                    className="bg-inputbg border-border text-foreground focus:ring-primary w-full rounded-xl border px-4 py-2 focus:ring-2 focus:outline-none"
                                    required
                                />
                                {field === 'confirmPassword' && (
                                    <button
                                        type="button"
                                        onClick={() => setShowPassword(!showPassword)}
                                        className="text-txtcolor-300 absolute top-1/2 right-3 -translate-y-1/2"
                                    >
                                        {showPassword ? (
                                            <EyeOff className="h-4 w-4" />
                                        ) : (
                                            <Eye className="h-4 w-4" />
                                        )}
                                    </button>
                                )}
                            </div>
                        </div>
                    ))}
                    <div className="flex gap-3 pt-4">
                        <button
                            type="button"
                            onClick={onClose}
                            className="text-txtcolor-300 hover:bg-secondarybg flex-1 rounded-xl px-4 py-2 font-bold transition-colors"
                        >
                            Anuluj
                        </button>
                        <button
                            type="submit"
                            disabled={changePasswordMutation.isPending}
                            className="bg-primary text-darkgray hover:bg-secondary flex flex-1 items-center justify-center gap-2 rounded-xl px-4 py-2 font-bold transition-colors"
                        >
                            {changePasswordMutation.isPending ? (
                                <Loader2 className="h-4 w-4 animate-spin" />
                            ) : (
                                <Check className="h-4 w-4" />
                            )}{' '}
                            Zapisz
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

const InfoCard = ({ label, value }: any) => (
    <div className="bg-background border-border rounded-2xl border p-5 shadow-sm">
        <p className="text-txtcolor-300 mb-1 text-xs font-bold tracking-wider uppercase">{label}</p>
        <p className="text-foreground text-lg font-bold">{value}</p>
    </div>
);

const StatBox = ({ value, label, color, bg }: any) => (
    <div className={`rounded-xl border p-4 ${bg} border-transparent`}>
        <p className={`text-3xl font-black ${color}`}>{value}</p>
        <p className={`mt-1 text-[10px] font-bold uppercase opacity-70 ${color}`}>{label}</p>
    </div>
);

const CouncilRow = ({ council, isActive }: any) => {
    const roleLabels: Record<string, { label: string; style: string }> = {
        PRZEWODNICZACY_SU: {
            label: 'Przewodniczący',
            style: 'bg-warning/10 text-warning border-warning/20',
        },
        ZASTEPCA_SU: {
            label: 'Zastępca Przewodniczącego',
            style: 'bg-secondary/10 text-secondary border-secondary/20',
        },
        SKARBNIK_SU: { label: 'Skarbnik', style: 'bg-success/10 text-success border-success/20' },
        OPIEKUN_SU: { label: 'Opiekun', style: 'bg-accent/10 text-accent border-accent/20' },
        CZLONEK_SU: { label: 'Członek', style: 'bg-info/10 text-info border-info/20' },
        BYLY_CZLONEK_SU: {
            label: 'Były Członek',
            style: 'bg-zinc/10 text-txtcolor-300 border-zinc/20',
        },
        default: { label: 'Członek', style: 'bg-info/10 text-info border-info/20' },
    };

    const roleStyle = roleLabels[council.userRole] || roleLabels['default'];
    return (
        <div
            className={`flex flex-col items-start justify-between rounded-xl border p-4 sm:flex-row sm:items-center ${isActive ? 'bg-secondarybg/30 border-secondarybg' : 'bg-secondarybg/10 border-transparent opacity-75'}`}
        >
            <div>
                <p className="text-foreground font-bold">{council.councilName}</p>
                <p className="text-txtcolor-300 mt-1 text-xs">
                    {isActive
                        ? `Początek: ${format(parseISO(council.startDate), 'dd.MM.yyyy', { locale: pl })}`
                        : `Koniec: ${council.endDate ? format(parseISO(council.endDate), 'dd.MM.yyyy') : '-'}`}
                </p>
            </div>
            <span
                className={`mt-2 rounded-full border px-3 py-1 text-xs font-bold tracking-wider uppercase sm:mt-0 ${roleStyle.style}`}
            >
                {roleStyle.label}
            </span>
        </div>
    );
};

const EventRow = ({ event, type }: any) => {
    const isCurrent = type === 'current';
    return (
        <div
            className={`flex items-center justify-between rounded-xl border p-3 ${isCurrent ? 'bg-info/5 border-info/10' : 'bg-secondarybg/30 border-secondarybg opacity-60'}`}
        >
            <div>
                <p className="text-foreground text-sm font-bold">{event.title}</p>
                <p className="text-txtcolor-300 text-[10px]">
                    {format(parseISO(event.startDate), 'dd.MM', { locale: pl })} -{' '}
                    {format(parseISO(event.endDate), 'dd.MM.yyyy', { locale: pl })}
                </p>
            </div>
            {isCurrent && (
                <span className="text-info bg-background border-info/20 rounded border px-2 py-1 text-[10px] font-bold">
                    Aktywne
                </span>
            )}
        </div>
    );
};

const GlobalRoleBadge = ({ role }: { role: string }) => {
    const normalized = role.toUpperCase();
    let style = {
        label: role,
        icon: BadgeCheck,
        classes: 'bg-secondarybg text-txtcolor-300 border-border',
    };

    if (normalized.includes('ADMIN'))
        style = {
            label: 'Administrator',
            icon: Shield,
            classes: 'bg-purple-500/10 text-purple-600 border-purple-500/20',
        };
    else if (normalized.includes('TEACHER'))
        style = {
            label: 'Nauczyciel',
            icon: GraduationCap,
            classes: 'bg-emerald-500/10 text-emerald-600 border-emerald-500/20',
        };
    else if (normalized.includes('STUDENT'))
        style = {
            label: 'Uczeń',
            icon: User,
            classes: 'bg-blue-500/10 text-blue-600 border-blue-500/20',
        };

    const Icon = style.icon;
    return (
        <span
            className={`flex items-center gap-1 rounded-full border px-3 py-1 text-xs font-bold ${style.classes}`}
        >
            <Icon className="h-3 w-3" /> {style.label}
        </span>
    );
};
