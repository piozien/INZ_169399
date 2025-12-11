'use client';

import { X, Save, Loader2, Archive, CheckCircle, ShieldCheck } from 'lucide-react';
import { CouncilResponseDto } from '@/types/council.types';
import FormField from '@/components/FormField';
import { useEditCouncil } from '@/hooks/council/useEditCouncil';

interface Props {
    isOpen: boolean;
    onClose: () => void;
    council: CouncilResponseDto;
}

export default function EditCouncilModal({ isOpen, onClose, council }: Props) {
    const {
        name,
        setName,
        academicYear,
        setAcademicYear,
        startDate,
        setStartDate,
        endDate,
        setEndDate,
        active,
        toggleArchive,
        defaultCouncil,
        toggleDefault,
        handleSubmit,
        isPending,
    } = useEditCouncil(council, onClose, isOpen);

    if (!isOpen) return null;

    return (
        <div className="bg-background/80 animate-in fade-in fixed inset-0 z-50 flex items-center justify-center p-4 backdrop-blur-sm duration-200">
            <div className="bg-background border-border animate-in zoom-in-95 flex max-h-[90vh] w-full max-w-lg flex-col overflow-hidden rounded-xl border shadow-2xl duration-200">
                <div className="border-border bg-secondarybg flex shrink-0 items-center justify-between border-b p-4">
                    <h3 className="text-foreground text-lg font-bold">Edytuj Samorząd</h3>
                    <button
                        onClick={onClose}
                        className="text-txtcolor-300 hover:text-foreground transition-colors"
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <div className="custom-scrollbar overflow-y-auto p-6">
                    <form id="edit-council-form" onSubmit={handleSubmit} className="space-y-5">
                        <div className="space-y-4">
                            <FormField
                                id="name"
                                label="Nazwa"
                                type="text"
                                value={name}
                                onChange={(e) => setName(e.target.value)}
                                placeholder="np. Samorząd ZSO nr. 1"
                                disabled={isPending}
                            />
                            <FormField
                                id="year"
                                label="Rok szkolny"
                                type="text"
                                value={academicYear}
                                onChange={(e) => setAcademicYear(e.target.value)}
                                placeholder="np. 2025/2026"
                                disabled={isPending}
                            />
                            <div className="grid grid-cols-2 gap-4">
                                <FormField
                                    id="start"
                                    label="Początek"
                                    type="date"
                                    value={startDate}
                                    onChange={(e) => setStartDate(e.target.value)}
                                    placeholder=""
                                    disabled={isPending}
                                />
                                <FormField
                                    id="end"
                                    label="Koniec"
                                    type="date"
                                    value={endDate}
                                    onChange={(e) => setEndDate(e.target.value)}
                                    placeholder=""
                                    disabled={isPending}
                                />
                            </div>
                        </div>

                        <div className="bg-border my-2 h-px" />

                        <div className="space-y-3">
                            <h4 className="text-txtcolor-300 text-sm font-bold tracking-wider uppercase">
                                Status i Widoczność
                            </h4>
                            <div className="grid grid-cols-1 gap-3">
                                <ToggleCard
                                    active={active}
                                    onClick={toggleArchive}
                                    activeIcon={CheckCircle}
                                    inactiveIcon={Archive}
                                    title={active ? 'Samorząd Aktywny' : 'Zarchiwizowany'}
                                    desc={
                                        active
                                            ? 'Kliknij, aby zarchiwizować kadencję.'
                                            : 'Kliknij, aby przywrócić aktywność.'
                                    }
                                    colorClass={active ? 'success' : 'error'}
                                />

                                <ToggleCard
                                    active={defaultCouncil}
                                    onClick={!active ? undefined : toggleDefault}
                                    disabled={!active}
                                    activeIcon={ShieldCheck}
                                    inactiveIcon={ShieldCheck}
                                    title="Główny Samorząd"
                                    desc="Domyślny cel dla nowych sugestii."
                                    colorClass="secondary"
                                />
                            </div>
                        </div>
                    </form>
                </div>

                <div className="border-border bg-secondarybg flex shrink-0 justify-end gap-3 border-t p-4">
                    <button
                        type="button"
                        onClick={onClose}
                        disabled={isPending}
                        className="text-txtcolor-300 hover:bg-inputbg rounded-lg px-4 py-2 text-sm font-medium transition-colors"
                    >
                        Anuluj
                    </button>
                    <button
                        type="submit"
                        form="edit-council-form"
                        disabled={isPending}
                        className="bg-primary text-darkgray flex items-center gap-2 rounded-lg px-4 py-2 text-sm font-bold transition-all hover:opacity-90 disabled:opacity-50"
                    >
                        {isPending ? (
                            <Loader2 className="h-4 w-4 animate-spin" />
                        ) : (
                            <Save className="h-4 w-4" />
                        )}{' '}
                        Zapisz zmiany
                    </button>
                </div>
            </div>
        </div>
    );
}

const ToggleCard = ({
    active,
    onClick,
    disabled,
    activeIcon: IconA,
    inactiveIcon: IconB,
    title,
    desc,
    colorClass,
}: any) => {
    const colors: any = {
        success: {
            bg: 'bg-success/5 border-success/30 hover:bg-success/10',
            icon: 'bg-success/20 text-success',
            text: 'text-success',
            switch: 'bg-success',
        },
        error: {
            bg: 'bg-error/5 border-error/30 hover:bg-error/10',
            icon: 'bg-error/20 text-error',
            text: 'text-error',
            switch: 'bg-inputbg',
        },
        secondary: {
            bg: 'bg-secondary/5 border-secondary/30 hover:bg-secondary/10',
            icon: 'bg-secondary/20 text-secondary',
            text: 'text-secondary',
            switch: 'bg-secondary',
        },
    };

    const currentStyle = active
        ? colors[colorClass]
        : {
              bg: 'bg-inputbg border-border hover:border-txtcolor-300',
              icon: 'bg-inputbg text-txtcolor-300',
              text: 'text-foreground',
              switch: 'bg-txtcolor-300/30',
          };
    const wrapperClass = disabled
        ? 'opacity-50 cursor-not-allowed bg-inputbg border-border'
        : `${currentStyle.bg} cursor-pointer`;

    return (
        <div
            onClick={onClick}
            className={`flex items-center justify-between rounded-lg border p-3 transition-all ${wrapperClass}`}
        >
            <div className="flex items-center gap-3">
                <div
                    className={`rounded-md p-2 ${active ? currentStyle.icon : 'bg-inputbg text-txtcolor-300'}`}
                >
                    <IconA className="h-5 w-5" />
                </div>
                <div className="text-sm">
                    <p className={`font-bold ${active ? currentStyle.text : 'text-foreground'}`}>
                        {title}
                    </p>
                    <p className="text-txtcolor-300 text-xs">{desc}</p>
                </div>
            </div>
            <div
                className={`relative h-5 w-10 rounded-full transition-colors ${active ? currentStyle.switch : 'bg-inputbg'}`}
            >
                <div
                    className={`absolute top-1 left-1 h-3 w-3 rounded-full bg-white transition-transform ${active ? 'translate-x-5' : ''}`}
                />
            </div>
        </div>
    );
};
