'use client';

import { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { X, Save, Loader2, Archive, CheckCircle, ShieldCheck } from 'lucide-react';
import { updateCouncil } from '@/lib/api/council';
import { CouncilResponseDto, CouncilRequestDto } from '@/types/council.types';
import FormField from '@/components/FormField';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    council: CouncilResponseDto;
}

const toInputDate = (dateString?: string) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-CA');
};

export default function EditCouncilModal({ isOpen, onClose, council }: Props) {
    const queryClient = useQueryClient();

    const [name, setName] = useState(council.name);
    const [academicYear, setAcademicYear] = useState(council.academicYear);
    const [startDate, setStartDate] = useState(toInputDate(council.startDate));
    const [endDate, setEndDate] = useState(toInputDate(council.endDate));

    const [active, setActive] = useState(council.active);
    const [defaultCouncil, setDefaultCouncil] = useState(council.defaultCouncil);

    useEffect(() => {
        if (isOpen) {
            setName(council.name);
            setAcademicYear(council.academicYear);
            setStartDate(toInputDate(council.startDate));
            setEndDate(toInputDate(council.endDate));
            setActive(council.active);
            setDefaultCouncil(council.defaultCouncil);
        }
    }, [council, isOpen]);

    const mutation = useMutation({
        mutationFn: (data: CouncilRequestDto) => updateCouncil(council.id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['council', council.id] });
            queryClient.invalidateQueries({ queryKey: ['userCouncils'] });
            onClose();
        },
        onError: (err) => alert(err instanceof Error ? err.message : 'Błąd edycji'),
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        mutation.mutate({
            name,
            academicYear,
            startDate,
            endDate,
            active,
            defaultCouncil
        });
    };

    const handleArchiveToggle = () => {
        if (active) {
            if(confirm("Czy na pewno chcesz zarchiwizować ten samorząd? Nie będzie on już domyślny.")) {
                setActive(false);
                setDefaultCouncil(false);
            }
        } else {
            setActive(true);
        }
    };

    const handleDefaultToggle = () => {
        if (!defaultCouncil) {
            setDefaultCouncil(true);
            setActive(true);
        } else {
            setDefaultCouncil(false);
        }
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm p-4 animate-in fade-in duration-200">
            <div className="w-full max-w-lg bg-background border border-border rounded-xl shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200 flex flex-col max-h-[90vh]">

                <div className="flex justify-between items-center p-4 border-b border-border bg-secondarybg shrink-0">
                    <h3 className="font-bold text-lg text-foreground">Edytuj Samorząd</h3>
                    <button onClick={onClose} className="text-txtcolor-300 hover:text-foreground transition-colors">
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <div className="p-6 overflow-y-auto custom-scrollbar">
                    <form id="edit-council-form" onSubmit={handleSubmit} className="space-y-5">
                        <div className="space-y-4">
                            <FormField
                                id="name"
                                label="Nazwa"
                                type="text"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                placeholder="np. Samorząd ZSO nr. 1"
                                disabled={mutation.isPending}
                            />

                            <FormField
                                id="year"
                                label="Rok szkolny"
                                type="text"
                                value={academicYear}
                                onChange={(e) => setAcademicYear(e.target.value)}
                                placeholder="np. 2025/2026"
                                disabled={mutation.isPending}
                            />

                            <div className="grid grid-cols-2 gap-4">
                                <FormField
                                    id="start"
                                    label="Początek"
                                    type="date"
                                    value={startDate}
                                    onChange={(e) => setStartDate(e.target.value)}
                                    placeholder=""
                                    disabled={mutation.isPending}
                                />
                                <FormField
                                    id="end"
                                    label="Koniec"
                                    type="date"
                                    value={endDate}
                                    onChange={(e) => setEndDate(e.target.value)}
                                    placeholder=""
                                    disabled={mutation.isPending}
                                />
                            </div>
                        </div>

                        <div className="h-px bg-border my-2" />

                        <div className="space-y-3">
                            <h4 className="text-sm font-bold text-txtcolor-300 uppercase tracking-wider">Status i Widoczność</h4>
                            <div className="grid grid-cols-1 gap-3">

                                <div
                                    onClick={handleArchiveToggle}
                                    className={`p-3 rounded-lg border cursor-pointer transition-all flex items-center justify-between ${
                                        active
                                            ? 'bg-success/5 border-success/30 hover:bg-success/10'
                                            : 'bg-error/5 border-error/30 hover:bg-error/10'
                                    }`}
                                >
                                    <div className="flex items-center gap-3">
                                        <div className={`p-2 rounded-md ${active ? 'bg-success/20 text-success' : 'bg-error/20 text-error'}`}>
                                            {active ? <CheckCircle className="w-5 h-5" /> : <Archive className="w-5 h-5" />}
                                        </div>
                                        <div className="text-sm">
                                            <p className={`font-bold ${active ? 'text-success' : 'text-error'}`}>
                                                {active ? 'Samorząd Aktywny' : 'Zarchiwizowany'}
                                            </p>
                                            <p className="text-xs text-txtcolor-300">
                                                {active ? 'Kliknij, aby zarchiwizować kadencję.' : 'Kliknij, aby przywrócić aktywność.'}
                                            </p>
                                        </div>
                                    </div>
                                    <div className={`w-10 h-5 rounded-full relative transition-colors ${active ? 'bg-success' : 'bg-inputbg'}`}>
                                        <div className={`absolute top-1 left-1 bg-white w-3 h-3 rounded-full transition-transform ${active ? 'translate-x-5' : ''}`} />
                                    </div>
                                </div>
                                <div
                                    onClick={!active ? undefined : handleDefaultToggle}
                                    className={`p-3 rounded-lg border transition-all flex items-center justify-between ${
                                        !active ? 'opacity-50 cursor-not-allowed border-border bg-inputbg' :
                                            defaultCouncil
                                                ? 'bg-secondary/5 border-secondary/30 hover:bg-secondary/10 cursor-pointer'
                                                : 'bg-inputbg border-border hover:border-txtcolor-300 cursor-pointer'
                                    }`}
                                >
                                    <div className="flex items-center gap-3">
                                        <div className={`p-2 rounded-md ${defaultCouncil ? 'bg-secondary/20 text-secondary' : 'bg-inputbg text-txtcolor-300'}`}>
                                            <ShieldCheck className="w-5 h-5" />
                                        </div>
                                        <div className="text-sm">
                                            <p className={`font-bold ${defaultCouncil ? 'text-secondary' : 'text-foreground'}`}>
                                                Główny Samorząd
                                            </p>
                                            <p className="text-xs text-txtcolor-300">
                                                Domyślny cel dla nowych sugestii.
                                            </p>
                                        </div>
                                    </div>
                                    <div className={`w-10 h-5 rounded-full relative transition-colors ${defaultCouncil ? 'bg-secondary' : 'bg-txtcolor-300/30'}`}>
                                        <div className={`absolute top-1 left-1 bg-white w-3 h-3 rounded-full transition-transform ${defaultCouncil ? 'translate-x-5' : ''}`} />
                                    </div>
                                </div>

                            </div>
                        </div>

                    </form>
                </div>

                <div className="flex justify-end gap-3 p-4 border-t border-border bg-secondarybg shrink-0">
                    <button
                        type="button"
                        onClick={onClose}
                        disabled={mutation.isPending}
                        className="px-4 py-2 rounded-lg text-sm font-medium text-txtcolor-300 hover:bg-inputbg transition-colors"
                    >
                        Anuluj
                    </button>
                    <button
                        type="submit"
                        form="edit-council-form"
                        disabled={mutation.isPending}
                        className="bg-primary text-darkgray px-4 py-2 rounded-lg font-bold text-sm flex items-center gap-2 hover:opacity-90 disabled:opacity-50 transition-all"
                    >
                        {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
                        Zapisz zmiany
                    </button>
                </div>

            </div>
        </div>
    );
}