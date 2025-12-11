import { useState, useEffect } from 'react';

export const useCookieConsent = () => {
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

    return {
        showBanner,
        acceptCookies,
    };
};
