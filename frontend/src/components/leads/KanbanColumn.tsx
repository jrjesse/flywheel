import { useDroppable } from "@dnd-kit/core";
import { KanbanCard } from "./KanbanCard";

interface ColumnProps {
  id: string;
  title: string;
  leads: any[];
  onDoubleClickLead: (lead: any) => void;
}

export function KanbanColumn({ id, title, leads, onDoubleClickLead }: ColumnProps) {
  const { isOver, setNodeRef } = useDroppable({ id });

  // Sorteio Automático: Leads mais quentes (Score maior) primeiro, ou se for atrasado, os mais velhos primeiro.
  const sortedLeads = [...leads].sort((a, b) => b.score - a.score);

  return (
    <div className="flex flex-col bg-slate-100/50 rounded-2xl w-80 shrink-0 overflow-hidden border border-slate-200">
      <div className="p-4 border-b border-slate-200 flex justify-between items-center bg-slate-100/80">
        <h3 className="font-semibold text-slate-700">{title}</h3>
        <span className="bg-slate-200 text-slate-600 text-xs font-bold px-2 py-1 rounded-full">
          {leads.length}
        </span>
      </div>
      <div
        ref={setNodeRef}
        className={`flex-1 p-3 overflow-y-auto transition-colors ${
          isOver ? "bg-indigo-50/50" : ""
        }`}
      >
        {sortedLeads.map((lead) => (
          <KanbanCard key={lead.id} lead={lead} onDoubleClick={() => onDoubleClickLead(lead)} />
        ))}
        {leads.length === 0 && (
          <div className="h-full min-h-[100px] flex items-center justify-center border-2 border-dashed border-slate-200 rounded-xl">
            <p className="text-xs text-slate-400">Solte leads aqui</p>
          </div>
        )}
      </div>
    </div>
  );
}
