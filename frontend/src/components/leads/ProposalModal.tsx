import { useState } from "react";
import { apiFetch } from "@/lib/api";
import { X, Send, FileText } from "lucide-react";

interface ProposalModalProps {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  lead: any;
  onClose: () => void;
  onSuccess: () => void;
}

export function ProposalModal({ lead, onClose, onSuccess }: ProposalModalProps) {
  const defaultClientName = lead.company?.companyName || lead.name || "";
  
  const [clientName, setClientName] = useState(defaultClientName);
  const [proposalValue, setProposalValue] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSend = async () => {
    if (!clientName || !proposalValue) {
      setError("Por favor, preencha todos os campos.");
      return;
    }
    
    setLoading(true);
    setError("");

    try {
      await apiFetch(`/api/leads/${lead.id}/proposal`, {
        method: "POST",
        body: JSON.stringify({
          clientName,
          proposalValue: parseFloat(proposalValue.replace(",", ".")),
        }),
      });
      onSuccess();
    } catch (err) {
      console.error(err);
      setError("Erro de comunicação com o servidor.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-sm z-[60] flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md overflow-hidden animate-in zoom-in-95 duration-200">
        
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-100 flex justify-between items-center bg-indigo-50/50">
          <div className="flex items-center gap-2 text-indigo-800">
            <FileText className="w-5 h-5" />
            <h2 className="font-semibold text-lg">Criar Proposta Comercial</h2>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-slate-600 transition-colors">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Body */}
        <div className="p-6 space-y-4">
          {error && (
            <div className="bg-red-50 text-red-600 text-sm p-3 rounded-lg border border-red-100">
              {error}
            </div>
          )}
          
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Nome do Prospect / Cliente</label>
            <input 
              type="text" 
              value={clientName}
              onChange={(e) => setClientName(e.target.value)}
              className="w-full px-4 py-2 bg-slate-50 border border-slate-300 rounded-xl text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
              placeholder="Ex: STI Med"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-1">Valor da Proposta (Mensal)</label>
            <div className="relative">
              <span className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 font-medium text-sm">R$</span>
              <input 
                type="number" 
                step="0.01"
                value={proposalValue}
                onChange={(e) => setProposalValue(e.target.value)}
                className="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-300 rounded-xl text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all"
                placeholder="0.00"
              />
            </div>
          </div>
          
          <div className="bg-slate-50 p-3 rounded-lg border border-slate-200 mt-4">
            <p className="text-xs text-slate-500 leading-relaxed">
              O sistema irá gerar um PDF padrão "Ring Tecnologia" (Armário Inteligente e Expurgo Hospitalar) com os dados acima e enviará um e-mail automaticamente para <strong>{lead.email}</strong>.
            </p>
          </div>
        </div>

        {/* Footer */}
        <div className="px-6 py-4 bg-slate-50 border-t border-slate-100 flex justify-end gap-3">
          <button 
            onClick={onClose}
            className="px-4 py-2 text-sm font-medium text-slate-600 hover:text-slate-800 hover:bg-slate-200 rounded-xl transition-colors"
          >
            Cancelar
          </button>
          <button 
            onClick={handleSend}
            disabled={loading}
            className="flex items-center gap-2 px-5 py-2 bg-indigo-600 text-white text-sm font-medium rounded-xl hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-sm active:scale-95"
          >
            {loading ? (
              <span className="animate-pulse">Gerando e Enviando...</span>
            ) : (
              <>
                <Send className="w-4 h-4" />
                Enviar Proposta
              </>
            )}
          </button>
        </div>
        
      </div>
    </div>
  );
}
