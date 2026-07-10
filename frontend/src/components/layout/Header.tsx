"use client";
import { apiFetch } from "@/lib/api";

import { useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { Bell, Search, AlertOctagon, Check, FileText } from "lucide-react";
import { LeadModal } from "@/components/leads/LeadModal";
import { bigramSimilarity } from "@/utils/fuzzySearch";

interface SystemNotification {
  id: number;
  message: string;
  type: string;
  leadId: number;
  read: boolean;
  createdAt: string;
}

export function Header() {
  const [notifications, setNotifications] = useState<SystemNotification[]>([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [showSearch, setShowSearch] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [searchResults, setSearchResults] = useState<any[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [selectedSearchLead, setSelectedSearchLead] = useState<any>(null);

  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        setShowSearch(true);
      }
      if (e.key === 'Escape') {
        setShowSearch(false);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  useEffect(() => {
    if (!searchQuery) {
      setSearchResults([]);
      return;
    }
    
    const delayDebounceFn = setTimeout(async () => {
      setIsSearching(true);
      try {
        const res = await apiFetch("/api/leads");
        if (res.ok) {
          const data = await res.json();
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          const SIMILARITY_THRESHOLD = 0.3;

          const scored = data.map((l: any) => {
            const nameScore = bigramSimilarity(searchQuery, l.name);
            // In backend, company might be an object { companyName: ... } or string depending on API.
            // If it's the dashboard /api/leads, l.companyName or l.company?.companyName might be used. 
            // In Dashboard page, we used l.company (string) for some reason, but let's be safe:
            const companyName = typeof l.company === 'string' ? l.company : (l.company?.companyName || "");
            const companyScore = bigramSimilarity(searchQuery, companyName);
            
            const roleScore = l.role ? bigramSimilarity(searchQuery, l.role) : 0;
            const statusStr = l.status === "PROPOSAL_SENT" ? "proposta" : l.status;
            const statusScore = bigramSimilarity(searchQuery, statusStr);

            const maxScore = Math.max(nameScore, companyScore, roleScore, statusScore);
            return { ...l, searchScore: maxScore };
          });

          const filtered = scored
            .filter((l: any) => l.searchScore > SIMILARITY_THRESHOLD)
            .sort((a: any, b: any) => b.searchScore - a.searchScore);

          setSearchResults(filtered);
        }
      } catch (error) {
        console.error("Search error", error);
      } finally {
        setIsSearching(false);
      }
    }, 300);

    return () => clearTimeout(delayDebounceFn);
  }, [searchQuery]);

  useEffect(() => {
    fetchNotifications();
    const interval = setInterval(fetchNotifications, 10000);
    return () => clearInterval(interval);
  }, []);

  const fetchNotifications = async () => {
    try {
      const res = await apiFetch("/api/notifications/unread");
      if (res.ok) {
        const data = await res.json();
        setNotifications(data);
      }
    } catch (error) {
      console.error("Error fetching notifications", error);
    }
  };

  const markAsRead = async (id: number) => {
    try {
      const res = await apiFetch(`/api/notifications/${id}/read`, {
        method: 'PATCH'
      });
      if (res.ok) {
        setNotifications(notifications.filter(n => n.id !== id));
      }
    } catch (error) {
      console.error("Error marking notification as read", error);
    }
  };

  return (
    <header className="h-16 border-b border-slate-200 bg-white/80 backdrop-blur-md sticky top-0 z-40 flex items-center justify-between px-8 shadow-sm">
      <div 
        className="flex items-center max-w-md w-full relative group cursor-pointer"
        onClick={() => setShowSearch(true)}
      >
        <Search className="w-5 h-5 text-slate-400 absolute left-3 group-hover:text-indigo-500 transition-colors" />
        <div className="w-full pl-10 pr-16 py-2 rounded-lg bg-slate-100 border border-transparent group-hover:border-indigo-200 group-hover:bg-indigo-50/50 transition-all text-sm text-slate-500 flex items-center justify-between">
          <span>Pesquisar leads, propostas...</span>
          <span className="flex items-center gap-1 text-[10px] font-bold text-slate-400 bg-white px-1.5 py-0.5 rounded border border-slate-200 shadow-sm">
            <span className="text-xs">⌘</span> K
          </span>
        </div>
      </div>
      
      <div className="flex items-center gap-4 relative">
        <button 
          onClick={() => setShowDropdown(!showDropdown)}
          className="relative p-2 text-slate-500 hover:bg-slate-100 rounded-full transition-colors"
        >
          <Bell className="w-5 h-5" />
          {notifications.length > 0 && (
            <span className="absolute top-1 right-1 w-4 h-4 bg-red-500 text-white text-[9px] font-bold rounded-full ring-2 ring-white flex items-center justify-center">
              {notifications.length}
            </span>
          )}
        </button>

        {/* Dropdown Menu */}
        {showDropdown && (
          <div className="absolute top-full right-0 mt-2 w-80 bg-white rounded-xl shadow-lg border border-slate-200 overflow-hidden z-50 animate-in slide-in-from-top-2">
            <div className="px-4 py-3 border-b border-slate-100 bg-slate-50 flex justify-between items-center">
              <h3 className="text-sm font-semibold text-slate-800">Notificações</h3>
              <span className="text-xs text-slate-500">{notifications.length} não lidas</span>
            </div>
            
            <div className="max-h-80 overflow-y-auto">
              {notifications.length === 0 ? (
                <div className="p-6 text-center text-slate-500 text-sm">
                  Nenhuma notificação no momento.
                </div>
              ) : (
                <div className="divide-y divide-slate-50">
                  {notifications.map(notif => (
                    <div key={notif.id} className="p-4 hover:bg-slate-50 transition-colors flex gap-3 items-start group">
                      <div className="p-1.5 bg-red-50 text-red-500 rounded-lg shrink-0">
                        <AlertOctagon className="w-4 h-4" />
                      </div>
                      <div className="flex-1">
                        <p className="text-sm text-slate-700 leading-tight">
                          {notif.message}
                        </p>
                        <p className="text-xs text-slate-400 mt-1">
                          {new Date(notif.createdAt).toLocaleTimeString()}
                        </p>
                      </div>
                      <button 
                        onClick={() => markAsRead(notif.id)}
                        className="text-slate-300 hover:text-indigo-600 transition-colors opacity-0 group-hover:opacity-100 p-1 bg-white rounded border border-slate-200 shadow-sm"
                        title="Marcar como lido"
                      >
                        <Check className="w-4 h-4" />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Global Search Modal / Command Palette */}
      {showSearch && (
        <div className="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-sm flex items-start justify-center pt-24 px-4 animate-in fade-in duration-200" onClick={() => setShowSearch(false)}>
          <div 
            className="bg-white rounded-2xl w-full max-w-2xl shadow-2xl overflow-hidden border border-slate-200 animate-in slide-in-from-top-8 duration-300"
            onClick={e => e.stopPropagation()}
          >
            <div className="flex items-center border-b border-slate-100 px-5">
              <Search className="w-5 h-5 text-indigo-500" />
              <input 
                type="text" 
                autoFocus
                value={searchQuery}
                onChange={e => setSearchQuery(e.target.value)}
                placeholder="Busque por leads, empresas ou contatos..." 
                className="w-full px-4 py-5 outline-none text-lg text-slate-800 placeholder:text-slate-400 font-medium bg-transparent"
              />
              <button 
                onClick={() => setShowSearch(false)}
                className="text-[10px] font-bold text-slate-400 bg-slate-100 hover:bg-slate-200 hover:text-slate-600 px-2 py-1 rounded transition-colors"
              >
                ESC
              </button>
            </div>

            <div className="max-h-[400px] overflow-y-auto p-3 bg-slate-50/50">
              {!searchQuery ? (
                <div className="p-8 text-center text-slate-500">
                  <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4 border border-slate-200">
                    <Search className="w-8 h-8 text-slate-300" />
                  </div>
                  <p className="font-semibold text-slate-700 mb-1">O que você está procurando?</p>
                  <p className="text-sm">Comece a digitar para pesquisar em toda a plataforma.</p>
                  
                  <div className="flex gap-2 justify-center mt-6">
                    <span className="text-xs font-semibold px-3 py-1.5 bg-white border border-slate-200 rounded-lg shadow-sm text-slate-600">Propostas</span>
                    <span className="text-xs font-semibold px-3 py-1.5 bg-white border border-slate-200 rounded-lg shadow-sm text-slate-600">Configurações</span>
                    <span className="text-xs font-semibold px-3 py-1.5 bg-white border border-slate-200 rounded-lg shadow-sm text-slate-600">Leads Frios</span>
                  </div>
                </div>
              ) : isSearching ? (
                <div className="p-12 text-center text-slate-500 flex flex-col items-center">
                  <div className="w-8 h-8 border-2 border-indigo-200 border-t-indigo-600 rounded-full animate-spin mb-4"></div>
                  <p className="text-sm font-medium">Buscando "{searchQuery}"...</p>
                </div>
              ) : searchResults.length === 0 ? (
                <div className="p-12 text-center text-slate-500">
                  <div className="w-12 h-12 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-3">
                    <AlertOctagon className="w-5 h-5 text-slate-400" />
                  </div>
                  <p className="font-medium text-slate-700">Nenhum resultado encontrado</p>
                  <p className="text-sm mt-1">Não encontramos nada para "{searchQuery}".</p>
                </div>
              ) : (
                <div className="space-y-1">
                  <div className="px-3 py-2 text-xs font-bold text-slate-400 uppercase tracking-wider">
                    Leads Encontrados ({searchResults.length})
                  </div>
                  {searchResults.map(lead => (
                    <div 
                      key={lead.id} 
                      onClick={() => {
                        setShowSearch(false);
                        setSelectedSearchLead(lead);
                      }}
                      className="p-3 hover:bg-white hover:shadow-sm rounded-xl cursor-pointer transition-all flex items-center justify-between group border border-transparent hover:border-slate-200"
                    >
                      <div>
                        <p className="font-semibold text-slate-800 group-hover:text-indigo-600 transition-colors flex items-center gap-2">
                          {lead.name}
                          {lead.status === "PROPOSAL_SENT" && (
                            <span className="flex items-center gap-1 text-[10px] font-bold bg-sky-50 text-sky-600 px-1.5 py-0.5 rounded border border-sky-100">
                              <FileText className="w-3 h-3" /> PROPOSTA
                            </span>
                          )}
                        </p>
                        <p className="text-sm text-slate-500 flex items-center gap-2">
                          <span>{typeof lead.company === 'string' ? lead.company : (lead.company?.companyName || 'Sem empresa')}</span>
                          {lead.score && (
                            <>
                              <span className="w-1 h-1 rounded-full bg-slate-300"></span>
                              <span className="text-indigo-600 font-medium">Score {lead.score}</span>
                            </>
                          )}
                          <span className="w-1 h-1 rounded-full bg-slate-300"></span>
                          <span className="text-xs text-slate-400">Relevância: {Math.round(lead.searchScore * 100)}%</span>
                        </p>
                      </div>
                      <div className="px-3 py-1.5 bg-slate-100 group-hover:bg-indigo-50 text-slate-500 group-hover:text-indigo-600 text-xs font-semibold rounded-lg transition-colors flex items-center gap-1">
                        Abrir Perfil
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
            
            <div className="border-t border-slate-100 bg-slate-50 px-5 py-3 flex justify-between items-center text-xs font-medium text-slate-400">
              <div className="flex gap-4">
                <span className="flex items-center gap-1">Use <kbd className="font-sans px-1 py-0.5 bg-white border border-slate-200 rounded text-slate-500 shadow-sm">↑</kbd> <kbd className="font-sans px-1 py-0.5 bg-white border border-slate-200 rounded text-slate-500 shadow-sm">↓</kbd> para navegar</span>
                <span className="flex items-center gap-1">Use <kbd className="font-sans px-1 py-0.5 bg-white border border-slate-200 rounded text-slate-500 shadow-sm">Enter</kbd> para abrir</span>
              </div>
              <div>Flywheel Search OS</div>
            </div>
          </div>
        </div>
      )}

      {/* Render Lead Profile Modal Globally */}
      {selectedSearchLead && typeof document !== "undefined" && createPortal(
        <LeadModal lead={selectedSearchLead} onClose={() => setSelectedSearchLead(null)} />,
        document.body
      )}
    </header>
  );
}
