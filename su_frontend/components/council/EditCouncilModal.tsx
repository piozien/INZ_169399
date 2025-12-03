'use client';

import { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { X, Save, Loader2 } from 'lucide-react';
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

    useEffect(() => {
        if (isOpen) {
            setName(council.name);
            setAcademicYear(council.academicYear);
            setStartDate(toInputDate(council.startDate));
            setEndDate(toInputDate(council.endDate));
        }
    }, [council, isOpen]);

    const mutation = useMutation({
        mutationFn: (data: CouncilRequestDto) => updateCouncil(council.id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['council', council.id] });
            onClose();
        },
        onError: (err) => alert(err instanceof Error ? err.message : 'Błąd edycji'),
    });

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        mutation.mutate({ name, academicYear, startDate, endDate });
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 backdrop-blur-sm p-4 animate-in fade-in duration-200">
            <div className="w-full max-w-lg bg-background border border-border rounded-xl shadow-2xl overflow-hidden animate-in zoom-in-95 duration-200">

                <div className="flex justify-between items-center p-4 border-b border-border bg-secondarybg">
                    <h3 className="font-bold text-lg text-foreground">Edytuj Samorząd</h3>
                    <button onClick={onClose} className="text-txtcolor-300 hover:text-foreground transition-colors">
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-6 space-y-4">
                    <FormField
                        id="name"
                        label="Nazwa"
                        type="text"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        placeholder="np. Samorząd 2025/2026"
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

                    <div className="flex justify-end gap-3 pt-4 border-t border-border mt-6">
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
                            disabled={mutation.isPending}
                            className="bg-primary text-darkgray px-4 py-2 rounded-lg font-bold text-sm flex items-center gap-2 hover:opacity-90 disabled:opacity-50 transition-all"
                        >
                            {mutation.isPending ? <Loader2 className="h-4 w-4 animate-spin" /> : <Save className="h-4 w-4" />}
                            Zapisz zmiany
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}