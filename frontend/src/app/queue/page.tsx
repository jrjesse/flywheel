"use client";

import { useCallback, useEffect, useState } from "react";
import { MessageSquare, RefreshCw, Loader2 } from "lucide-react";
import { apiFetch } from "@/lib/api";
import type { QueueInteraction } from "@/lib/types";

export default function QueueDashboard() {
  const [items, setItems] = useState<QueueInteraction[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const load = useCallback(async () => {
    try {
      const data = await apiFetch<QueueInteraction[]>("/api/queue/interactions");
      setItems(data);
    } catch {
      setItems([]);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useEffect(() => {
    load();
    const interval = setInterval(load, 10000);
    return () => clearInterval(interval);
  }, [load]);

  const handleRefresh = () => {
    setRefreshing(true);
    load();
  };

  const naFila = items.filter((i) => i.status === "AGUARDANDO_ATENDIMENTO" || i.status === "RECEBIDO").length;
  const emAtendimento = items.filter((i) => i.status === "EM_ATENDIMENTO").length;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 flex items-center gap-2">
            <MessageSquare className="w-7 h-7 text-emerald-600" />
            Fila WhatsApp
          </h1>
          <p className="text-slate-500 mt-1">Interações ativas do tenant</p>
        </div>
        <button
          type="button"
          onClick={handleRefresh}
          disabled={refreshing}
          className="flex items-center gap-2 px-4 py-2 bg-white border border-slate-200 rounded-lg text-sm font-medium hover:bg-slate-50"
        >
          <RefreshCw className={`w-4 h-4 ${refreshing ? "animate-spin" : ""}`} />
          Atualizar
        </button>
      </div>

      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <MetricCard label="Na fila" value={naFila} />
        <MetricCard label="Em atendimento" value={emAtendimento} />
        <MetricCard label="Total ativo" value={items.length} />
        <MetricCard label="Transbordo" value={items.filter((i) => i.status === "TRANSBORDADO").length} />
      </div>

      {loading ? (
        <div className="flex justify-center py-16">
          <Loader2 className="w-8 h-8 animate-spin text-indigo-600" />
        </div>
      ) : items.length === 0 ? (
        <div className="bg-white rounded-xl border border-slate-200 p-12 text-center text-slate-500">
          Nenhuma interação na fila.
        </div>
      ) : (
        <div className="space-y-3">
          {items.map((item) => (
            <div key={item.id} className="bg-white rounded-xl border border-slate-200 p-4 flex justify-between items-center">
              <div>
                <p className="font-semibold text-slate-900">{item.leadName}</p>
                <p className="text-sm text-slate-500">Lead #{item.leadId} · {item.channel}</p>
              </div>
              <span className="text-xs font-bold px-2.5 py-1 rounded-full bg-slate-100 text-slate-700">
                {item.status}
              </span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function MetricCard({ label, value }: { label: string; value: number }) {
  return (
    <div className="bg-white rounded-xl border border-slate-200 p-4">
      <p className="text-xs text-slate-500 uppercase tracking-wide">{label}</p>
      <p className="text-2xl font-bold text-slate-900 mt-1">{value}</p>
    </div>
  );
}
