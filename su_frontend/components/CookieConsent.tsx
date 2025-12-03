'use client';

import { useState, useEffect } from 'react';
import { X, Cookie } from 'lucide-react';

export default function CookieConsent() {
    const [showBanner, setShowBanner] = useState(false);

    useEffect(() => {
        const consent = localStorage.getItem('cookie-consent-accepted');
        if (!consent) {
            setShowBanner(true);
        }
    }, []);

    const acceptCookies = () => {
        localStorage.setItem('cookie-consent-accepted', 'true');
        setShowBanner(false);
    };

    if (!showBanner) return null;

    return (
        <div className="fixed bottom-4 right-4 left-4 md:left-auto md:w-[400px] z-50 animate-in slide-in-from-bottom-4 duration-500">
            <div className="bg-secondarybg backdrop-blur-md border border-border p-5 rounded-xl shadow-2xl flex flex-col gap-4">
                <div className="flex items-start justify-between">
                    <div className="flex items-center gap-3 text-primary">
                        <Cookie className="h-6 w-6" />
                        <span className="font-bold text-lg">Pliki Cookies</span>
                    </div>
                    <button
                        onClick={acceptCookies}
                        className="text-txtcolor-300 hover:text-foreground transition-colors"
                    >
                        <X className="h-5 w-5" />
                    </button>
                </div>

                <p className="text-sm text-txtcolor-300 leading-relaxed">
                    Ta strona używa plików cookies wyłącznie w celu zapewnienia poprawnego działania mechanizmu logowania i bezpieczeństwa sesji. Korzystając ze strony, wyrażasz na to zgodę.
                </p>

                <div className="flex gap-3 mt-2">
                    <button
                        onClick={acceptCookies}
                        className="flex-1 bg-primary text-darkgray font-bold py-2 px-4 rounded-lg hover:opacity-90 transition-opacity text-sm"
                    >
                        Rozumiem, wchodzę
                    </button>
                </div>
            </div>
        </div>
    );
}