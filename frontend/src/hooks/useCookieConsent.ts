import { useState, useEffect } from 'react';

export interface CookieConsentState {
  necessary: boolean; // Always true
  analytics: boolean;
  marketing: boolean;
  version: string;
}

const CONSENT_KEY = 'flywheel_cookie_consent';
const CURRENT_VERSION = '1.0';

export function useCookieConsent() {
  const [consent, setConsent] = useState<CookieConsentState | null>(null);
  const [hasInteracted, setHasInteracted] = useState<boolean>(true); // Default true to avoid flash on SSR/Hydration

  useEffect(() => {
    // Only run on client
    const stored = localStorage.getItem(CONSENT_KEY);
    if (stored) {
      try {
        const parsed = JSON.parse(stored) as CookieConsentState;
        // Check version to invalidate old consents if needed
        if (parsed.version === CURRENT_VERSION) {
          setConsent(parsed);
          setHasInteracted(true);
        } else {
          setHasInteracted(false);
        }
      } catch (e) {
        setHasInteracted(false);
      }
    } else {
      setHasInteracted(false);
      setConsent({
        necessary: true,
        analytics: false,
        marketing: false,
        version: CURRENT_VERSION
      });
    }
  }, []);

  const saveConsent = (newConsent: Partial<CookieConsentState>) => {
    const updatedConsent: CookieConsentState = {
      necessary: true,
      analytics: newConsent.analytics ?? false,
      marketing: newConsent.marketing ?? false,
      version: CURRENT_VERSION
    };
    
    setConsent(updatedConsent);
    setHasInteracted(true);
    localStorage.setItem(CONSENT_KEY, JSON.stringify(updatedConsent));

    // Here you would trigger an event for GTM or other tag managers if needed
    // window.dispatchEvent(new CustomEvent('cookieConsentChanged', { detail: updatedConsent }));
  };

  const acceptAll = () => {
    saveConsent({ analytics: true, marketing: true });
  };

  const rejectAll = () => {
    saveConsent({ analytics: false, marketing: false });
  };

  return {
    consent,
    hasInteracted,
    saveConsent,
    acceptAll,
    rejectAll
  };
}
