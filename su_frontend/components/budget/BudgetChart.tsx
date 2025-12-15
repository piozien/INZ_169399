'use client';

import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import { PieChart as PieChartIcon } from 'lucide-react';

interface Props {
    income: number;
    expenses: number;
}

export default function BudgetChart({ income, expenses }: Props) {
    const data = [
        { name: 'Przychody', value: income, color: 'var(--color-success)' },
        { name: 'Wydatki', value: expenses, color: 'var(--color-error)' },
    ];

    if (income === 0 && expenses === 0) {
        return (
            <div className="bg-secondarybg border-border text-txtcolor-300 flex h-[350px] w-full flex-col items-center justify-center gap-3 rounded-xl border p-6">
                <div className="bg-background border-border rounded-full border p-4">
                    <PieChartIcon className="h-8 w-8 opacity-20" />
                </div>
                <p className="text-sm">Brak danych finansowych do wykresu</p>
            </div>
        );
    }

    return (
        <div className="bg-secondarybg border-border flex h-[350px] w-full flex-col rounded-xl border p-4 shadow-sm">
            <h3 className="text-txtcolor-300 mb-2 pl-2 text-sm font-bold tracking-wider uppercase">
                Struktura Budżetu
            </h3>

            <div className="min-h-0 w-full flex-1">
                <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                        <Pie
                            data={data}
                            cx="50%"
                            cy="50%"
                            innerRadius="60%"
                            outerRadius="80%"
                            paddingAngle={5}
                            dataKey="value"
                            stroke="none"
                        >
                            {data.map((entry, index) => (
                                <Cell key={`cell-${index}`} fill={entry.color} />
                            ))}
                        </Pie>
                        <Tooltip
                            contentStyle={{
                                backgroundColor: 'var(--color-background)',
                                borderColor: 'var(--color-border)',
                                borderRadius: '12px',
                                color: 'var(--color-foreground)',
                                boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)',
                            }}
                            itemStyle={{ color: 'var(--color-foreground)', fontWeight: 'bold' }}
                            formatter={(value: number) => `${value.toFixed(2)} PLN`}
                        />
                        <Legend
                            verticalAlign="bottom"
                            height={36}
                            iconType="circle"
                            formatter={(value) => (
                                <span className="text-txtcolor-300 ml-1 text-sm">{value}</span>
                            )}
                        />
                    </PieChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
}
