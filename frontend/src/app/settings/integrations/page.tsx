"use client";

import { useState } from "react";
import { MessageSquare, Camera, FileSpreadsheet, Settings2, Plus, ChevronRight } from "lucide-react";
import { WhatsAppSettingsModal } from "@/components/integrations/WhatsAppSettingsModal";
import { InstagramSettingsModal } from "@/components/integrations/InstagramSettingsModal";
import { GoogleFormsSettingsModal } from "@/components/integrations/GoogleFormsSettingsModal";

export default function IntegrationsHub() {
  const [activeModal, setActiveModal] = useState<string | null>(null);

  const integrations = [
    {
      id: "whatsapp",
      name: "WhatsApp Oficial",
      description: "Conecte a API Oficial da Meta para enviar e receber mensagens com estabilidade e segurança.",
      icon: <MessageSquare className="w-8 h-8 text-emerald-500" />,
      color: "bg-emerald-500/10 border-emerald-500/20",
      status: "Configurado"
    },
    {
      id: "instagram",
      name: "Instagram Direct",
      description: "Atenda seus leads do Instagram diretamente pelo CRM, sem perder o contexto.",
      icon: <Camera className="w-8 h-8 text-pink-500" />,
      color: "bg-pink-500/10 border-pink-500/20",
      status: "Pendente"
    },
    {
      id: "google_forms",
      name: "Google Forms",
      description: "Receba leads automaticamente via webhook assim que um formulário for preenchido.",
      icon: <FileSpreadsheet className="w-8 h-8 text-indigo-500" />,
      color: "bg-indigo-500/10 border-indigo-500/20",
      status: "Pendente"
    }
  ];

  return (
    <div className="flex-1 p-8 bg-slate-50 min-h-screen overflow-y-auto">
      <div className="max-w-5xl mx-auto space-y-8">
        
        {/* Header */}
        <div className="flex justify-between items-end">
          <div>
            <h1 className="text-3xl font-bold text-slate-900 tracking-tight">Hub de Integrações</h1>
            <p className="text-slate-500 mt-1 text-lg">Centralize a conexão do CRM com todos os seus canais de aquisição.</p>
          </div>
          <button className="flex items-center gap-2 px-4 py-2 bg-white border border-slate-200 text-slate-700 text-sm font-medium rounded-lg hover:bg-slate-50 transition-colors shadow-sm">
            <Settings2 className="w-4 h-4" />
            Preferências Globais
          </button>
        </div>

        {/* Integration Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {integrations.map((item) => (
            <div 
              key={item.id}
              onClick={() => setActiveModal(item.id)}
              className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm hover:shadow-md hover:border-slate-300 transition-all cursor-pointer group flex flex-col relative overflow-hidden"
            >
              {/* Decorative Background Blob */}
              <div className={`absolute -right-6 -top-6 w-24 h-24 rounded-full opacity-50 blur-2xl transition-transform group-hover:scale-150 ${item.color}`}></div>
              
              <div className="flex justify-between items-start mb-4 relative z-10">
                <div className={`p-3 rounded-xl ${item.color}`}>
                  {item.icon}
                </div>
                <div className={`px-2.5 py-1 rounded-full text-xs font-semibold ${
                  item.status === 'Configurado' ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-600'
                }`}>
                  {item.status}
                </div>
              </div>
              
              <div className="relative z-10 flex-1">
                <h3 className="text-xl font-bold text-slate-800 mb-2">{item.name}</h3>
                <p className="text-sm text-slate-500 leading-relaxed">{item.description}</p>
              </div>

              <div className="mt-6 pt-4 border-t border-slate-100 flex justify-between items-center relative z-10">
                <span className="text-sm font-medium text-slate-600 group-hover:text-indigo-600 transition-colors">
                  Configurar
                </span>
                <ChevronRight className="w-4 h-4 text-slate-400 group-hover:text-indigo-600 group-hover:translate-x-1 transition-all" />
              </div>
            </div>
          ))}

          {/* Add New Integration Placeholder */}
          <div className="bg-slate-50 rounded-2xl border-2 border-dashed border-slate-200 p-6 flex flex-col items-center justify-center text-center hover:border-indigo-300 hover:bg-indigo-50/30 transition-colors cursor-pointer group">
            <div className="w-12 h-12 bg-white rounded-full flex items-center justify-center shadow-sm mb-3 group-hover:bg-indigo-100 transition-colors">
              <Plus className="w-6 h-6 text-slate-400 group-hover:text-indigo-600" />
            </div>
            <h3 className="text-sm font-bold text-slate-600 group-hover:text-indigo-700">Solicitar Nova Integração</h3>
            <p className="text-xs text-slate-400 mt-1">Nós criamos pra você</p>
          </div>
        </div>
      </div>

      {/* Modals */}
      <WhatsAppSettingsModal isOpen={activeModal === "whatsapp"} onClose={() => setActiveModal(null)} />
      <InstagramSettingsModal isOpen={activeModal === "instagram"} onClose={() => setActiveModal(null)} />
      <GoogleFormsSettingsModal isOpen={activeModal === "google_forms"} onClose={() => setActiveModal(null)} />
    </div>
  );
}
