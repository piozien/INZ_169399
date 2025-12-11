import React from 'react';

export default function MicrosoftIcon({ className }: { className?: string }) {
    return (
        <svg
            width="21"
            height="21"
            viewBox="0 0 21 21"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            className={className}
        >
            <path d="M1 1H9.5V9.5H1V1Z" fill="#F25022" />
            <path d="M11.5 1H20V9.5H11.5V1Z" fill="#7FBA00" />
            <path d="M1 11.5H9.5V20H1V11.5Z" fill="#00A4EF" />
            <path d="M11.5 11.5H20V20H11.5V11.5Z" fill="#FFB900" />
        </svg>
    );
}
