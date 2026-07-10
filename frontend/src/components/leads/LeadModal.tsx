import { useState, useEffect } from "react";
import { apiFetch } from "@/lib/api";
import { X, Send, Briefcase, Camera, Users, MessageCircle, Video, Code, Link as LinkIcon, Plus, AlertOctagon, FileText } from "lucide-react";
import { ProposalModal } from "./ProposalModal";

const ALL_SOCIAL_TYPES = ['LINKEDIN', 'INSTAGRAM', 'FACEBOOK', 'TWITTER', 'YOUTUBE', 'GITHUB', 'OTHER'];

const getIcon = (type: string) => {
  switch(type) {
    case 'LINKEDIN': return <Briefcase className="w-3.5 h-3.5"/>;
    case 'INSTAGRAM': return <Camera className="w-3.5 h-3.5"/>;
    case 'FACEBOOK': return <Users className="w-3.5 h-3.5"/>;
    case 'TWITTER': return <MessageCircle className="w-3.5 h-3.5"/>;
    case 'YOUTUBE': return <Video className="w-3.5 h-3.5"/>;
    case 'GITHUB': return <Code className="w-3.5 h-3.5"/>;
    default: return <LinkIcon className="w-3.5 h-3.5"/>;
  }
}

export function LeadModal({ lead, onClose }: { lead: any; onClose: () => void }) {
  const [interactions, setInteractions] = useState<any[]>([]);
  const [newDesc, setNewDesc] = useState("");
  const [loading, setLoading] = useState(true);
  const [showProposalModal, setShowProposalModal] = useState(false);

  // Social Media states
  const [socialMedias, setSocialMedias] = useState<any[]>([]);
  const [showAddSocial, setShowAddSocial] = useState(false);
  const [socialType, setSocialType] = useState("");
  const [socialUrl, setSocialUrl] = useState("");
  const [socialError, setSocialError] = useState("");

  const availableTypes = ALL_SOCIAL_TYPES.filter(type => !socialMedias.some(s => s.type === type));

  useEffect(() => {
    // Busca Histórico
    apiFetch(`/api/leads/${lead.id}/interactions`)
      .then(res => res.json())
      .then(data => {
        setInteractions(data);
        setLoading(false);
      })
      .catch(err => {
        console.error("Erro ao buscar interações:", err);
        setLoading(false);
      });

    // Busca Redes Sociais
    apiFetch(`/api/leads/${lead.id}/social`)
      .then(res => res.json())
      .then(data => setSocialMedias(data))
      .catch(err => console.error("Erro ao buscar social:", err));
  }, [lead.id]);

  const handlePost = async () => {
    if (!newDesc.trim()) return;
    try {
      const res = await apiFetch(`/api/leads/${lead.id}/interactions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: "Consultor Comercial", description: newDesc })
      });
      if (res.ok) {
        const saved = await res.json();
        setInteractions([saved, ...interactions]);
        setNewDesc("");
      }
    } catch (err) {
      console.error("Erro ao salvar interação:", err);
    }
  };

  const handleChurnAlert = async () => {
    try {
      const resInteraction = await apiFetch(`/api/leads/${lead.id}/interactions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username: "Consultor Comercial", description: "⚠️ ALERTA DE CHURN: Cliente reportou insatisfação." })
      });
      if (resInteraction.ok) {
        const saved = await resInteraction.json();
        setInteractions([saved, ...interactions]);
      }
      
      await apiFetch(`/api/notifications`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          message: `Risco de Churn: Cliente ${lead.name} (${lead.company?.companyName || 'N/A'}) reportou insatisfação.`,
          type: 'CHURN_RISK',
          leadId: lead.id
        })
      });
    } catch (err) {
      console.error("Erro ao registrar churn:", err);
    }
  };

  const handleAddSocial = async () => {
    if (!socialType || !socialUrl.trim()) return;
    setSocialError("");
    try {
      const res = await apiFetch(`/api/leads/${lead.id}/social`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ type: socialType, url: socialUrl })
      });
      
      if (res.ok) {
        const saved = await res.json();
        setSocialMedias([...socialMedias, saved]);
        setShowAddSocial(false);
        setSocialType("");
        setSocialUrl("");
      } else if (res.status === 409) {
        const errData = await res.json();
        setSocialError(errData.error || "Já existe essa plataforma");
      }
    } catch (err) {
      console.error(err);
      setSocialError("Erro de comunicação com o servidor.");
    }
  };

  const handleDeleteSocial = async (id: number) => {
    try {
      const res = await apiFetch(`/api/leads/social/${id}`, { method: 'DELETE' });
      if (res.ok) {
        setSocialMedias(socialMedias.filter(s => s.id !== id));
      }
    } catch(err) {
      console.error("Erro ao deletar social", err);
    }
  };

  return (
    <div className="fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-2xl overflow-hidden flex flex-col max-h-[90vh]">
        
        {/* Modal Header c/ Perfis */}
        <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-start bg-slate-50">
          <div className="flex-1 pr-4">
            <div className="flex items-center gap-2 mb-3">
              <p className="text-sm text-slate-500">{lead.email} • {lead.phone}</p>
              {lead.company?.mrr && (
                <span className="text-[10px] font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200 shadow-sm">
                  MRR R$ {Number(lead.company.mrr).toLocaleString('pt-BR')}
                </span>
              )}
            </div>

            {lead.bio && (
              <div className="mb-4 bg-indigo-50/50 border border-indigo-100 rounded-lg p-3 relative">
                 <span className="absolute -top-2 left-3 bg-indigo-100 text-indigo-700 text-[9px] font-bold uppercase tracking-wider px-1.5 py-0.5 rounded">Icebreaker / Bio (IA)</span>
                 <p className="text-sm text-slate-700 italic mt-1 leading-relaxed">"{lead.bio}"</p>
              </div>
            )}
            
            {/* Social Media Row */}
            <div className="flex flex-wrap gap-2 items-center">
                {socialMedias.map(sm => (
                   <div key={sm.id} className="flex items-center bg-white border border-slate-200 pl-3 pr-2 py-1.5 rounded-full shadow-sm text-xs font-medium text-slate-700 hover:border-slate-300 transition-colors group">
                       <a href={sm.url.startsWith('http') ? sm.url : `https://${sm.url}`} target="_blank" rel="noreferrer" className="flex items-center gap-1.5 hover:text-indigo-600">
                           {getIcon(sm.type)}
                           <span className="capitalize">{sm.type.toLowerCase()}</span>
                       </a>
                       <div className="w-[1px] h-3 bg-slate-200 mx-2"></div>
                       <button onClick={() => handleDeleteSocial(sm.id)} className="text-slate-300 hover:text-red-500 transition-colors" title="Remover">
                           <X className="w-3.5 h-3.5"/>
                       </button>
                   </div>
                ))}
                
                {!showAddSocial && availableTypes.length > 0 && (
                   <button onClick={() => setShowAddSocial(true)} className="flex items-center gap-1 px-3 py-1.5 rounded-full border border-dashed border-slate-300 text-xs text-slate-500 font-medium hover:text-indigo-600 hover:border-indigo-400 hover:bg-indigo-50/50 transition-colors">
                      <Plus className="w-3.5 h-3.5"/>
                      Adicionar Perfil
                   </button>
                )}
            </div>

            {/* Social Media Inline Form */}
            {showAddSocial && (
                <div className="mt-3 flex flex-wrap items-center gap-2 bg-white p-2 rounded-xl border border-indigo-100 shadow-sm md:w-fit animate-in fade-in slide-in-from-top-1">
                    <select 
                        value={socialType} 
                        onChange={(e) => setSocialType(e.target.value)}
                        className="text-xs border border-slate-200 rounded-lg py-1.5 px-2 bg-slate-50 text-slate-700 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-400"
                    >
                        <option value="">Plataforma...</option>
                        {availableTypes.map(t => <option key={t} value={t}>{t === 'OTHER' ? 'Outro' : t}</option>)}
                    </select>
                    <input 
                        type="text" 
                        value={socialUrl} 
                        onChange={(e) => setSocialUrl(e.target.value)} 
                        placeholder="Link do perfil..."
                        className="text-xs border border-slate-200 rounded-lg py-1.5 px-3 flex-1 min-w-[180px] bg-slate-50 text-slate-900 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-400"
                    />
                    <button onClick={handleAddSocial} className="bg-indigo-600 text-white p-1.5 rounded-lg hover:bg-indigo-700 transition shadow-sm">
                        <Plus className="w-4 h-4"/>
                    </button>
                    <button onClick={() => { setShowAddSocial(false); setSocialError(""); }} className="text-slate-400 hover:text-slate-600 p-1.5 hover:bg-slate-100 rounded-lg transition">
                        <X className="w-4 h-4"/>
                    </button>
                    {socialError && <span className="basis-full text-[11px] text-red-500 font-medium px-1 mt-1">{socialError}</span>}
                </div>
            )}
          </div>

          <button onClick={onClose} className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-200 rounded-full transition-colors shrink-0">
            <X className="w-5 h-5"/>
          </button>
        </div>

        {/* Modal Body (Histórico) */}
        <div className="flex-1 overflow-y-auto p-6 bg-slate-50/50">
          <h3 className="text-sm font-semibold text-slate-700 mb-4">Histórico de Interações</h3>
          
          <div className="space-y-4">
            {loading ? (
              <p className="text-sm text-slate-500 animate-pulse">Carregando histórico...</p>
            ) : interactions.length === 0 ? (
              <div className="text-center py-6">
                <p className="text-sm text-slate-500 italic">Nenhuma interação registrada ainda.</p>
              </div>
            ) : (
              interactions.map(int => (
                <div key={int.id} className="bg-white p-4 rounded-xl border border-slate-200 shadow-sm relative group">
                  <div className="absolute -left-[1.35rem] top-5 w-2.5 h-2.5 bg-indigo-200 border-2 border-white rounded-full"></div>
                  <div className="flex justify-between items-start mb-2">
                    <span className="font-semibold text-sm text-slate-800">{int.username}</span>
                    <span className="text-xs font-medium text-slate-400 bg-slate-100 px-2 py-0.5 rounded-full">{new Date(int.timestamp).toLocaleString()}</span>
                  </div>
                  <p className="text-sm text-slate-600 leading-relaxed">{int.description}</p>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Modal Footer (Input Histórico) */}
        <div className="p-4 bg-white border-t border-slate-200 shadow-[0_-4px_6px_-1px_rgba(0,0,0,0.02)]">
          <div className="flex items-center gap-2">
            <button 
              onClick={handleChurnAlert} 
              className="p-2.5 bg-red-50 text-red-600 border border-red-200 rounded-xl hover:bg-red-100 hover:text-red-700 transition-colors flex items-center justify-center gap-1.5 font-medium text-sm whitespace-nowrap"
              title="Alertar Risco de Churn"
            >
              <AlertOctagon className="w-4 h-4" />
              <span className="hidden sm:inline">Churn</span>
            </button>
            <button 
              onClick={() => setShowProposalModal(true)} 
              className="p-2.5 bg-indigo-50 text-indigo-600 border border-indigo-200 rounded-xl hover:bg-indigo-100 hover:text-indigo-700 transition-colors flex items-center justify-center gap-1.5 font-medium text-sm whitespace-nowrap"
              title="Criar Proposta Comercial"
            >
              <FileText className="w-4 h-4" />
              <span className="hidden sm:inline">Criar Proposta</span>
            </button>
            <input 
              type="text" 
              value={newDesc}
              onChange={(e) => setNewDesc(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handlePost()}
              placeholder="Adicionar nova interação ou anotação..."
              className="flex-1 px-4 py-2.5 bg-slate-50 border border-slate-300 rounded-xl text-sm text-slate-900 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
            />
            <button onClick={handlePost} className="p-3 bg-indigo-600 text-white rounded-xl hover:bg-indigo-700 hover:shadow-md transition-all active:scale-95 flex items-center justify-center">
              <Send className="w-4 h-4"/>
            </button>
          </div>
        </div>

        {/* Modals Extras */}
        {showProposalModal && (
          <ProposalModal 
            lead={lead} 
            onClose={() => setShowProposalModal(false)}
            onSuccess={() => {
              setShowProposalModal(false);
              onClose(); // Fechar o LeadModal para ver a atualização na grid
            }}
          />
        )}

      </div>
    </div>
  );
}
