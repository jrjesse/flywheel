"use client";
import { apiFetch } from "@/lib/api";

import { useEffect, useState } from "react";
import { DndContext, DragEndEvent } from "@dnd-kit/core";
import { KanbanColumn } from "@/components/leads/KanbanColumn";
import { LeadModal } from "@/components/leads/LeadModal";

const COLUMNS = [
  { id: "PENDING", title: "Pendente" },
  { id: "IN_PROGRESS", title: "Em Progresso" },
  { id: "PROPOSAL_SENT", title: "Proposta Enviada" },
  { id: "COMPLETED", title: "Concluído" },
  { id: "DELAYED", title: "Atrasado" }, // Leads vindo automaticamente do SLA Tracker!
];

export default function LeadsKanbanPage() {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [leads, setLeads] = useState<any[]>([]);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [selectedLead, setSelectedLead] = useState<any | null>(null);

  const fetchLeads = async () => {
    try {
      const res = await apiFetch("/api/leads");
      if (res.ok) {
        const data = await res.json();
        setLeads(data);
      }
    } catch (e) {
      console.error(e);
    }
  };

  useEffect(() => {
    fetchLeads();
    const intv = setInterval(fetchLeads, 5000);
    return () => clearInterval(intv);
  }, []);

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over) return;
    
    const leadId = parseInt(active.id.toString());
    const newStatus = over.id.toString();

    const targetLead = leads.find(l => l.id === leadId);
    if (!targetLead || targetLead.status === newStatus) return;

    // Optimistic Update
    setLeads(prev => prev.map(l => l.id === leadId ? { ...l, status: newStatus } : l));

    // Request to Backend
    try {
      await apiFetch(`/api/leads/${leadId}/status`, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ status: newStatus }),
      });
    } catch (error) {
      console.error("Failed to update status", error);
      fetchLeads(); // rollback se der erro
    }
  };

  return (
    <div className="flex flex-col h-[calc(100vh-130px)] max-h-full overflow-hidden animate-in fade-in duration-500">
      
      {/* Header local */}
      <div className="mb-6 shrink-0">
        <h1 className="text-2xl font-bold text-slate-100">Pipeline de Leads (Kanban)</h1>
        <p className="text-sm text-slate-500 mt-1">
          Arraste e solte os cards para atualizar o status do funil de vendas. Status sincronizados em tempo real.
        </p>
      </div>

      {/* Board */}
      <div className="flex-1 overflow-x-auto pb-6">
        <DndContext onDragEnd={handleDragEnd}>
          <div className="flex gap-6 h-full min-w-max items-start">
            {COLUMNS.map(col => (
              <KanbanColumn
                key={col.id}
                id={col.id}
                title={col.title}
                leads={leads.filter(l => l.status === col.id)}
                onDoubleClickLead={(l) => setSelectedLead(l)}
              />
            ))}
          </div>
        </DndContext>
      </div>

      {/* Modal Overlay */}
      {selectedLead && (
        <LeadModal lead={selectedLead} onClose={() => setSelectedLead(null)} />
      )}
    </div>
  );
}
