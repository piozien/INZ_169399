'use client';

import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';

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
            <div className="h-[300px] flex items-center justify-center text-txtcolor-300 bg-secondarybg rounded-xl border border-border border-dashed">
                Brak danych do wykresu
            </div>
        );
    }

    return (
        <div className="h-[350px] w-full bg-secondarybg p-4 rounded-xl border border-border flex flex-col">
            <h3 className="text-sm font-bold text-txtcolor-300 mb-2 uppercase tracking-wider">Struktura Budżetu</h3>

            <div className="flex-1 w-full min-h-0">
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
                            contentStyle={{ backgroundColor: 'var(--color-background)', borderColor: 'var(--color-border)', borderRadius: '8px', color: 'var(--color-foreground)' }}
                            itemStyle={{ color: 'var(--color-foreground)' }}
                            formatter={(value: number) => `${value.toFixed(2)} PLN`}
                        />
                        <Legend
                            verticalAlign="bottom"
                            height={36}
                            iconType="circle"
                        />
                    </PieChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
}