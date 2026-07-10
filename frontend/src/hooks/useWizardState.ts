import { useState, useEffect } from 'react';

export function useWizardState<T>(key: string, initialValue: T) {
  const [state, setState] = useState<T>(() => {
    if (typeof window === 'undefined') return initialValue;
    try {
      const item = window.localStorage.getItem(key);
      return item ? JSON.parse(item) : initialValue;
    } catch (_) {
      return initialValue;
    }
  });

  useEffect(() => {
    try {
      window.localStorage.setItem(key, JSON.stringify(state));
    } catch (_) {}
  }, [key, state]);

  const clearState = () => {
    try {
      window.localStorage.removeItem(key);
    } catch (_) {}
    setState(initialValue);
  };

  return [state, setState, clearState] as const;
}
