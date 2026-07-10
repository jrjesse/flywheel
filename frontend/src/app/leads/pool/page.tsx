"use client";

import { useCallback, useEffect, useState } from "react";
import { Inbox, Loader2, UserPlus } from "lucide-react";
import { apiFetch } from "@/lib/api";
import type { Lead } from "@/lib/types";
import { useAuth } from "@/context/AuthProvider";
import { hasRole } from "@/lib/auth-storage";

export default function LeadPoolPage() {
  const { user } = useAuth();
  const [leads, setLeads] = useState<Lead[]>([]);
  const [loading, setLoading] = useState(true);
  const [claimingId, setClaimingId] = useState<number | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await apiFetch<Lead[]>("/api/leads/unassigned");
      setLeads(data);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const handleClaim = async (leadId: number) => {
    setClaimingId(leadId);
    try {
      await apiFetch(`/api/leads/${leadId}/claim`, { method: "PATCH" });
      setLeads((prev) => prev.filter((l) => l.id !== leadId));
    } finally {
      setClaimingId(null);
    }
  };

  const canClaim = hasRole(user, "AGENT") || hasRole(user, "ADMIN") || hasRole(user, "MANAGER");

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 flex items-center gap-2">
          <Inbox className="w-7 h-7 text-indigo-600" />
          Pool de Leads
        </h1>
        <p className="text-slate-500 mt-1">Leads sem vendedor atribuído — disponíveis para claim ou assign.</p>
      </div>

      {loading ? (
        <div className="flex justify-center py-20">
          <Loader2 className="w-8 h-8 animate-spin text-indigo-600" />
        </div>
      ) : leads.length === 0 ? (
        <div className="bg-white rounded-xl border border-slate-200 p-12 text-center text-slate-500">
          Nenhum lead no pool no momento.
        </div>
      ) : (
        <div className="grid gap-4">
          {leads.map((lead) => (
            <div
              key={lead.id}
              className="bg-white rounded-xl border border-slate-200 p-5 flex items-center justify-between"
            >
              <div>
                <h3 className="font-semibold text-slate-900">{lead.name}</h3>
                <p className="text-sm text-slate-500">{lead.email ?? "Sem email"}</p>
                <span className="text-xs text-slate-400 mt-1 inline-block">Status: {lead.status}</span>
              </div>
              {canClaim && (
                <button
                  type="button"
                  onClick={() => handleClaim(lead.id)}
                  disabled={claimingId === lead.id}
                  className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-semibold rounded-lg disabled:opacity-60"
                >
                  {claimingId === lead.id ? (
                    <Loader2 className="w-4 h-4 animate-spin" />
                  ) : (
                    <UserPlus className="w-4 h-4" />
                  )}
                  Assumir
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
