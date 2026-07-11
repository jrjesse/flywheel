"use client";

import { useState, useEffect } from "react";
import { useWizardState } from "@/hooks/useWizardState";
import { applyDocumentMask, validateCPF, validateCNPJ } from "@/utils/documentValidation";
import { Building2, Users, CreditCard, ChevronRight, ChevronLeft, Check, Loader2, MessageSquare, Camera, Mail, FileText } from "lucide-react";
import { useRouter } from "next/navigation";
import { apiFetch } from "@/lib/api";
import { setAuth } from "@/lib/auth-storage";
import type { AuthResponse } from "@/lib/types";

export default function CompanyRegister() {
  const router = useRouter();
  const [mounted, setMounted] = useState(false);
  const [step, setStep] = useWizardState("wizard_step", 1);
  const [data, setData, clearData] = useWizardState("wizard_data", {
    name: "",
    documentType: "CNPJ",
    document: "",
    phone: "",
    niche: "",
    invitedMemberEmail: "",
    invitedMemberRole: "AGENT",
    planType: "STARTER",
    adminEmails: "",
    adminPassword: ""
  });

  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showContractModal, setShowContractModal] = useState(false);

  const nextStep = () => {
    setError("");
    if (step === 1) {
      if (!data.name) return setError("Nome da empresa é obrigatório.");
      if (!data.document) return setError("Documento é obrigatório.");
      if (data.documentType === 'CPF' && !validateCPF(data.document)) return setError("CPF inválido. Verifique se possui 11 números e se está correto.");
      if (data.documentType === 'CNPJ' && !validateCNPJ(data.document)) return setError("CNPJ inválido. Verifique se o dígito verificador está correto.");
      setStep(2);
    } else if (step === 2) {
      setStep(3);
    } else if (step === 3) {
      setStep(4);
    }
  };

  const prevStep = () => {
    setError("");
    setStep(step - 1);
  };

  const handleFinish = async () => {
    setIsSubmitting(true);
    setError("");
    try {
      const email = data.adminEmails.split(",")[0]?.trim();
      if (!email) {
        setError("Informe o email do administrador.");
        setIsSubmitting(false);
        return;
      }
      if (!data.adminPassword || data.adminPassword.length < 8) {
        setError("Senha do administrador deve ter no mínimo 8 caracteres.");
        setIsSubmitting(false);
        return;
      }

      const res = await apiFetch<AuthResponse>("/api/auth/register", {
        method: "POST",
        body: JSON.stringify({
          tenantName: data.name,
          document: data.document.replace(/\D/g, ""),
          documentType: data.documentType,
          email,
          password: data.adminPassword,
          displayName: data.name,
        }),
      });

      setAuth(res.token, {
        userId: res.userId,
        tenantId: res.tenantId,
        email: res.email,
        displayName: res.displayName,
        roles: [res.role],
      });

      clearData();
      localStorage.removeItem("wizard_step");
      if (res.webhookSecret) {
        sessionStorage.setItem("webhook_secret_once", res.webhookSecret);
      }
      router.push("/");
    } catch {
      setError("Erro ao criar conta. Verifique os dados.");
      setIsSubmitting(false);
    }
  };

  useEffect(() => {
    setMounted(true);
  }, []);

  if (!mounted) {
    return (
      <div className="flex-1 flex items-center justify-center min-h-screen">
        <Loader2 className="w-8 h-8 animate-spin text-indigo-600" />
      </div>
    );
  }

  return (
    <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-md overflow-y-auto">
      <div className="min-h-full flex flex-col items-center justify-center p-6">
      
      <div className="w-full max-w-2xl mb-8 flex items-center justify-between">
        <StepIndicator currentStep={step} stepNumber={1} title="Workspace" icon={<Building2 className="w-5 h-5" />} />
        <div className={`flex-1 h-1 mx-4 rounded-full transition-colors duration-500 ${step >= 2 ? 'bg-indigo-600' : 'bg-slate-200'}`}></div>
        <StepIndicator currentStep={step} stepNumber={2} title="Time" icon={<Users className="w-5 h-5" />} />
        <div className={`flex-1 h-1 mx-4 rounded-full transition-colors duration-500 ${step >= 3 ? 'bg-indigo-600' : 'bg-slate-200'}`}></div>
        <StepIndicator currentStep={step} stepNumber={3} title="Canais" icon={<MessageSquare className="w-5 h-5" />} />
        <div className={`flex-1 h-1 mx-4 rounded-full transition-colors duration-500 ${step >= 4 ? 'bg-indigo-600' : 'bg-slate-200'}`}></div>
        <StepIndicator currentStep={step} stepNumber={4} title="Plano" icon={<CreditCard className="w-5 h-5" />} />
      </div>

      <div className="bg-white w-full max-w-2xl rounded-3xl shadow-xl border border-slate-100 overflow-hidden flex flex-col min-h-[450px]">
        <div className="p-8 flex-1">
          {error && (
            <div className="mb-6 p-4 bg-rose-50 border border-rose-200 text-rose-600 rounded-xl text-sm font-medium flex items-center gap-2">
              <span className="font-bold">Atenção:</span> {error}
            </div>
          )}

          {step === 1 && (
            <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500">
              <div className="text-center mb-8">
                <h2 className="text-2xl font-bold text-slate-800">Bem-vindo ao Flywheel</h2>
                <p className="text-slate-500 mt-1">Vamos configurar seu workspace para começarmos.</p>
              </div>

              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-1">Nome da Empresa</label>
                <input type="text" value={data.name} onChange={e => setData({...data, name: e.target.value})} className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all text-slate-900 font-medium placeholder:text-slate-400" placeholder="Acme Corp" />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <div className="flex items-center justify-between mb-1">
                    <label className="text-sm font-semibold text-slate-700">Documento</label>
                    <div className="flex bg-slate-100 rounded-lg p-0.5">
                      <button onClick={() => setData({...data, documentType: 'CNPJ', document: ''})} className={`px-2 py-0.5 text-xs font-medium rounded-md transition-colors ${data.documentType === 'CNPJ' ? 'bg-white shadow-sm text-indigo-600' : 'text-slate-500 hover:text-slate-700'}`}>CNPJ</button>
                      <button onClick={() => setData({...data, documentType: 'CPF', document: ''})} className={`px-2 py-0.5 text-xs font-medium rounded-md transition-colors ${data.documentType === 'CPF' ? 'bg-white shadow-sm text-indigo-600' : 'text-slate-500 hover:text-slate-700'}`}>CPF</button>
                    </div>
                  </div>
                  <input type="text" value={data.document} onChange={e => setData({...data, document: applyDocumentMask(e.target.value, data.documentType as any)})} className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all text-slate-900 font-medium placeholder:text-slate-400" placeholder={data.documentType === 'CNPJ' ? 'AB.CDE.FGH/0001-00' : '000.000.000-00'} />
                </div>
                <div>
                  <label className="block text-sm font-semibold text-slate-700 mb-1">Telefone (WhatsApp)</label>
                  <input type="text" value={data.phone} onChange={e => setData({...data, phone: e.target.value})} className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all text-slate-900 font-medium placeholder:text-slate-400" placeholder="+55 (11) 99999-9999" />
                </div>
              </div>

              <div>
                <label className="block text-sm font-semibold text-slate-700 mb-1">Nicho / Setor</label>
                <select value={data.niche} onChange={e => setData({...data, niche: e.target.value})} className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all text-slate-900 font-medium">
                  <option value="">Selecione um nicho...</option>
                  <option value="Tecnologia">Tecnologia (SaaS, Software)</option>
                  <option value="Serviços B2B">Serviços B2B</option>
                  <option value="Serviços B2C">Serviços B2C</option>
                  <option value="Marketing">Agência de Marketing</option>
                  <option value="Imobiliário">Imobiliário</option>
                  <option value="Aluguéis">Aluguéis</option>
                  <option value="Construção Civil">Construção Civil</option>
                  <option value="Indústria">Indústria</option>
                  <option value="Saúde">Saúde</option>
                  <option value="Agronegócio">Agronegócio</option>
                  <option value="Varejo/E-commerce">Varejo/E-commerce</option>
                  <option value="Outros">Outros</option>
                </select>
              </div>
            </div>
          )}

          {step === 2 && (
            <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500 flex flex-col items-center justify-center h-full text-center py-8">
              <div className="w-16 h-16 bg-indigo-100 rounded-full flex items-center justify-center text-indigo-600 mb-4">
                <Users className="w-8 h-8" />
              </div>
              <h2 className="text-2xl font-bold text-slate-800">Convide seu time</h2>
              <p className="text-slate-500 max-w-md mx-auto">
                Você pode convidar uma pessoa agora para colaborar na sua empresa. 
                Mais membros podem ser adicionados depois em <span className="font-medium text-slate-700">Configurações {'>'} Empresa {'>'} Membros</span>.
              </p>
              
              <div className="w-full max-w-sm mt-6 text-left space-y-4">
                <div>
                  <label className="block text-sm font-semibold text-slate-700 mb-1">E-mail do Colaborador (Opcional)</label>
                  <input type="email" value={data.invitedMemberEmail} onChange={e => setData({...data, invitedMemberEmail: e.target.value})} className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all text-slate-900 font-medium placeholder:text-slate-400" placeholder="colega@suaempresa.com" />
                </div>
                <div>
                  <label className="block text-sm font-semibold text-slate-700 mb-1">Perfil de Acesso</label>
                  <select value={data.invitedMemberRole} onChange={e => setData({...data, invitedMemberRole: e.target.value})} className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all text-slate-900 font-medium">
                    <option value="ADMIN">Administrador (Acesso Total)</option>
                    <option value="MANAGER">Gerente (Relatórios e Equipe)</option>
                    <option value="AGENT">Agente (Apenas Vendas/Atendimento)</option>
                  </select>
                </div>
              </div>
            </div>
          )}

          {step === 3 && (
            <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500 h-full">
              <div className="text-center mb-8">
                <h2 className="text-2xl font-bold text-slate-800">Conecte seus Canais</h2>
                <p className="text-slate-500 mt-1">Conecte seus canais para iniciar conversas e automações. Você pode fazer isso agora ou configurar mais tarde no Hub de Integrações.</p>
              </div>

              <div className="space-y-4 max-w-lg mx-auto">
                <div className="flex items-center justify-between p-4 border border-slate-200 rounded-2xl bg-white hover:border-indigo-300 transition-all cursor-pointer">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 bg-emerald-100 text-emerald-600 rounded-xl flex items-center justify-center">
                      <MessageSquare className="w-6 h-6" />
                    </div>
                    <div>
                      <h3 className="font-bold text-slate-800">WhatsApp Oficial</h3>
                      <p className="text-sm text-slate-500">API Cloud Oficial da Meta</p>
                    </div>
                  </div>
                  <button className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-sm font-semibold rounded-lg transition-colors">Conectar</button>
                </div>

                <div className="flex items-center justify-between p-4 border border-slate-200 rounded-2xl bg-white hover:border-indigo-300 transition-all cursor-pointer">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 bg-pink-100 text-pink-600 rounded-xl flex items-center justify-center">
                      <Camera className="w-6 h-6" />
                    </div>
                    <div>
                      <h3 className="font-bold text-slate-800">Instagram Direct</h3>
                      <p className="text-sm text-slate-500">Responda DMs no CRM</p>
                    </div>
                  </div>
                  <button className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-sm font-semibold rounded-lg transition-colors">Conectar</button>
                </div>

                <div className="flex items-center justify-between p-4 border border-slate-200 rounded-2xl bg-white hover:border-indigo-300 transition-all cursor-pointer">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 bg-blue-100 text-blue-600 rounded-xl flex items-center justify-center">
                      <Mail className="w-6 h-6" />
                    </div>
                    <div>
                      <h3 className="font-bold text-slate-800">E-mail (Gmail/Outlook)</h3>
                      <p className="text-sm text-slate-500">Sincronização de caixas</p>
                    </div>
                  </div>
                  <button className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-700 text-sm font-semibold rounded-lg transition-colors">Conectar</button>
                </div>
              </div>
            </div>
          )}

          {step === 4 && (
            <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-500 h-full">
              <div className="text-center mb-8">
                <h2 className="text-2xl font-bold text-slate-800">Escolha o seu plano</h2>
                <p className="text-slate-500 mt-1">Para finalizar a configuração, defina o tamanho da sua operação.</p>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div onClick={() => setData({...data, planType: 'STARTER'})} className={`p-6 rounded-2xl border-2 cursor-pointer transition-all flex flex-col ${data.planType === 'STARTER' ? 'border-indigo-600 bg-indigo-50/50 shadow-md ring-4 ring-indigo-50' : 'border-slate-200 hover:border-indigo-300'}`}>
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="font-bold text-lg text-slate-800">Starter</h3>
                    <div className={`w-6 h-6 rounded-full flex items-center justify-center flex-shrink-0 ${data.planType === 'STARTER' ? 'bg-indigo-600 text-white' : 'border-2 border-slate-200'}`}>
                      {data.planType === 'STARTER' && <Check className="w-4 h-4" />}
                    </div>
                  </div>
                  <p className="text-sm text-slate-500 mb-4 min-h-[40px]">Ideal para equipes pequenas começando a escalar vendas B2B.</p>
                  <p className="text-3xl font-bold text-slate-900 mb-2">R$ 199<span className="text-sm font-normal text-slate-500">/mês</span></p>
                  <div className="mb-6">
                    <span className="text-[11px] text-slate-500 font-semibold bg-slate-100 py-1 px-2.5 rounded-md inline-block border border-slate-200">
                      Pagamento recorrente de R$ 2.388,00 a cada 12 meses
                    </span>
                  </div>
                  
                  <ul className="space-y-3 text-sm text-slate-600 mt-auto border-t border-slate-200/60 pt-6">
                    <li className="flex items-start gap-2">
                      <Check className="w-4 h-4 text-indigo-500 mt-0.5 flex-shrink-0" />
                      <span>Crie e gerencie até 3 usuários por máquina.</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <Check className="w-4 h-4 text-indigo-500 mt-0.5 flex-shrink-0" />
                      <span>8 automações para otimizar as interações com leads.</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <Check className="w-4 h-4 text-indigo-500 mt-0.5 flex-shrink-0" />
                      <span>Suporte multicanal com até 3 conexões (WhatsApp, Instagram e outras).</span>
                    </li>
                  </ul>
                </div>

                <div onClick={() => setData({...data, planType: 'PRO'})} className={`p-6 rounded-2xl border-2 cursor-pointer transition-all flex flex-col ${data.planType === 'PRO' ? 'border-indigo-600 bg-indigo-50/50 shadow-md ring-4 ring-indigo-50' : 'border-slate-200 hover:border-indigo-300'}`}>
                  <div className="flex justify-between items-start mb-4">
                    <h3 className="font-bold text-lg text-slate-800">Pro</h3>
                    <div className={`w-6 h-6 rounded-full flex items-center justify-center flex-shrink-0 ${data.planType === 'PRO' ? 'bg-indigo-600 text-white' : 'border-2 border-slate-200'}`}>
                      {data.planType === 'PRO' && <Check className="w-4 h-4" />}
                    </div>
                  </div>
                  <p className="text-sm text-slate-500 mb-4 min-h-[40px]">Para operações avançadas com múltiplos canais e agentes.</p>
                  <p className="text-3xl font-bold text-slate-900 mb-2">R$ 499<span className="text-sm font-normal text-slate-500">/mês</span></p>
                  <div className="mb-6">
                    <span className="text-[11px] text-slate-500 font-semibold bg-slate-100 py-1 px-2.5 rounded-md inline-block border border-slate-200">
                      Pagamento recorrente de R$ 5.988,00 a cada 12 meses
                    </span>
                  </div>
                  
                  <ul className="space-y-3 text-sm text-slate-600 mt-auto border-t border-slate-200/60 pt-6">
                    <li className="flex items-start gap-2">
                      <Check className="w-4 h-4 text-indigo-500 mt-0.5 flex-shrink-0" />
                      <span>Crie e gerencie usuários ilimitados.</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <Check className="w-4 h-4 text-indigo-500 mt-0.5 flex-shrink-0" />
                      <span>Automações ilimitadas e fluxos complexos.</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <Check className="w-4 h-4 text-indigo-500 mt-0.5 flex-shrink-0" />
                      <span>Suporte multicanal sem limite de conexões.</span>
                    </li>
                    <li className="flex items-start gap-2">
                      <Check className="w-4 h-4 text-indigo-500 mt-0.5 flex-shrink-0" />
                      <span>Acesso à API para integração com outras ferramentas customizadas.</span>
                    </li>
                  </ul>
                </div>
              </div>

              <div className="mt-8 p-6 bg-slate-50 border border-slate-200 rounded-2xl animate-in fade-in">
                <div className="flex flex-col gap-2">
                  <label className="text-sm font-bold text-slate-800">
                    Provisionamento de Administradores
                  </label>
                  <p className="text-sm text-slate-500 mb-2">
                    Deseja já criar usuários com perfil de <strong>Administrador</strong>? Insira os e-mails separados por vírgula.
                  </p>
                  <input 
                    type="text" 
                    value={data.adminEmails} 
                    onChange={e => setData({...data, adminEmails: e.target.value})} 
                    className="w-full px-4 py-3 bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all text-slate-900 font-medium placeholder:text-slate-400" 
                    placeholder="admin@empresa.com" 
                  />
                  <label className="text-sm font-bold text-slate-800 mt-4">Senha do administrador</label>
                  <input
                    type="password"
                    value={data.adminPassword}
                    onChange={e => setData({...data, adminPassword: e.target.value})}
                    className="w-full px-4 py-3 bg-white border border-slate-300 rounded-xl focus:ring-2 focus:ring-indigo-500 outline-none transition-all text-slate-900 font-medium"
                    placeholder="Mínimo 8 caracteres"
                  />
                </div>
              </div>
            </div>
          )}
        </div>

        <div className="p-6 border-t border-slate-100 bg-slate-50/50 flex items-center justify-between">
          {step > 1 ? (
            <button onClick={prevStep} className="px-6 py-3 text-slate-600 font-semibold hover:bg-slate-200 rounded-xl transition-colors flex items-center gap-2">
              <ChevronLeft className="w-5 h-5" /> Voltar
            </button>
          ) : <div></div>}
          
          {step < 4 ? (
            <button onClick={nextStep} className="px-8 py-3 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold rounded-xl shadow-lg shadow-indigo-200 transition-all flex items-center gap-2">
              Avançar <ChevronRight className="w-5 h-5" />
            </button>
          ) : (
            <button onClick={() => setShowContractModal(true)} disabled={isSubmitting} className="px-8 py-3 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold rounded-xl shadow-lg shadow-emerald-200 transition-all flex items-center gap-2">
              <Check className="w-5 h-5" />
              Finalizar Setup
            </button>
          )}
        </div>
      </div>

      {showContractModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center animate-in fade-in p-4">
          <div className="bg-white rounded-2xl w-full max-w-lg p-6 md:p-8 shadow-xl relative animate-in zoom-in-95 duration-200">
            <div className="flex justify-between items-start mb-6">
              <div>
                <h2 className="text-2xl font-bold text-slate-800">Resumo do Contrato</h2>
                <p className="text-slate-500 mt-1">Verifique os detalhes da sua assinatura</p>
              </div>
              <div className="w-12 h-12 bg-indigo-100 rounded-xl flex items-center justify-center text-indigo-600">
                <FileText className="w-6 h-6" />
              </div>
            </div>

            <div className="bg-slate-50 border border-slate-200 rounded-xl p-5 mb-6 space-y-4">
              <div className="flex justify-between items-center border-b border-slate-200 pb-4">
                <div>
                  <p className="text-sm font-semibold text-slate-500">Plano Escolhido</p>
                  <p className="text-lg font-bold text-slate-900">{data.planType === 'STARTER' ? 'Starter B2B' : 'Pro Multi-Canal'}</p>
                </div>
                <div className="text-right">
                  <p className="text-sm font-semibold text-slate-500">Frequência</p>
                  <p className="text-lg font-bold text-slate-900">Anual</p>
                </div>
              </div>
              
              <div className="flex justify-between items-center">
                <p className="text-slate-600 font-medium">Valor Mensal Equivalente</p>
                <p className="font-semibold text-slate-800">R$ {data.planType === 'STARTER' ? '199,00' : '499,00'}</p>
              </div>
              
              <div className="flex justify-between items-center">
                <p className="text-slate-600 font-medium">Faturamento Anual (12x)</p>
                <p className="font-semibold text-slate-800">R$ {data.planType === 'STARTER' ? '2.388,00' : '5.988,00'}</p>
              </div>

              <div className="pt-4 border-t border-slate-200 flex justify-between items-center">
                <p className="text-lg font-bold text-slate-900">Total a Pagar Hoje</p>
                <p className="text-2xl font-black text-indigo-600">R$ {data.planType === 'STARTER' ? '2.388,00' : '5.988,00'}</p>
              </div>
            </div>

            <div className="text-xs text-slate-500 mb-8 leading-relaxed">
              Ao prosseguir para o pagamento, você concorda com os <strong>Termos de Serviço</strong> e <strong>Política de Privacidade</strong> da Flywheel. A assinatura anual garante acesso à plataforma e será renovada automaticamente a cada 12 meses.
            </div>

            <div className="flex justify-end gap-3">
              <button 
                onClick={() => setShowContractModal(false)}
                disabled={isSubmitting}
                className="px-5 py-2.5 text-sm font-semibold text-slate-600 hover:bg-slate-100 rounded-xl transition-colors"
              >
                Revisar Plano
              </button>
              <button 
                onClick={handleFinish}
                disabled={isSubmitting}
                className="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-bold rounded-xl shadow-md transition-all flex items-center gap-2"
              >
                {isSubmitting ? <Loader2 className="w-4 h-4 animate-spin" /> : <CreditCard className="w-4 h-4" />}
                Ir para o Pagamento
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  </div>
  );
}

interface StepIndicatorProps {
  currentStep: number;
  stepNumber: number;
  title: string;
  icon: React.ReactNode;
}

function StepIndicator({ currentStep, stepNumber, title, icon }: StepIndicatorProps) {
  const isCompleted = currentStep > stepNumber;
  const isActive = currentStep === stepNumber;
  
  return (
    <div className="flex flex-col items-center gap-2 relative z-20">
      <div className={`w-12 h-12 rounded-2xl flex items-center justify-center transition-all duration-300 ${isActive ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-200 scale-110' : isCompleted ? 'bg-indigo-100 text-indigo-600' : 'bg-slate-100 text-slate-400'}`}>
        {isCompleted ? <Check className="w-6 h-6" /> : icon}
      </div>
      <span className={`text-sm font-semibold absolute top-14 whitespace-nowrap transition-colors duration-300 ${isActive ? 'text-indigo-600' : isCompleted ? 'text-slate-700' : 'text-slate-400'}`}>{title}</span>
    </div>
  );
}
