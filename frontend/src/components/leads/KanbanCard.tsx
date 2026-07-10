import { useDraggable } from "@dnd-kit/core";
import { CSS } from "@dnd-kit/utilities";
import { Flame, Clock, MessageSquare } from "lucide-react";

export function KanbanCard({ lead, onDoubleClick }: { lead: any; onDoubleClick: () => void }) {
  const { attributes, listeners, setNodeRef, transform, isDragging } = useDraggable({
    id: lead.id.toString(),
    data: { lead },
  });

  const style = {
    transform: CSS.Translate.toString(transform),
    opacity: isDragging ? 0.5 : 1,
    zIndex: isDragging ? 50 : 1,
  };

  const ageInMin = Math.floor((new Date().getTime() - new Date(lead.createdAt).getTime()) / 60000);

  return (
    <div
      ref={setNodeRef}
      style={style}
      {...listeners}
      {...attributes}
      onDoubleClick={onDoubleClick}
      className={`bg-white p-4 rounded-xl shadow-sm border ${
        isDragging ? "border-indigo-400 ring-2 ring-indigo-200" : "border-slate-200 hover:border-indigo-300"
      } cursor-grab active:cursor-grabbing select-none transition-colors mb-3`}
    >
      <div className="flex justify-between items-start mb-2">
        <h4 className="font-semibold text-slate-800 text-sm truncate pr-2">{lead.name}</h4>
        {lead.score >= 50 && (
          <span className="shrink-0 bg-orange-100 text-orange-600 text-[10px] font-bold px-2 py-0.5 rounded flex items-center gap-1">
            <Flame className="w-3 h-3" /> {lead.score}
          </span>
        )}
      </div>
      <p className="text-xs text-slate-500 mb-3">{lead.role || "Lead"} • {lead.companySize || "N/A"}</p>
      
      <div className="flex items-center justify-between text-xs text-slate-400 border-t border-slate-100 pt-3">
        <div className="flex items-center gap-1">
          <MessageSquare className="w-3.5 h-3.5" /> Interagir
        </div>
        <div className="flex items-center gap-1">
          <Clock className="w-3.5 h-3.5" />
          {ageInMin}m
        </div>
      </div>
    </div>
  );
}
