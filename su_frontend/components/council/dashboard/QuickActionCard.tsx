import Link from 'next/link';
import { LucideIcon } from 'lucide-react';

interface Props {
    href: string;
    icon: LucideIcon;
    iconColorClass: string;
    bgColorClass: string;
    title: string;
    description: string;
}

export default function QuickActionCard({ href, icon: Icon, iconColorClass, bgColorClass, title, description }: Props) {
    return (
        <Link href={href} className="group">
            <div className="p-6 bg-secondarybg rounded-xl border border-border hover:border-secondary hover:bg-secondary/5 transition-all flex items-center gap-4">
                <div className={`p-3 rounded-lg group-hover:scale-110 transition-transform ${bgColorClass} ${iconColorClass}`}>
                    <Icon className="h-6 w-6" />
                </div>
                <div>
                    <h3 className="font-bold text-lg">{title}</h3>
                    <p className="text-sm text-txtcolor-300">{description}</p>
                </div>
            </div>
        </Link>
    );
}