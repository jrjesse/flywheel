"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  Users,
  MessageSquare,
  Settings,
  PieChart,
  Search,
  LogOut,
  FileText,
  UserCog,
  Inbox,
} from "lucide-react";
import { useAuth } from "@/context/AuthProvider";
import { hasRole, isAgentOnly } from "@/lib/auth-storage";

export function Sidebar() {
  const pathname = usePathname();
  const { user, logout } = useAuth();

  const menuItems = [
    { name: "Dashboard", href: "/", icon: LayoutDashboard, roles: ["ADMIN", "MANAGER", "AGENT", "VIEWER"] },
    { name: "Prospector B2B", href: "/leads/search", icon: Search, roles: ["ADMIN", "MANAGER", "AGENT"] },
    { name: "Pipeline Kanban", href: "/leads", icon: Users, roles: ["ADMIN", "MANAGER", "AGENT", "VIEWER"] },
    { name: "Pool de Leads", href: "/leads/pool", icon: Inbox, roles: ["ADMIN", "MANAGER", "AGENT"] },
    { name: "Fila WhatsApp", href: "/queue", icon: MessageSquare, roles: ["ADMIN", "MANAGER", "AGENT"] },
    { name: "Integrações", href: "/settings/integrations", icon: Settings, roles: ["ADMIN"] },
    { name: "Config. Propostas", href: "/settings/proposals", icon: FileText, roles: ["ADMIN", "MANAGER"] },
    { name: "Equipe / Membros", href: "/settings/company/members", icon: UserCog, roles: ["ADMIN"] },
  ];

  const visibleItems = menuItems.filter((item) =>
    item.roles.some((role) => hasRole(user, role))
  );

  const initials = user?.displayName?.charAt(0)?.toUpperCase() ?? "?";
  const roleLabel = isAgentOnly(user) ? "Vendedor" : user?.roles[0] ?? "";

  return (
    <aside className="w-64 bg-slate-900 text-slate-300 min-h-screen flex flex-col shrink-0 relative overflow-hidden">
      <div className="absolute inset-0 flex items-center justify-center pointer-events-none opacity-[0.03] z-0">
        <Settings className="w-96 h-96 animate-[spin_40s_linear_infinite] text-white" />
      </div>

      <div className="h-16 flex items-center px-6 border-b border-slate-800">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-indigo-500 flex items-center justify-center">
            <PieChart className="w-5 h-5 text-white" />
          </div>
          <span className="text-white font-bold text-lg tracking-wide">Sales Automation</span>
        </div>
      </div>

      <nav className="flex-1 px-4 py-6 space-y-2 relative z-10">
        {visibleItems.map((item) => {
          const isActive =
            pathname === item.href ||
            (item.href !== "/" && pathname.startsWith(item.href));
          const Icon = item.icon;

          return (
            <Link
              key={item.href}
              href={item.href}
              className={`flex items-center gap-3 px-3 py-2.5 rounded-lg font-medium transition-colors ${
                isActive
                  ? "bg-indigo-500/10 text-indigo-400"
                  : "text-slate-300 hover:bg-slate-800"
              }`}
            >
              <Icon className="w-5 h-5" />
              <span>{item.name}</span>
            </Link>
          );
        })}
      </nav>

      <div className="p-4 border-t border-slate-800 flex items-center justify-between relative z-10 bg-slate-900/80 backdrop-blur-md">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-full bg-slate-700 overflow-hidden flex items-center justify-center shrink-0">
            <span className="text-sm font-semibold text-slate-300">{initials}</span>
          </div>
          <div className="flex flex-col">
            <span className="text-sm font-medium text-white truncate w-24">
              {user?.displayName ?? "Usuário"}
            </span>
            <span className="text-xs text-slate-500">{roleLabel}</span>
          </div>
        </div>
        <button
          type="button"
          onClick={logout}
          className="p-2 text-slate-400 hover:text-red-400 hover:bg-slate-800/50 rounded-lg transition-colors shrink-0"
          title="Sair"
        >
          <LogOut className="w-4 h-4" />
        </button>
      </div>
    </aside>
  );
}
