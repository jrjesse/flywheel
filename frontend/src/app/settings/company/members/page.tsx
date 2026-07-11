"use client";

import { useCallback, useEffect, useState } from "react";
import { UserCog, Plus, Shield, ShieldAlert, ShieldCheck, Building2, Loader2 } from "lucide-react";
import { apiFetch } from "@/lib/api";
import type { UserResponse, UserRole } from "@/lib/types";

export default function CompanyMembersPage() {
  const [members, setMembers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [newEmail, setNewEmail] = useState("");
  const [newName, setNewName] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [newRole, setNewRole] = useState<UserRole>("AGENT");
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const data = await apiFetch<UserResponse[]>("/api/users");
      setMembers(data);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  const handleAddMember = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await apiFetch("/api/users", {
        method: "POST",
        body: JSON.stringify({
          email: newEmail,
          displayName: newName,
          password: newPassword,
          role: newRole,
        }),
      });
      setNewEmail("");
      setNewName("");
      setNewPassword("");
      setShowAddModal(false);
      await load();
    } finally {
      setSubmitting(false);
    }
  };

  const toggleActive = async (user: UserResponse) => {
    const action = user.active ? "deactivate" : "activate";
    await apiFetch(`/api/users/${user.id}/${action}`, { method: "PATCH" });
    await load();
  };

  const getRoleBadge = (role: UserRole) => {
    switch (role) {
      case "ADMIN":
        return (
          <span className="px-2.5 py-1 bg-red-100 text-red-700 rounded-md text-xs font-bold flex items-center gap-1">
            <ShieldAlert className="w-3 h-3" /> Admin
          </span>
        );
      case "MANAGER":
        return (
          <span className="px-2.5 py-1 bg-amber-100 text-amber-700 rounded-md text-xs font-bold flex items-center gap-1">
            <Shield className="w-3 h-3" /> Gerente
          </span>
        );
      default:
        return (
          <span className="px-2.5 py-1 bg-emerald-100 text-emerald-700 rounded-md text-xs font-bold flex items-center gap-1">
            <ShieldCheck className="w-3 h-3" /> {role}
          </span>
        );
    }
  };

  return (
    <div className="space-y-8">
      <div className="flex items-center justify-between">
        <div>
          <div className="flex items-center gap-3 text-slate-500 mb-2">
            <Building2 className="w-5 h-5" />
            <span className="font-medium">Configurações da Empresa</span>
          </div>
          <h1 className="text-3xl font-black text-slate-800 tracking-tight flex items-center gap-3">
            <UserCog className="w-8 h-8 text-indigo-600" />
            Equipe
          </h1>
        </div>
        <button
          type="button"
          onClick={() => setShowAddModal(true)}
          className="flex items-center gap-2 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-semibold rounded-xl"
        >
          <Plus className="w-5 h-5" />
          Convidar
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-16">
          <Loader2 className="w-8 h-8 animate-spin text-indigo-600" />
        </div>
      ) : (
        <div className="bg-white rounded-2xl border border-slate-200 overflow-hidden">
          <table className="w-full text-left">
            <thead className="bg-slate-50 border-b border-slate-200">
              <tr>
                <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase">Membro</th>
                <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase">Role</th>
                <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase">Status</th>
                <th className="px-6 py-4 text-xs font-bold text-slate-500 uppercase">Ações</th>
              </tr>
            </thead>
            <tbody>
              {members.map((m) => (
                <tr key={m.id} className="border-b border-slate-100">
                  <td className="px-6 py-4">
                    <p className="font-semibold text-slate-900">{m.displayName}</p>
                    <p className="text-sm text-slate-500">{m.email}</p>
                  </td>
                  <td className="px-6 py-4">{getRoleBadge(m.role)}</td>
                  <td className="px-6 py-4">
                    <span className={`text-xs font-bold ${m.active ? "text-emerald-600" : "text-slate-400"}`}>
                      {m.active ? "Ativo" : "Inativo"}
                    </span>
                  </td>
                  <td className="px-6 py-4">
                    {m.role !== "ADMIN" && (
                      <button
                        type="button"
                        onClick={() => toggleActive(m)}
                        className="text-sm text-indigo-600 hover:underline"
                      >
                        {m.active ? "Desativar" : "Reativar"}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {showAddModal && (
        <div className="fixed inset-0 bg-black/40 flex items-center justify-center z-50 p-4">
          <form onSubmit={handleAddMember} className="bg-white rounded-2xl p-6 w-full max-w-md space-y-4">
            <h2 className="text-xl font-bold">Convidar membro</h2>
            <input
              required
              placeholder="Nome"
              value={newName}
              onChange={(e) => setNewName(e.target.value)}
              className="w-full px-4 py-2 border rounded-lg"
            />
            <input
              required
              type="email"
              placeholder="Email"
              value={newEmail}
              onChange={(e) => setNewEmail(e.target.value)}
              className="w-full px-4 py-2 border rounded-lg"
            />
            <input
              required
              type="password"
              placeholder="Senha inicial"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              className="w-full px-4 py-2 border rounded-lg"
            />
            <select
              value={newRole}
              onChange={(e) => setNewRole(e.target.value as UserRole)}
              className="w-full px-4 py-2 border rounded-lg"
            >
              <option value="AGENT">Vendedor (AGENT)</option>
              <option value="MANAGER">Gerente (MANAGER)</option>
              <option value="VIEWER">Viewer</option>
            </select>
            <div className="flex gap-2 justify-end">
              <button type="button" onClick={() => setShowAddModal(false)} className="px-4 py-2 text-slate-600">
                Cancelar
              </button>
              <button
                type="submit"
                disabled={submitting}
                className="px-4 py-2 bg-indigo-600 text-white rounded-lg disabled:opacity-60"
              >
                Convidar
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
}
