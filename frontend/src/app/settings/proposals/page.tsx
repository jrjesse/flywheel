"use client";
import { apiFetch } from "@/lib/api";

import { useState, useEffect, useRef } from "react";
import { 
  Key, 
  Mail, 
  ShieldAlert, 
  CheckCircle2, 
  Loader2, 
  Info,
  FileText,
  Upload,
  File
} from "lucide-react";

export default function ProposalsSettings() {
  const [config, setConfig] = useState({
    smtpHost: "",
    smtpPort: 1025,
    smtpUsername: "",
    smtpPassword: "",
    smtpAuth: true,
    smtpTls: true,
    templateFilePath: ""
  });

  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [uploadStatus, setUploadStatus] = useState<'idle' | 'uploading' | 'success' | 'error'>('idle');
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    apiFetch<Record<string, unknown>>(`/api/settings/proposals`)
      .then((data) => {
        if (data) {
          setConfig((prev) => ({ ...prev, ...data }));
        }
        setIsLoading(false);
      })
      .catch(console.error);
  }, []);

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const handleChange = (e: any) => {
    const { name, value, type, checked } = e.target;
    setConfig(prev => ({ 
      ...prev, 
      [name]: type === 'checkbox' ? checked : value 
    }));
  };

  const handleSave = async () => {
    setIsSaving(true);
    try {
      await apiFetch(`/api/settings/proposals`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(config)
      });
    } finally {
      setIsSaving(false);
    }
  };

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const handleFileUpload = async (e: any) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    setUploadStatus('uploading');
    try {
      const data = await apiFetch<{ templateFilePath: string }>(`/api/settings/proposals/template`, {
        method: "POST",
        body: formData,
      });
      setConfig((prev) => ({ ...prev, templateFilePath: data.templateFilePath }));
      setUploadStatus("success");
      setTimeout(() => setUploadStatus("idle"), 3000);
    } catch (err) {
      console.error(err);
      setUploadStatus("error");
    }
  };

  if (isLoading) return <div className="p-8">Carregando configurações...</div>;

  return (
    <div className="flex-1 p-8 bg-slate-50 min-h-screen overflow-y-auto">
      <div className="max-w-4xl mx-auto space-y-8">
        
        {/* Header */}
        <div>
          <h1 className="text-3xl font-bold text-slate-900 tracking-tight">Propostas Comerciais</h1>
          <p className="text-slate-500 mt-1">Configure as credenciais de envio de e-mail e o template PDF padrão das propostas.</p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          
          <div className="lg:col-span-2 space-y-6">
            {/* Section 1: Email / SMTP Credentials */}
            <section className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
              <div className="p-5 border-b border-slate-100 bg-slate-50/50">
                <h2 className="font-semibold text-slate-800 flex items-center gap-2">
                  <Mail className="w-5 h-5 text-indigo-500" />
                  Servidor SMTP (E-mail)
                </h2>
              </div>
              <div className="p-5 space-y-4">
                
                <div className="grid grid-cols-2 gap-4">
                  <InputField 
                    label="Host (Servidor SMTP)" 
                    name="smtpHost" 
                    value={config.smtpHost} 
                    onChange={handleChange}
                    tooltip="Ex: smtp.gmail.com ou smtp.sendgrid.net"
                  />
                  <InputField 
                    label="Porta" 
                    name="smtpPort" 
                    type="number"
                    value={config.smtpPort} 
                    onChange={handleChange}
                    tooltip="Ex: 587 (TLS) ou 465 (SSL) ou 1025 (MailHog/Local)"
                  />
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <InputField 
                    label="Usuário / E-mail Remetente" 
                    name="smtpUsername" 
                    value={config.smtpUsername} 
                    onChange={handleChange}
                    tooltip="Endereço de e-mail que enviará a proposta"
                  />
                  <InputField 
                    label="Senha / Token" 
                    name="smtpPassword" 
                    type="password" 
                    value={config.smtpPassword} 
                    onChange={handleChange}
                    icon={<Key className="w-4 h-4 text-slate-400" />}
                    tooltip="Senha de aplicativo ou API Key do provedor"
                  />
                </div>

                <div className="flex gap-6 pt-2">
                  <label className="flex items-center gap-2 text-sm text-slate-700 cursor-pointer">
                    <input 
                      type="checkbox" 
                      name="smtpAuth" 
                      checked={config.smtpAuth} 
                      onChange={handleChange}
                      className="rounded border-slate-300 text-indigo-600 focus:ring-indigo-500"
                    />
                    Requer Autenticação
                  </label>
                  <label className="flex items-center gap-2 text-sm text-slate-700 cursor-pointer">
                    <input 
                      type="checkbox" 
                      name="smtpTls" 
                      checked={config.smtpTls} 
                      onChange={handleChange}
                      className="rounded border-slate-300 text-indigo-600 focus:ring-indigo-500"
                    />
                    Usar TLS/SSL
                  </label>
                </div>

              </div>
            </section>

            {/* Section 2: PDF Template Upload */}
            <section className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
              <div className="p-5 border-b border-slate-100 bg-slate-50/50">
                <h2 className="font-semibold text-slate-800 flex items-center gap-2">
                  <FileText className="w-5 h-5 text-indigo-500" />
                  Template da Proposta (PDF)
                </h2>
              </div>
              <div className="p-5 space-y-6">
                
                <div className="bg-indigo-50 p-4 rounded-xl border border-indigo-100">
                  <h3 className="font-semibold text-indigo-800 flex items-center gap-2 mb-2">
                    <Info className="w-4 h-4" /> Instruções de Configuração
                  </h3>
                  <p className="text-sm text-indigo-700 leading-relaxed">
                    Abrir o PDF base da empresa em um editor de formulários (como Adobe Acrobat, Docfly, etc.), criar os campos de texto preenchíveis exatamente com os nomes:
                  </p>
                  <ul className="list-disc list-inside text-sm text-indigo-800 font-medium mt-2 space-y-1">
                    <li><code className="bg-white px-1.5 py-0.5 rounded text-xs">ClientName</code> - Para o nome do cliente</li>
                    <li><code className="bg-white px-1.5 py-0.5 rounded text-xs">CurrentDate</code> - Para a data de emissão</li>
                    <li><code className="bg-white px-1.5 py-0.5 rounded text-xs">ProposalValue</code> - Para o valor total</li>
                  </ul>
                  <p className="text-sm text-indigo-700 leading-relaxed mt-2">
                    Após criar os campos, faça o upload do arquivo aqui. O sistema vai reconhecer automaticamente e preencher os campos certinhos.
                  </p>
                </div>

                <div className="border-2 border-dashed border-slate-300 rounded-xl p-8 flex flex-col items-center justify-center text-center hover:bg-slate-50 transition-colors">
                  <input 
                    type="file" 
                    accept="application/pdf"
                    ref={fileInputRef}
                    className="hidden"
                    onChange={handleFileUpload}
                  />
                  
                  <div className="w-12 h-12 rounded-full bg-slate-100 flex items-center justify-center mb-4">
                    {uploadStatus === 'uploading' ? (
                      <Loader2 className="w-6 h-6 text-indigo-500 animate-spin" />
                    ) : uploadStatus === 'success' ? (
                      <CheckCircle2 className="w-6 h-6 text-emerald-500" />
                    ) : (
                      <Upload className="w-6 h-6 text-slate-400" />
                    )}
                  </div>
                  
                  <p className="text-slate-700 font-medium mb-1">
                    Arraste o seu arquivo PDF aqui ou
                  </p>
                  <button 
                    onClick={() => fileInputRef.current?.click()}
                    className="text-indigo-600 font-medium hover:text-indigo-700 transition-colors"
                  >
                    selecione no seu computador
                  </button>
                  
                  {config.templateFilePath && (
                    <div className="mt-4 pt-4 border-t border-slate-200 w-full flex flex-col items-center">
                      <p className="text-xs text-slate-400 mb-2">Template atualmente em uso:</p>
                      <div className="flex items-center gap-2 bg-emerald-50 text-emerald-700 px-3 py-1.5 rounded-lg text-sm font-medium border border-emerald-200 max-w-full overflow-hidden">
                        <File className="w-4 h-4 shrink-0" />
                        <span className="truncate">{config.templateFilePath.split('/').pop() || config.templateFilePath.split('\\').pop()}</span>
                      </div>
                    </div>
                  )}

                  {uploadStatus === 'error' && (
                    <p className="text-rose-500 text-sm mt-3 flex items-center gap-1">
                      <ShieldAlert className="w-4 h-4" /> Erro ao enviar o arquivo.
                    </p>
                  )}
                </div>

              </div>
            </section>
          </div>

          <div className="space-y-6">
            {/* Save Action */}
            <div className="bg-white rounded-2xl border border-slate-200 p-5 shadow-sm">
              <h3 className="font-medium text-slate-800 mb-4">Ações</h3>
              <button 
                onClick={handleSave}
                disabled={isSaving}
                className="w-full py-3 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold rounded-xl shadow-lg shadow-indigo-200 transition-all flex justify-center items-center gap-2 disabled:opacity-70"
              >
                {isSaving ? <Loader2 className="w-5 h-5 animate-spin" /> : <CheckCircle2 className="w-5 h-5" />}
                Salvar Configurações
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

// Subcomponents
// eslint-disable-next-line @typescript-eslint/no-explicit-any
function InputField({ label, name, type = "text", value, onChange, icon, tooltip }: any) {
  return (
    <div>
      <label className="text-sm font-medium text-slate-700 mb-1 flex items-center gap-2">
        {label}
        {tooltip && <Tooltip text={tooltip} />}
      </label>
      <div className="relative">
        {icon && (
          <div className="absolute left-3 top-2.5">
            {icon}
          </div>
        )}
        <input 
          type={type} 
          name={name}
          value={value || ''} 
          onChange={onChange}
          className={`w-full ${icon ? 'pl-10' : 'pl-3'} pr-3 py-2 bg-slate-50 border border-slate-200 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 outline-none transition-shadow`}
          placeholder="..."
        />
      </div>
    </div>
  );
}

function Tooltip({ text }: { text: string }) {
  return (
    <div className="group relative flex items-center">
      <Info className="w-4 h-4 text-slate-400 cursor-help" />
      <div className="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 w-48 p-2 bg-slate-800 text-white text-xs rounded-lg opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-10 shadow-xl">
        {text}
        <div className="absolute top-full left-1/2 -translate-x-1/2 border-4 border-transparent border-t-slate-800"></div>
      </div>
    </div>
  );
}
