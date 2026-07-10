import { useState, useEffect } from "react";
import { apiFetch } from "@/lib/api";
import { ShieldAlert, CheckCircle2, Loader2, Copy, Check, X, FileSpreadsheet, Key } from "lucide-react";

export function GoogleFormsSettingsModal({ isOpen, onClose }: { isOpen: boolean; onClose: () => void }) {
  const [clientId] = useState("00000000-0000-0000-0000-000000000001");
  const [config, setConfig] = useState({
    webhookToken: "GF_" + Math.random().toString(36).substr(2, 9),
    formName: "",
    active: true
  });

  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [testStatus, setTestStatus] = useState<'idle' | 'testing' | 'success' | 'error'>('idle');
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setIsLoading(true);
      apiFetch(`/api/settings/google-forms/${clientId}`)
        .then(res => res.json())
        .then(data => {
          if(data.id) setConfig(data);
          setIsLoading(false);
        })
        .catch(() => setIsLoading(false));
    }
  }, [clientId, isOpen]);

  if (!isOpen) return null;

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setConfig(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleSave = async () => {
    setIsSaving(true);
    try {
      await apiFetch(`/api/settings/google-forms/${clientId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(config)
      });
      onClose();
    } finally {
      setIsSaving(false);
    }
  };

  const testConnection = async () => {
    setTestStatus('testing');
    try {
      const res = await apiFetch(`/api/settings/google-forms/${clientId}/test`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(config)
      });
      if (res.ok) setTestStatus('success');
      else setTestStatus('error');
    } catch {
      setTestStatus('error');
    }
  };

  const copyWebhook = () => {
    navigator.clipboard.writeText(`https://api.suasolucao.com/api/queue/webhook/google-forms/${clientId}?token=${config.webhookToken}`);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const generateNewToken = () => {
    setConfig(prev => ({ ...prev, webhookToken: "GF_" + Math.random().toString(36).substr(2, 12) }));
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col">
        {/* Header */}
        <div className="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-indigo-100 flex items-center justify-center text-indigo-600">
              <FileSpreadsheet className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-slate-800">Google Forms</h2>
              <p className="text-sm text-slate-500">Receba leads de formulários via Webhook.</p>
            </div>
          </div>
          <button onClick={onClose} className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-200 rounded-full transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          {isLoading ? (
            <div className="flex justify-center items-center h-32 text-slate-500">
              <Loader2 className="w-8 h-8 animate-spin" />
            </div>
          ) : (
            <>
              <div className="flex items-center gap-2 mb-2">
                <input type="checkbox" id="active" name="active" checked={config.active} onChange={handleChange} className="w-4 h-4 text-indigo-600 rounded" />
                <label htmlFor="active" className="text-sm font-medium text-slate-700">Ativar Integração</label>
              </div>

              <div className="space-y-4">
                <div>
                  <label className="text-sm font-medium text-slate-700 mb-1 flex items-center gap-2">Nome de Referência (Opcional)</label>
                  <input type="text" name="formName" value={config.formName || ''} onChange={handleChange} placeholder="Ex: Formulário de Contato Site" className="w-full px-3 py-2 border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none" />
                </div>

                <div>
                  <label className="text-sm font-medium text-slate-700 mb-1 flex items-center justify-between">
                    <span className="flex items-center gap-2"><Key className="w-4 h-4" /> Webhook Token (Segurança)</span>
                    <button onClick={generateNewToken} className="text-xs text-indigo-600 hover:underline">Gerar Novo</button>
                  </label>
                  <input type="text" name="webhookToken" value={config.webhookToken || ''} readOnly className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm outline-none text-slate-600 font-mono" />
                  <p className="text-xs text-slate-500 mt-1">Este token garante que apenas o seu formulário possa enviar dados para o seu CRM.</p>
                </div>
              </div>

              <div className="pt-4 flex justify-between items-center border-t border-slate-100">
                <div className="flex gap-4 items-center">
                  <button onClick={testConnection} disabled={testStatus === 'testing' || !config.webhookToken} className="px-4 py-2 bg-slate-900 text-white text-sm font-medium rounded-lg hover:bg-slate-800 transition-colors flex items-center gap-2 disabled:opacity-50">
                    {testStatus === 'testing' && <Loader2 className="w-4 h-4 animate-spin" />}
                    Validar Token
                  </button>
                  {testStatus === 'success' && <span className="text-sm font-medium text-emerald-600 flex items-center gap-1"><CheckCircle2 className="w-4 h-4" /> Token Válido</span>}
                  {testStatus === 'error' && <span className="text-sm font-medium text-rose-600 flex items-center gap-1"><ShieldAlert className="w-4 h-4" /> Inválido</span>}
                </div>
              </div>

              <section className="bg-slate-900 rounded-xl overflow-hidden text-white mt-6">
                <div className="p-4 border-b border-slate-800 flex justify-between items-center">
                  <h3 className="font-semibold text-sm">Webhook URL</h3>
                  <button onClick={copyWebhook} className="text-xs flex items-center gap-1 text-slate-400 hover:text-white transition-colors">
                    {copied ? <Check className="w-3 h-3 text-emerald-500" /> : <Copy className="w-3 h-3" />} Copiar
                  </button>
                </div>
                <div className="p-4 bg-black/50">
                  <p className="text-xs text-slate-400 mb-2">Configure um Google Apps Script no seu formulário para fazer um POST para esta URL:</p>
                  <code className="text-xs text-emerald-400 font-mono break-all">https://api.suasolucao.com/api/queue/webhook/google-forms/{clientId}?token={config.webhookToken}</code>
                </div>
              </section>
            </>
          )}
        </div>

        {/* Footer */}
        <div className="p-6 border-t border-slate-100 bg-white flex justify-end gap-3">
          <button onClick={onClose} className="px-5 py-2.5 text-slate-600 font-medium hover:bg-slate-100 rounded-xl transition-colors">
            Cancelar
          </button>
          <button onClick={handleSave} disabled={isSaving} className="px-5 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-xl shadow-lg shadow-indigo-200 flex items-center gap-2 transition-all">
            {isSaving ? <Loader2 className="w-5 h-5 animate-spin" /> : <CheckCircle2 className="w-5 h-5" />}
            Salvar e Ativar
          </button>
        </div>
      </div>
    </div>
  );
}
