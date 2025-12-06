'use client';

import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { createCouncil } from '@/lib/api/council';

interface CreateCouncilFormProps {
  onCancel: () => void;
  onSuccess: () => void;
}

interface CouncilFormData {
  name: string;
  academicYear: string;
  startDate: string;
  endDate: string;
}

export default function CreateCouncilForm({ onCancel, onSuccess }: CreateCouncilFormProps) {
  const [formData, setFormData] = useState<CouncilFormData>({
    name: '',
    academicYear: '',
    startDate: '',
    endDate: '',
  });

  const [error, setError] = useState<string | null>(null);
  const queryClient = useQueryClient();

  const createMutation = useMutation({
    mutationFn: createCouncil,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['userCouncils'] });
      onSuccess();
    },
    onError: (err) => {
      setError(err instanceof Error ? err.message : 'Wystąpił błąd');
    },
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { id, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [id]: value,
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!formData.name.trim() || !formData.academicYear.trim() || !formData.startDate || !formData.endDate) {
      setError('Wszystkie pola są wymagane.');
      return;
    }

    if (new Date(formData.startDate) > new Date(formData.endDate)) {
      setError('Data rozpoczęcia nie może być późniejsza niż data zakończenia.');
      return;
    }

    createMutation.mutate(formData);
  };

  const inputClassName = "w-full bg-inputbg text-foreground rounded-lg px-4 py-3 focus:outline-none focus:ring-2 focus:ring-secondary transition-all placeholder-txtcolor-300/30";
  const labelClassName = "block text-xs text-txtcolor-300 mb-2";

  return (
    <div className="w-full">
      <div className="mb-6">
        <span className="text-lg font-medium text-foreground border-b-2 border-secondary pb-1">
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
                className="flex-1 px-4 py-3 rounded-full border border-border text-txtcolor-300 hover:text-foreground hover:bg-inputbg transition-colors font-medium text-sm"
            >
                Anuluj
            </button>
            <button
                type="submit"
                disabled={createMutation.isPending}
                className="flex-1 bg-primary text-background font-semibold px-4 py-3 rounded-full hover:opacity-90 disabled:opacity-50 transition-opacity text-sm"
            >
                {createMutation.isPending ? 'Tworzenie...' : 'Stwórz samorząd'}
            </button>
        </div>
      </form>
    </div>
  );
}