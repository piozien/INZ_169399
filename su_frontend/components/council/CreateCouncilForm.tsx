'use client';

import { useCreateCouncilForm } from '@/hooks/council/useCreateCouncilForm';

interface CreateCouncilFormProps {
    onCancel: () => void;
    onSuccess: () => void;
}

export default function CreateCouncilForm({ onCancel, onSuccess }: CreateCouncilFormProps) {
    const { formData, error, handleChange, handleSubmit, isPending } =
        useCreateCouncilForm(onSuccess);

    const inputClassName =
        'w-full bg-inputbg text-foreground rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-secondary transition-all placeholder-txtcolor-300/30';
    const labelClassName = 'block text-xs text-txtcolor-300 mb-2';

    return (
        <div className="w-full">
            <div className="mb-6">
                <span className="text-foreground border-secondary border-b-2 pb-1 text-lg font-medium">
                    Nowy samorząd
                </span>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5">
                <div>
                    <label htmlFor="name" className={labelClassName}>
                        Nazwa samorządu
                    </label>
                    <input
                        type="text"
                        id="name"
                        value={formData.name}
                        onChange={handleChange}
                        className={inputClassName}
                        placeholder="np. Samorząd Uczniowski ZSO nr 1"
                        required
                    />
                </div>

                <div>
                    <label htmlFor="academicYear" className={labelClassName}>
                        Rok szkolny
                    </label>
                    <input
                        type="text"
                        id="academicYear"
                        value={formData.academicYear}
                        onChange={handleChange}
                        className={inputClassName}
                        placeholder="np. 2024/2025"
                        required
                    />
                </div>

                <div className="flex gap-4">
                    <div className="flex-1">
                        <label htmlFor="startDate" className={labelClassName}>
                            Data rozpoczęcia
                        </label>
                        <input
                            type="date"
                            id="startDate"
                            value={formData.startDate}
                            onChange={handleChange}
                            className={`${inputClassName} [color-scheme:dark]`}
                            required
                        />
                    </div>
                    <div className="flex-1">
                        <label htmlFor="endDate" className={labelClassName}>
                            Data zakończenia
                        </label>
                        <input
                            type="date"
                            id="endDate"
                            value={formData.endDate}
                            onChange={handleChange}
                            className={`${inputClassName} [color-scheme:dark]`}
                            required
                        />
                    </div>
                </div>

                {error && <p className="text-error text-sm">{error}</p>}

                <div className="flex gap-3 pt-2">
                    <button
                        type="button"
                        onClick={onCancel}
                        className="border-border text-txtcolor-300 hover:text-foreground hover:bg-inputbg flex-1 rounded-full border px-4 py-3 text-sm font-medium transition-colors"
                    >
                        Anuluj
                    </button>
                    <button
                        type="submit"
                        disabled={isPending}
                        className="bg-primary text-background flex-1 rounded-full px-4 py-3 text-sm font-semibold transition-opacity hover:opacity-90 disabled:opacity-50"
                    >
                        {isPending ? 'Tworzenie...' : 'Stwórz samorząd'}
                    </button>
                </div>
            </form>
        </div>
    );
}
