import { LucideIcon, ArrowRight } from 'lucide-react';
import Link from 'next/link';

interface Props {
    icon: LucideIcon;
    iconColor?: string;
    title: string;
    value?: string | number;
    unit?: string;
    linkHref?: string;
    linkLabel?: string;
    customContent?: React.ReactNode;
}

export default function StatCard({ icon: Icon, iconColor, title, value, unit, linkHref, linkLabel, customContent }: Props) {
    return (
        <div className="bg-secondarybg p-6 rounded-xl border border-border flex flex-col justify-between">
            <div>
                <div className="flex items-center gap-2 text-txtcolor-300 mb-2">
                    <Icon className={`h-5 w-5 ${iconColor}`} />
                    <span className="text-sm font-medium uppercase tracking-wider">{title}</span>
                </div>

                {customContent ? customContent : (
                    <div className="mt-4">
                        <span className="text-4xl font-bold text-foreground">{value}</span>
                        {unit && <span className="text-txtcolor-300 ml-2">{unit}</span>}
                    </div>
                )}
            </div>

            {linkHref && linkLabel && (
                <Link
                    href={linkHref}
                    className="mt-4 flex items-center text-sm text-secondary font-medium hover:underline"
                >
                    {linkLabel} <ArrowRight className="h-4 w-4 ml-1" />
                </Link>
            )}
        </div>
    );
}