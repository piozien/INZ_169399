'use client';

import {useState, useEffect} from 'react';
import {
    X,
    Shield,
    History,
    Plus,
    AlertTriangle,
    CheckCircle,
    Ban,
    Loader2,
    Info,
    Pencil,
    Save,
} from 'lucide-react';
import {UserDto, StatusEnum} from '@/types/user.types';
import {useUserLogs} from '@/hooks/admin/useUserLogs';
import {useAdminUsers} from '@/hooks/admin/useAdminUsers';
import UserStatusBadge from './UserStatusBadge';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    user: UserDto;
}

export default function UserDetailsModal({isOpen, onClose, user}: Props) {
    const [tab, setTab] = useState<'ROLES' | 'LOGS'>('ROLES');

    const [isEditing, setIsEditing] = useState(false);
    const [editForm, setEditForm] = useState({
        fullName: '',
        status: 'PENDING' as StatusEnum,
    });

    const {
        assignRole,
        removeRole,
        deleteUser,
        unblockUser,
        availableRoles,
        updateUser,
        isUpdating
    } = useAdminUsers();

    const {data: logs, isLoading: logsLoading} = useUserLogs(isOpen ? user.id : null);

    useEffect(() => {
        if (user) {
            setEditForm({
                fullName: user.fullName,
                status: user.status,
            });
            setIsEditing(false);
        }
    }, [user, isOpen]);

    const handleSave = async () => {
        if (!editForm.fullName.trim()) return alert('Imię i nazwisko nie może być puste');

        try {
            await updateUser({
                userId: user.id,
                data: {
                    fullName: editForm.fullName,
                    status: editForm.status
                }
            });
            setIsEditing(false);
        } catch (error) {
        }
    };

    if (!isOpen) return null;

    return (
        <div
            className="bg-background/80 animate-in fade-in fixed inset-0 z-[60] flex items-center justify-center p-4 backdrop-blur-sm duration-200">
            <div
                className="bg-background border-border animate-in zoom-in-95 flex max-h-[90vh] w-full max-w-2xl flex-col rounded-2xl border shadow-2xl">

                <div className="border-border bg-secondarybg/20 flex items-start justify-between border-b p-6">
                    <div className="flex-1 mr-4">
                        {isEditing ? (
                            <div className="space-y-3 animate-in fade-in slide-in-from-top-1 duration-200">
                                <div>
                                    <label className="text-xs font-bold text-txtcolor-300 uppercase mb-1 block">Imię i
                                        Nazwisko</label>
                                    <input
                                        type="text"
                                        value={editForm.fullName}
                                        onChange={(e) => setEditForm({...editForm, fullName: e.target.value})}
                                        className="w-full bg-inputbg border border-border rounded-lg px-3 py-2 text-foreground font-bold focus:ring-2 focus:ring-primary focus:outline-none"
                                    />
                                </div>
                                <div>
                                    <label className="text-xs font-bold text-txtcolor-300 uppercase mb-1 block">Status
                                        Konta</label>
                                    <select
                                        value={editForm.status}
                                        onChange={(e) => setEditForm({
                                            ...editForm,
                                            status: e.target.value as StatusEnum
                                        })}
                                        className="w-full bg-inputbg border border-border rounded-lg px-3 py-2 text-foreground text-sm focus:ring-2 focus:ring-primary focus:outline-none"
                                    >
                                        <option value="CONFIRMED">CONFIRMED (Aktywny)</option>
                                        <option value="PENDING">PENDING (Oczekujący)</option>
                                        <option value="BLOCKED">BLOCKED (Zablokowany)</option>
                                    </select>
                                </div>
                                <div className="flex gap-2 pt-1">
                                    <button
                                        onClick={handleSave}
                                        disabled={isUpdating}
                                        className="flex items-center gap-2 bg-primary text-darkgray px-4 py-2 rounded-lg text-sm font-bold hover:bg-primary/90 transition-colors disabled:opacity-50"
                                    >
                                        {isUpdating ? <Loader2 className="h-4 w-4 animate-spin"/> :
                                            <Save className="h-4 w-4"/>}
                                        Zapisz
                                    </button>
                                    <button
                                        onClick={() => {
                                            setIsEditing(false);
                                            setEditForm({fullName: user.fullName, status: user.status});
                                        }}
                                        disabled={isUpdating}
                                        className="flex items-center gap-2 bg-transparent text-txtcolor-300 border border-border px-4 py-2 rounded-lg text-sm font-bold hover:bg-secondarybg transition-colors"
                                    >
                                        Anuluj
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <>
                                <div className="mb-1 flex items-center gap-3">
                                    <h2 className="text-foreground text-xl font-bold">{user.fullName}</h2>
                                    <UserStatusBadge status={user.status}/>

                                    <button
                                        onClick={() => setIsEditing(true)}
                                        className="ml-2 p-1.5 text-txtcolor-300 hover:text-primary hover:bg-primary/10 rounded-lg transition-colors"
                                        title="Edytuj dane"
                                    >
                                        <Pencil className="h-4 w-4"/>
                                    </button>
                                </div>
                                <p className="text-txtcolor-300 text-sm">{user.email}</p>
                                <div
                                    className="text-txtcolor-300 mt-2 font-mono text-[10px] tracking-widest uppercase opacity-50">
                                    ID: {user.id}
                                </div>
                            </>
                        )}
                    </div>

                    {!isEditing && (
                        <button
                            onClick={onClose}
                            className="hover:bg-inputbg text-txtcolor-300 hover:text-foreground rounded-full p-2 transition-colors"
                        >
                            <X className="h-5 w-5"/>
                        </button>
                    )}
                </div>

                <div className="border-border bg-secondarybg/30 flex border-b">
                    <button
                        onClick={() => setTab('ROLES')}
                        className={`flex flex-1 items-center justify-center gap-2 border-b-2 py-3 text-sm font-bold transition-colors ${tab === 'ROLES' ? 'border-primary text-primary bg-primary/5' : 'text-txtcolor-300 hover:bg-secondarybg hover:text-foreground border-transparent'}`}
                    >
                        <Shield className="h-4 w-4"/> Uprawnienia
                    </button>
                    <button
                        onClick={() => setTab('LOGS')}
                        className={`flex flex-1 items-center justify-center gap-2 border-b-2 py-3 text-sm font-bold transition-colors ${tab === 'LOGS' ? 'border-primary text-primary bg-primary/5' : 'text-txtcolor-300 hover:bg-secondarybg hover:text-foreground border-transparent'}`}
                    >
                        <History className="h-4 w-4"/> Historia Aktywności
                    </button>
                </div>

                <div className="custom-scrollbar flex-1 overflow-y-auto p-6">
                    {tab === 'ROLES' && (
                        <div className="animate-in slide-in-from-left-2 fade-in space-y-8 duration-300">

                            <div>
                                <h3 className="text-txtcolor-300 mb-3 flex items-center gap-2 text-xs font-bold tracking-wider uppercase">
                                    <Shield className="h-3 w-3"/> Przypisane Role Globalne
                                </h3>
                                <div className="flex flex-wrap gap-2">
                                    {user.roles.length > 0 ? (
                                        user.roles.map((role) => (
                                            <div
                                                key={role}
                                                className="bg-secondarybg border-border group hover:border-error/30 flex items-center gap-2 rounded-lg border px-3 py-1.5 text-sm font-medium transition-colors"
                                            >
                                                <span className="text-foreground">{role}</span>
                                                <button
                                                    onClick={() => {
                                                        if (confirm(`Czy na pewno odebrać rolę ${role}?`))
                                                            removeRole({userId: user.id, role});
                                                    }}
                                                    className="text-txtcolor-300 hover:text-error opacity-0 transition-colors group-hover:opacity-100"
                                                    title="Odbierz rolę"
                                                >
                                                    <X className="h-3.5 w-3.5"/>
                                                </button>
                                            </div>
                                        ))
                                    ) : (
                                        <span className="text-txtcolor-300 pl-1 text-sm italic">
                                            Brak przypisanych ról globalnych.
                                        </span>
                                    )}
                                </div>
                            </div>

                            <div className="border-border border-t pt-6">
                                <h3 className="text-txtcolor-300 mb-3 flex items-center gap-2 text-xs font-bold tracking-wider uppercase">
                                    <Plus className="h-3 w-3"/> Nadaj Rolę Globalną
                                </h3>
                                <div className="mb-3 flex flex-wrap gap-2">
                                    {availableRoles
                                        .filter((r: string) => !user.roles.includes(r))
                                        .map((role: string) => (
                                            <button
                                                key={role}
                                                onClick={() => assignRole({userId: user.id, role})}
                                                className="bg-primary/5 text-primary border-primary/20 hover:bg-primary/10 flex items-center gap-2 rounded-lg border px-3 py-1.5 text-sm font-bold transition-all active:scale-95"
                                            >
                                                <Plus className="h-3.5 w-3.5"/> {role}
                                            </button>
                                        ))}
                                    {availableRoles.length === 0 && (
                                        <p className="text-warning flex items-center gap-2 text-xs">
                                            <AlertTriangle className="h-4 w-4"/> Brak dostępnych ról do nadania.
                                        </p>
                                    )}
                                </div>
                                <div
                                    className="text-txtcolor-300 bg-secondarybg/30 border-border flex items-start gap-2 rounded border p-2.5 text-[10px]">
                                    <Info className="text-primary mt-0.5 h-4 w-4 shrink-0"/>
                                    <p>
                                        Powyższe role dotyczą całego systemu. Role samorządowe (np. Przewodniczący)
                                        nadaje się w panelu konkretnego samorządu.
                                    </p>
                                </div>
                            </div>
                            <div className="border-error/20 mt-6 border-t pt-6">
                                <h3 className="text-error mb-3 flex items-center gap-2 text-xs font-bold tracking-wider uppercase">
                                    <AlertTriangle className="h-3 w-3"/> Zarządzanie Kontem
                                </h3>
                                <div className="flex gap-3">
                                    <button
                                        onClick={() => setIsEditing(true)}
                                        disabled={isEditing}
                                        className="bg-primary/5 text-primary border-primary/20 hover:bg-primary/10 disabled:opacity-50 disabled:cursor-not-allowed flex flex-1 items-center justify-center gap-2 rounded-xl border py-3 font-bold transition-colors"
                                    >
                                        <Pencil className="h-5 w-5"/> Edytuj Dane
                                    </button>

                                    {user.status === 'BLOCKED' ? (
                                        <button
                                            onClick={() => unblockUser(user.id)}
                                            className="bg-success/10 text-success border-success/20 hover:bg-success/20 flex flex-1 items-center justify-center gap-2 rounded-xl border py-3 font-bold transition-colors"
                                        >
                                            <CheckCircle className="h-5 w-5"/> Odblokuj Konto
                                        </button>
                                    ) : (
                                        <button
                                            onClick={() => {
                                                if (confirm('Czy na pewno zablokować/usunąć tego użytkownika?')) {
                                                    deleteUser(user.id);
                                                    onClose();
                                                }
                                            }}
                                            className="bg-error/5 text-error border-error/20 hover:bg-error/10 flex flex-1 items-center justify-center gap-2 rounded-xl border py-3 font-bold transition-colors"
                                        >
                                            <Ban className="h-5 w-5"/> Zablokuj / Usuń
                                        </button>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}

                    {tab === 'LOGS' && (
                        <div className="animate-in slide-in-from-right-2 fade-in duration-300">
                            {logsLoading ? (
                                <div className="flex justify-center py-10">
                                    <Loader2 className="text-primary h-8 w-8 animate-spin"/>
                                </div>
                            ) : (
                                <div className="space-y-3">
                                    {logs?.length === 0 && (
                                        <div
                                            className="text-txtcolor-300 flex flex-col items-center py-10 text-center opacity-50">
                                            <History className="mb-2 h-10 w-10"/>
                                            <p>Brak odnotowanej aktywności w logach.</p>
                                        </div>
                                    )}
                                    {logs?.map((log) => (
                                        <div
                                            key={log.id}
                                            className="bg-secondarybg/30 border-border hover:border-secondary/50 flex flex-col gap-1 rounded-lg border p-3 text-sm transition-colors"
                                        >
                                            <div className="flex items-center justify-between">
                                                <span
                                                    className="text-foreground bg-background border-border rounded border px-2 py-0.5 text-[10px] font-bold tracking-wide uppercase shadow-sm">
                                                    {log.actionType}
                                                </span>
                                                <span className="text-txtcolor-300 font-mono text-[10px]">
                                                    {new Date(log.createdAt).toLocaleString()}
                                                </span>
                                            </div>
                                            <p className="text-txtcolor-300 pt-1 pl-1 text-sm">
                                                {log.action}
                                            </p>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}