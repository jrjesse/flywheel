import { useState, useEffect } from "react";
import { apiFetch } from "@/lib/api";
import { Key, Smartphone, ShieldAlert, Lock, CheckCircle2, Loader2, Info, Clock, Users, Copy, Check, X } from "lucide-react";

export function WhatsAppSettingsModal({ isOpen, onClose }: { isOpen: boolean; onClose: () => void }) {
  const [clientId] = useState("00000000-0000-0000-0000-000000000001");
  const [config, setConfig] = useState({
    accessToken: "",
    phoneNumberId: "",
    wabaId: "",
    appSecret: "",
    verifyToken: "VT_" + Math.random().toString(36).substr(2, 9),
    debounceSeconds: 45,
    slaMinutes: 5,
    maxCapacity: 5
  });

  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [testStatus, setTestStatus] = useState<'idle' | 'testing' | 'success' | 'error'>('idle');
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setIsLoading(true);
      apiFetch(`/api/settings/whatsapp/${clientId}`)
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
    const { name, value } = e.target;
    setConfig(prev => ({ ...prev, [name]: value }));
  };

  const handleSave = async () => {
    setIsSaving(true);
    try {
      await apiFetch(`/api/settings/whatsapp/${clientId}`, {
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
      const res = await apiFetch(`/api/settings/whatsapp/${clientId}/test`, {
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
    navigator.clipboard.writeText(`https://api.suasolucao.com/api/queue/webhook/${clientId}`);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col">
        {/* Header */}
        <div className="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50">
          <div>
            <h2 className="text-xl font-bold text-slate-800">WhatsApp Oficial</h2>
            <p className="text-sm text-slate-500">Configure as credenciais e regras de fila.</p>
          </div>
          <button onClick={onClose} className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-200 rounded-full transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto p-6">
          {isLoading ? (
            <div className="flex justify-center items-center h-32 text-slate-500">
              <Loader2 className="w-8 h-8 animate-spin" />
            </div>
          ) : (
            <div className="space-y-8">
              <div className="bg-sky-50 border border-sky-200 rounded-xl p-5 flex gap-4">
                <Info className="w-6 h-6 text-sky-600 flex-shrink-0" />
                <div className="text-sm text-sky-800 space-y-2">
                  <h4 className="font-bold text-base">Recomendação: Login via Facebook</h4>
                  <p><strong>Simplicidade:</strong> Configuração instantânea via fluxo oficial da Meta. Sem necessidade de conhecimentos técnicos.</p>
                  <p><strong>Segurança:</strong> Conexão nativa que oferece maior qualidade ao número e proteção contra bloqueios.</p>
                  <p className="text-xs mt-3 opacity-80"><strong>Nota:</strong> A configuração manual exige conhecimentos em desenvolvimento e criação de apps no Meta Developers, sendo mais suscetível a instabilidades de conexão.</p>
                </div>
              </div>

              <section className="bg-white rounded-xl border border-slate-200 overflow-hidden">
                <div className="p-4 border-b border-slate-100 bg-slate-50">
                  <h3 className="font-semibold text-slate-800 flex items-center gap-2">
                    <ShieldAlert className="w-4 h-4 text-emerald-500" />
                    Credenciais Meta API
                  </h3>
                </div>
                <div className="p-5 space-y-4">
                  <InputField label="Permanent Access Token" name="accessToken" type="password" value={config.accessToken} onChange={handleChange} icon={<Key className="w-4 h-4 text-slate-400" />} />
                  <div className="grid grid-cols-2 gap-4">
                    <InputField label="Phone Number ID" name="phoneNumberId" value={config.phoneNumberId} onChange={handleChange} icon={<Smartphone className="w-4 h-4 text-slate-400" />} />
                    <InputField label="WhatsApp Business ID (WABA ID)" name="wabaId" value={config.wabaId} onChange={handleChange} />
                  </div>
                  <div className="grid grid-cols-2 gap-4">
                    <InputField label="App Secret" name="appSecret" type="password" value={config.appSecret} onChange={handleChange} icon={<Lock className="w-4 h-4 text-slate-400" />} />
                    <InputField label="Webhook Verify Token" name="verifyToken" value={config.verifyToken} onChange={handleChange} />
                  </div>
                  <div className="pt-4 flex items-center gap-4 border-t border-slate-100 mt-4">
                    <button onClick={testConnection} disabled={testStatus === 'testing' || !config.accessToken} className="px-4 py-2 bg-slate-900 text-white text-sm font-medium rounded-lg hover:bg-slate-800 transition-colors flex items-center gap-2 disabled:opacity-50">
                      {testStatus === 'testing' && <Loader2 className="w-4 h-4 animate-spin" />}
                      Testar Conexão
                    </button>
                    {testStatus === 'success' && <span className="text-sm font-medium text-emerald-600 flex items-center gap-1"><CheckCircle2 className="w-4 h-4" /> Conexão OK</span>}
                    {testStatus === 'error' && <span className="text-sm font-medium text-rose-600 flex items-center gap-1"><ShieldAlert className="w-4 h-4" /> Falha na Conexão</span>}
                  </div>
                </div>
              </section>

              <div className="grid grid-cols-2 gap-6">
                <section className="bg-white rounded-xl border border-slate-200 overflow-hidden">
                  <div className="p-4 border-b border-slate-100 bg-slate-50">
                    <h3 className="font-semibold text-slate-800 flex items-center gap-2">
                      <Clock className="w-4 h-4 text-amber-500" />
                      Regras da Fila
                    </h3>
                  </div>
                  <div className="p-5 space-y-4">
                    <div>
                      <label className="text-sm font-medium text-slate-700">Debounce (s)</label>
                      <input type="number" name="debounceSeconds" value={config.debounceSeconds} onChange={handleChange} className="w-full mt-1 px-3 py-2 border border-slate-200 rounded-lg text-sm" />
                    </div>
                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="text-sm font-medium text-slate-700">SLA (min)</label>
                        <input type="number" name="slaMinutes" value={config.slaMinutes} onChange={handleChange} className="w-full mt-1 px-3 py-2 border border-slate-200 rounded-lg text-sm" />
                      </div>
                      <div>
                        <label className="text-sm font-medium text-slate-700">Max Capacidade</label>
                        <input type="number" name="maxCapacity" value={config.maxCapacity} onChange={handleChange} className="w-full mt-1 px-3 py-2 border border-slate-200 rounded-lg text-sm" />
                      </div>
                    </div>
                  </div>
                </section>

                <section className="bg-slate-900 rounded-xl overflow-hidden text-white">
                  <div className="p-4 border-b border-slate-800">
                    <h3 className="font-semibold flex items-center gap-2">Webhook URL</h3>
                  </div>
                  <div className="p-5 space-y-4">
                    <div className="bg-black/50 p-3 rounded-lg border border-slate-800 break-all">
                      <code className="text-xs text-emerald-400 font-mono">https://api.suasolucao.com/api/queue/webhook/{clientId}</code>
                    </div>
                    <button onClick={copyWebhook} className="w-full py-2 bg-slate-800 hover:bg-slate-700 rounded-lg text-sm font-medium transition-colors flex justify-center items-center gap-2">
                      {copied ? <Check className="w-4 h-4 text-emerald-500" /> : <Copy className="w-4 h-4" />}
                      {copied ? 'Copiado!' : 'Copiar URL'}
                    </button>
                  </div>
                </section>
              </div>
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="p-6 border-t border-slate-100 bg-white flex justify-end gap-3">
          <button onClick={onClose} className="px-5 py-2.5 text-slate-600 font-medium hover:bg-slate-100 rounded-xl transition-colors">
            Cancelar
          </button>
          <button onClick={handleSave} disabled={isSaving} className="px-5 py-2.5 bg-emerald-600 hover:bg-emerald-700 text-white font-medium rounded-xl shadow-lg shadow-emerald-200 flex items-center gap-2 transition-all">
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
        <input type={type} name={name} value={value || ''} onChange={onChange} className={`w-full ${icon ? 'pl-10' : 'pl-3'} pr-3 py-2 border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 outline-none`} />
      </div>
    </div>
  );
}
