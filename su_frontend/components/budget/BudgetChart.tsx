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

    const hasData = income > 0 || expenses > 0;

    if (!hasData) {
        return (
            <div className="bg-secondarybg border-border text-txtcolor-300 flex h-[350px] w-full flex-col items-center justify-center gap-3 rounded-xl border p-6">
                <div className="bg-background border-border rounded-full border p-4">
                    <PieChartIcon className="h-8 w-8 opacity-20" />
                </div>
                <p className="text-sm font-medium">Brak operacji finansowych</p>
                <p className="text-xs opacity-60">Dodaj transakcje, aby zobaczyć wykres.</p>
            </div>
        );
    }

    return (
        <div className="bg-secondarybg border-border flex h-[350px] w-full flex-col rounded-xl border p-4 shadow-sm">
            <h3 className="text-txtcolor-300 mb-2 pl-2 text-xs font-bold tracking-wider uppercase">
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
                                borderRadius: '8px',
                                color: 'var(--color-foreground)',
                                boxShadow: '0 4px 12px rgba(0,0,0,0.1)',
                                padding: '8px 12px',
                            }}
                            itemStyle={{
                                color: 'var(--color-foreground)',
                                fontWeight: 'bold',
                                fontSize: '13px',
                            }}
                            formatter={(value: number) => `${value.toFixed(2)} PLN`}
                            cursor={false}
                        />
                        <Legend
                            verticalAlign="bottom"
                            height={36}
                            iconType="circle"
                            iconSize={8}
                            formatter={(value) => (
                                <span className="text-txtcolor-300 ml-1 text-xs font-medium uppercase tracking-wide">
                                    {value}
                                </span>
                            )}
                        />
                    </PieChart>
                </ResponsiveContainer>
            </div>
        </div>
    );
}