'use client';

import React, { useState, useEffect } from 'react';
import { useCookieConsent, CookieConsentState } from '@/hooks/useCookieConsent';
import { Shield, X, Check, Settings2 } from 'lucide-react';

export function CookieConsent() {
  const { consent, hasInteracted, acceptAll, rejectAll, saveConsent } = useCookieConsent();
  const [showPreferences, setShowPreferences] = useState(false);
  const [localConsent, setLocalConsent] = useState<Partial<CookieConsentState>>({
    analytics: false,
    marketing: false
  });
  
  // To avoid hydration mismatch
  const [mounted, setMounted] = useState(false);
  
  useEffect(() => {
    setMounted(true);
  }, []);

  useEffect(() => {
    if (consent) {
      setLocalConsent({
        analytics: consent.analytics,
        marketing: consent.marketing
      });
    }
  }, [consent]);

  if (!mounted || hasInteracted) return null;

  const handleSavePreferences = () => {
    saveConsent(localConsent);
    setShowPreferences(false);
  };

  const togglePreference = (key: 'analytics' | 'marketing') => {
    setLocalConsent(prev => ({ ...prev, [key]: !prev[key] }));
  };

  return (
    <>
      {/* Main Banner */}
      {!showPreferences && (
        <div className="fixed bottom-0 left-0 right-0 z-50 p-4 pointer-events-none sm:p-6 md:p-8 flex justify-center">
          <div className="pointer-events-auto w-full max-w-4xl bg-white border border-slate-200 rounded-xl p-6 flex flex-col md:flex-row gap-6 items-start md:items-center justify-between transition-all duration-300 transform translate-y-0">
            <div className="flex-1 flex gap-4">
              <div className="bg-blue-50 text-blue-600 p-3 rounded-lg h-fit">
                <Shield className="w-6 h-6" />
              </div>
              <div className="flex flex-col gap-1">
                <h3 className="text-base font-semibold text-slate-900">Privacidade e Cookies</h3>
                <p className="text-sm text-slate-600 leading-relaxed">
                  Utilizamos cookies para melhorar sua experiência em nossa plataforma, analisar o tráfego e personalizar conteúdo. 
                  Ao clicar em "Aceitar todos", você concorda com o uso de cookies de acordo com nossa Política de Privacidade (LGPD).
                </p>
              </div>
            </div>
            <div className="flex flex-wrap gap-3 w-full md:w-auto mt-4 md:mt-0">
              <button 
                onClick={() => setShowPreferences(true)}
                className="flex-1 md:flex-none px-4 py-2.5 text-sm font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg transition-colors duration-200 whitespace-nowrap"
              >
                Preferências
              </button>
              <button 
                onClick={rejectAll}
                className="flex-1 md:flex-none px-4 py-2.5 text-sm font-medium text-slate-700 border border-slate-200 hover:bg-slate-50 rounded-lg transition-colors duration-200 whitespace-nowrap"
              >
                Rejeitar Opcionais
              </button>
              <button 
                onClick={acceptAll}
                className="flex-1 md:flex-none px-6 py-2.5 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors duration-200 whitespace-nowrap"
              >
                Aceitar Todos
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Preferences Modal */}
      {showPreferences && (
        <div className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm">
          <div className="w-full max-w-lg bg-white rounded-xl shadow-none border border-slate-200 overflow-hidden flex flex-col max-h-[90vh]">
            
            <div className="flex items-center justify-between p-5 border-b border-slate-100">
              <div className="flex items-center gap-3">
                <Settings2 className="w-5 h-5 text-slate-700" />
                <h2 className="text-lg font-semibold text-slate-900">Preferências de Cookies</h2>
              </div>
              <button 
                onClick={() => setShowPreferences(false)}
                className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition-colors"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto p-5 space-y-6">
              
              {/* Strictly Necessary */}
              <div className="flex gap-4">
                <div className="flex-1">
                  <h4 className="text-sm font-semibold text-slate-900 mb-1">Estritamente Necessários</h4>
                  <p className="text-sm text-slate-600">Essenciais para o funcionamento da plataforma. Não podem ser desativados.</p>
                </div>
                <div className="pt-1">
                  <div className="w-11 h-6 bg-blue-600 rounded-full relative cursor-not-allowed opacity-70">
                    <div className="absolute right-1 top-1 bg-white w-4 h-4 rounded-full flex items-center justify-center">
                       <Check className="w-3 h-3 text-blue-600" />
                    </div>
                  </div>
                </div>
              </div>

              {/* Analytics */}
              <div className="flex gap-4 border-t border-slate-100 pt-5">
                <div className="flex-1">
                  <h4 className="text-sm font-semibold text-slate-900 mb-1">Cookies Analíticos</h4>
                  <p className="text-sm text-slate-600">Ajudam-nos a entender como os visitantes interagem com o site, fornecendo informações anonimizadas sobre métricas.</p>
                </div>
                <div className="pt-1">
                  <button 
                    onClick={() => togglePreference('analytics')}
                    className={`w-11 h-6 rounded-full relative transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 ${localConsent.analytics ? 'bg-blue-600' : 'bg-slate-200'}`}
                  >
                    <div className={`absolute top-1 bg-white w-4 h-4 rounded-full transition-transform duration-200 ${localConsent.analytics ? 'translate-x-6' : 'translate-x-1'}`} />
                  </button>
                </div>
              </div>

              {/* Marketing */}
              <div className="flex gap-4 border-t border-slate-100 pt-5">
                <div className="flex-1">
                  <h4 className="text-sm font-semibold text-slate-900 mb-1">Cookies de Marketing</h4>
                  <p className="text-sm text-slate-600">Usados para rastrear visitantes em sites. A intenção é exibir anúncios relevantes e envolventes.</p>
                </div>
                <div className="pt-1">
                  <button 
                    onClick={() => togglePreference('marketing')}
                    className={`w-11 h-6 rounded-full relative transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 ${localConsent.marketing ? 'bg-blue-600' : 'bg-slate-200'}`}
                  >
                    <div className={`absolute top-1 bg-white w-4 h-4 rounded-full transition-transform duration-200 ${localConsent.marketing ? 'translate-x-6' : 'translate-x-1'}`} />
                  </button>
                </div>
              </div>

            </div>

            <div className="p-5 border-t border-slate-100 bg-slate-50 flex justify-end gap-3">
              <button 
                onClick={handleSavePreferences}
                className="px-5 py-2.5 text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 rounded-lg transition-colors duration-200"
              >
                Salvar Preferências
              </button>
            </div>
            
          </div>
        </div>
      )}
    </>
  );
}
