import { useState, useEffect } from "react";
import { apiFetch } from "@/lib/api";
import { Key, ShieldAlert, Lock, CheckCircle2, Loader2, Copy, Check, X, Camera } from "lucide-react";

export function InstagramSettingsModal({ isOpen, onClose }: { isOpen: boolean; onClose: () => void }) {
  const [clientId] = useState("00000000-0000-0000-0000-000000000001");
  const [config, setConfig] = useState({
    accessToken: "",
    instagramAccountId: "",
    pageId: "",
    appSecret: "",
    verifyToken: "VT_" + Math.random().toString(36).substr(2, 9),
    active: true
  });

  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [testStatus, setTestStatus] = useState<'idle' | 'testing' | 'success' | 'error'>('idle');
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setIsLoading(true);
      apiFetch<any>(`/api/settings/instagram/${clientId}`)
        .then((data) => {
          if (data.id) setConfig(data);
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
      await apiFetch(`/api/settings/instagram/${clientId}`, {
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
      await apiFetch(`/api/settings/instagram/${clientId}/test`, {
        method: "POST",
        body: JSON.stringify(config),
      });
      setTestStatus("success");
    } catch {
      setTestStatus('error');
    }
  };

  const copyWebhook = () => {
    navigator.clipboard.writeText(`https://api.suasolucao.com/api/queue/webhook/instagram/${clientId}`);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col">
        {/* Header */}
        <div className="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-lg bg-pink-100 flex items-center justify-center text-pink-600">
              <Camera className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-xl font-bold text-slate-800">Instagram Direct</h2>
              <p className="text-sm text-slate-500">Credenciais para envio e recebimento de DMs.</p>
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
                <input type="checkbox" id="active" name="active" checked={config.active} onChange={handleChange} className="w-4 h-4 text-pink-600 rounded" />
                <label htmlFor="active" className="text-sm font-medium text-slate-700">Ativar Integração</label>
              </div>

              <div className="bg-blue-50 border border-blue-200 rounded-xl p-6 mb-6 flex flex-col items-center text-center">
                <h3 className="font-bold text-slate-800 mb-2">Conectar com Facebook (Recomendado)</h3>
                <p className="text-sm text-slate-600 mb-4 max-w-sm">A forma mais rápida e segura de integrar sua conta do Instagram Direct. Não requer conhecimentos técnicos.</p>
                <a href="https://facebook.com/login.php" target="_blank" rel="noopener noreferrer" className="px-6 py-2.5 bg-[#1877F2] hover:bg-[#166FE5] text-white font-semibold rounded-lg shadow-md transition-colors flex items-center gap-2">
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                    <path fillRule="evenodd" d="M22 12c0-5.523-4.477-10-10-10S2 6.477 2 12c0 4.991 3.657 9.128 8.438 9.878v-6.987h-2.54V12h2.54V9.797c0-2.506 1.492-3.89 3.777-3.89 1.094 0 2.238.195 2.238.195v2.46h-1.26c-1.243 0-1.63.771-1.63 1.562V12h2.773l-.443 2.89h-2.33v6.988C18.343 21.128 22 16.991 22 12z" clipRule="evenodd" />
                  </svg>
                  Login com Facebook
                </a>
              </div>

              <div className="space-y-4">
                <InputField label="Permanent Access Token" name="accessToken" type="password" value={config.accessToken} onChange={handleChange} icon={<Key className="w-4 h-4 text-slate-400" />} />
                <div className="grid grid-cols-2 gap-4">
                  <InputField label="Instagram Account ID" name="instagramAccountId" value={config.instagramAccountId} onChange={handleChange} />
                  <InputField label="Facebook Page ID" name="pageId" value={config.pageId} onChange={handleChange} />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <InputField label="App Secret" name="appSecret" type="password" value={config.appSecret} onChange={handleChange} icon={<Lock className="w-4 h-4 text-slate-400" />} />
                  <InputField label="Webhook Verify Token" name="verifyToken" value={config.verifyToken} onChange={handleChange} />
                </div>
              </div>

              <div className="pt-4 flex justify-between items-center border-t border-slate-100">
                <div className="flex gap-4 items-center">
                  <button onClick={testConnection} disabled={testStatus === 'testing' || !config.accessToken} className="px-4 py-2 bg-slate-900 text-white text-sm font-medium rounded-lg hover:bg-slate-800 transition-colors flex items-center gap-2 disabled:opacity-50">
                    {testStatus === 'testing' && <Loader2 className="w-4 h-4 animate-spin" />}
                    Testar Conexão
                  </button>
                  {testStatus === 'success' && <span className="text-sm font-medium text-emerald-600 flex items-center gap-1"><CheckCircle2 className="w-4 h-4" /> Conexão OK</span>}
                  {testStatus === 'error' && <span className="text-sm font-medium text-rose-600 flex items-center gap-1"><ShieldAlert className="w-4 h-4" /> Falha</span>}
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
                  <code className="text-xs text-emerald-400 font-mono break-all">https://api.suasolucao.com/api/queue/webhook/instagram/{clientId}</code>
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
          <button onClick={handleSave} disabled={isSaving} className="px-5 py-2.5 bg-pink-600 hover:bg-pink-700 text-white font-medium rounded-xl shadow-lg shadow-pink-200 flex items-center gap-2 transition-all">
            {isSaving ? <Loader2 className="w-5 h-5 animate-spin" /> : <CheckCircle2 className="w-5 h-5" />}
            Salvar e Ativar
          </button>
        </div>
      </div>
    </div>
  );
}

interface InputFieldProps {
  label: string;
  name: string;
  type?: string;
  value: string | number;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  icon?: React.ReactNode;
}

function InputField({ label, name, type = "text", value, onChange, icon }: InputFieldProps) {
  return (
    <div>
      <label className="text-sm font-medium text-slate-700 mb-1 flex items-center gap-2">{label}</label>
      <div className="relative">
        {icon && <div className="absolute left-3 top-2.5">{icon}</div>}
        <input type={type} name={name} value={value || ''} onChange={onChange} className={`w-full ${icon ? 'pl-10' : 'pl-3'} pr-3 py-2 border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-pink-500 outline-none`} />
      </div>
    </div>
  );
}
