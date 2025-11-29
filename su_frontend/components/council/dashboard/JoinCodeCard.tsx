'use client';

import { useState } from 'react';
import { Hash, Copy, Check } from 'lucide-react';

interface Props {
    joinCode: string;
}

export default function JoinCodeCard({ joinCode }: Props) {
    const [copied, setCopied] = useState(false);

    const copyToClipboard = () => {
        navigator.clipboard.writeText(joinCode);
        setCopied(true);
        setTimeout(() => setCopied(false), 2000);
    };

    return (
        <div className="bg-secondarybg p-6 rounded-xl border border-border flex flex-col justify-between group hover:border-secondary/50 transition-colors">
            <div>
                <div className="flex items-center gap-2 text-txtcolor-300 mb-2">
                    <Hash className="h-5 w-5 text-secondary" />
                    <span className="text-sm font-medium uppercase tracking-wider">Kod dołączenia</span>
                </div>
                <p className="text-sm text-txtcolor-300 mb-4">
                    Podaj ten kod uczniom, aby mogli dołączyć do samorządu.
                </p>
            </div>
            <button
                onClick={copyToClipboard}
                className="flex items-center justify-between w-full bg-inputbg p-3 rounded-lg border border-border hover:border-secondary group-hover:bg-background transition-all"
            >
                <code className="text-xl font-mono font-bold text-primary tracking-widest">
                    {joinCode}
                </code>
                {copied ? (
                    <Check className="h-5 w-5 text-green-500" />
                ) : (
                    <Copy className="h-5 w-5 text-txtcolor-300 group-hover:text-secondary" />
                )}
            </button>
        </div>
    );
}