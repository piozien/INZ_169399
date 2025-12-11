'use client';

import { X, Cookie } from 'lucide-react';
import { useCookieConsent } from '@/hooks/useCookieConsent';

export default function CookieConsent() {
    const { showBanner, acceptCookies } = useCookieConsent();

    if (!showBanner) return null;

    return (
        <div className="animate-in slide-in-from-bottom-4 fixed right-4 bottom-4 left-4 z-[100] duration-500 md:left-auto md:w-[400px]">
            <div className="bg-secondarybg/95 border-border flex flex-col gap-4 rounded-xl border p-5 shadow-2xl backdrop-blur-md">
                <div className="flex items-start justify-between">
                    <div className="text-primary flex items-center gap-3">
                        <Cookie className="h-6 w-6" />
                        <span className="text-lg font-bold">Pliki Cookies</span>
                    </div>
                    <button
                        onClick={acceptCookies}
                        className="text-txtcolor-300 hover:text-foreground transition-colors"
                        aria-label="Zamknij"
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <p className="text-txtcolor-300 text-sm leading-relaxed">
                    Ta strona używa plików cookies wyłącznie w celu zapewnienia poprawnego działania
                    mechanizmu logowania i bezpieczeństwa sesji. Korzystając ze strony, wyrażasz na
                    to zgodę.
                </p>

                <div className="mt-2 flex gap-3">
                    <button
                        onClick={acceptCookies}
                        className="bg-primary text-darkgray shadow-primary/10 flex-1 rounded-lg px-4 py-2 text-sm font-bold shadow-lg transition-opacity hover:opacity-90"
                    >
                        Rozumiem, wchodzę
                    </button>
                </div>
            </div>
        </div>
    );
}
