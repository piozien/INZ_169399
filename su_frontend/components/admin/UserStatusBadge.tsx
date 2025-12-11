import { Ban, CheckCircle, Clock, HelpCircle } from 'lucide-react';
import { UserDto } from '@/types/user.types';

const STATUS_CONFIG = {
    CONFIRMED: {
        label: 'Aktywny',
        icon: CheckCircle,
        className: 'text-success bg-success/10 border-success/20',
    },
    PENDING: {
        label: 'Oczekuje',
        icon: Clock,
        className: 'text-warning bg-warning/10 border-warning/20',
    },
    BLOCKED: {
        label: 'Zablokowany',
        icon: Ban,
        className: 'text-error bg-error/10 border-error/20',
    },
};

export default function UserStatusBadge({ status }: { status: UserDto['status'] }) {
    const config = STATUS_CONFIG[status] || {
        label: status,
        icon: HelpCircle,
        className: 'text-txtcolor-300 bg-inputbg border-border',
    };

    const Icon = config.icon;

    return (
        <span
            className={`inline-flex items-center gap-1.5 rounded-full border px-2.5 py-0.5 text-[10px] font-bold uppercase ${config.className}`}
        >
            <Icon className="h-3 w-3" />
            {config.label}
        </span>
    );
}
