"use client";
import { apiFetch } from "@/lib/api";

import { useEffect, useState, useMemo } from "react";
import { Users, AlertTriangle, CheckCircle2, Flame, ArrowUpRight, ArrowRight, DollarSign, Target, ArrowUpDown, ArrowUp, ArrowDown, Trophy, FileText } from "lucide-react";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell, PieChart, Pie } from 'recharts';
import { LeadModal } from "@/components/leads/LeadModal";

interface Lead {
  id: number;
  name: string;
  email: string;
  phone: string;
  role: string;
  companySize: string;
  score: number;
  status: string;
  source?: string;
  company?: {
    mrr?: number;
  };
  closedRevenue?: number;
  createdAt: string;
  updatedAt: string;
  contactedAt?: string | null;
}

const formatTimeElapsed = (dateStr: string) => {
  if (!dateStr) return '-';
  const diffMs = Math.max(0, new Date().getTime() - new Date(dateStr).getTime());
  const diffMins = Math.floor(diffMs / 60000);
  const days = Math.floor(diffMins / (60 * 24));
  const hours = Math.floor((diffMins % (60 * 24)) / 60);
  const minutes = diffMins % 60;
  
  if (days > 0) return `${days}d ${hours}h ${minutes}m`;
  if (hours > 0) return `${hours}h ${minutes}m`;
  return `${minutes}m`;
};

export default function Dashboard() {
  const [leads, setLeads] = useState<Lead[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAll, setShowAll] = useState(false);
  const [sortConfig, setSortConfig] = useState<{ key: string, direction: 'asc' | 'desc' } | null>(null);
  const [filterType, setFilterType] = useState<string>('all');
  const [selectedLead, setSelectedLead] = useState<Lead | null>(null);

  // Settings & Goals state
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [settings, setSettings] = useState<any>(null);
  const [showGoalModal, setShowGoalModal] = useState(false);
  const [editingRevenueGoal, setEditingRevenueGoal] = useState(100000);
  const [editingContactGoal, setEditingContactGoal] = useState(50);

  const handleSort = (key: string) => {
    let direction: 'asc' | 'desc' = 'asc';
    if (sortConfig && sortConfig.key === key && sortConfig.direction === 'asc') {
      direction = 'desc';
    }
    setSortConfig({ key, direction });
  };

  const getSortIcon = (key: string) => {
    if (!sortConfig || sortConfig.key !== key) {
      return <ArrowUpDown className="w-3 h-3 inline ml-1 opacity-20 group-hover:opacity-100" />;
    }
    return sortConfig.direction === 'asc' 
      ? <ArrowUp className="w-3 h-3 inline ml-1 text-indigo-600" />
      : <ArrowDown className="w-3 h-3 inline ml-1 text-indigo-600" />;
  };

  const filteredLeads = useMemo(() => {
    let filtered = [...leads];
    
    switch(filterType) {
      case 'forecasted':
        filtered = filtered.filter(l => l.status === "PENDING" || l.status === "NEGOTIATING" || l.status === "PROSPECT");
        break;
      case 'proposals':
        filtered = filtered.filter(l => l.status === "PROPOSAL_SENT");
        break;
      case 'closed':
        filtered = filtered.filter(l => l.status === "COMPLETED");
        break;
      case 'pending':
        filtered = filtered.filter(l => l.status === "PENDING" || l.status === "NEGOTIATING");
        break;
      case 'contacted':
        filtered = filtered.filter(l => l.status === "CONTACTED");
        break;
      case 'hot':
        filtered = filtered.filter(l => l.score >= 50);
        break;
      case 'breach':
        filtered = filtered.filter(l => {
          const ageInMin = (new Date().getTime() - new Date(l.createdAt).getTime()) / 60000;
          return l.status === "PENDING" && ageInMin > 5;
        });
        break;
      case 'all':
      default:
        if (filterType.startsWith('source:')) {
          const sourceKey = filterType.split(':')[1];
          filtered = filtered.filter(l => (l.source || "OUTROS") === sourceKey);
        } else if (filterType.startsWith('score:')) {
          const scoreKey = filterType.split(':')[1];
          if (scoreKey === '0-25') filtered = filtered.filter(l => l.score <= 25);
          else if (scoreKey === '26-50') filtered = filtered.filter(l => l.score > 25 && l.score <= 50);
          else if (scoreKey === '51-75') filtered = filtered.filter(l => l.score > 50 && l.score <= 75);
          else if (scoreKey === '76-100') filtered = filtered.filter(l => l.score > 75 && l.score <= 100);
          else if (scoreKey === '100+') filtered = filtered.filter(l => l.score > 100);
          else if (scoreKey === 'hot') filtered = filtered.filter(l => l.score >= 100);
          else if (scoreKey === 'warm') filtered = filtered.filter(l => l.score >= 50 && l.score < 100);
          else if (scoreKey === 'cold') filtered = filtered.filter(l => l.score < 50);
        }
        break;
    }
    return filtered;
  }, [leads, filterType]);

  const sortedLeads = useMemo(() => {
    let sortableLeads = [...filteredLeads];
    if (sortConfig !== null) {
      sortableLeads.sort((a, b) => {
        let aValue = a[sortConfig.key as keyof Lead];
        let bValue = b[sortConfig.key as keyof Lead];
        
        if (aValue === null || aValue === undefined) aValue = '';
        if (bValue === null || bValue === undefined) bValue = '';

        if (aValue < bValue) return sortConfig.direction === 'asc' ? -1 : 1;
        if (aValue > bValue) return sortConfig.direction === 'asc' ? 1 : -1;
        return 0;
      });
    }
    return sortableLeads;
  }, [filteredLeads, sortConfig]);

  useEffect(() => {
    fetchLeads();
    fetchSettings();
    const interval = setInterval(fetchLeads, 5000);
    return () => clearInterval(interval);
  }, []);

  const fetchSettings = async () => {
    try {
      const data = await apiFetch<any>("/api/settings/proposals");
      setSettings(data);
      if (data.revenueGoal) setEditingRevenueGoal(data.revenueGoal);
      if (data.contactGoal) setEditingContactGoal(data.contactGoal);
    } catch (error) {
      console.error("Error fetching settings:", error);
    }
  };

  const fetchLeads = async () => {
    try {
      const data = await apiFetch<Lead[]>("/api/leads");
      data.sort((a, b) => b.score - a.score);
      setLeads(data);
    } catch (error) {
      console.error("Error fetching leads:", error);
    } finally {
      setLoading(false);
    }
  };

  const slaBreaches = leads.filter(l => {
    const ageInMin = (new Date().getTime() - new Date(l.createdAt).getTime()) / 60000;
    return l.status === "PENDING" && ageInMin > 5;
  });

  const pendingTasksCount = leads.filter(l => l.status === "PENDING" || l.status === "NEGOTIATING").length;

  const forecastedRevenue = leads
    .filter(l => l.status === "PENDING" || l.status === "NEGOTIATING" || l.status === "PROSPECT") 
    .reduce((sum, l) => sum + (l.company?.mrr || 0), 0);

  const closedRevenue = leads
    .filter(l => l.status === "COMPLETED")
    .reduce((sum, l) => {
      const revenue = l.closedRevenue !== undefined && l.closedRevenue !== null ? l.closedRevenue : (l.company?.mrr || 0);
      return sum + revenue;
    }, 0);
  
  const closedCount = leads.filter(l => l.status === "COMPLETED").length;

  const proposalsSentRevenue = leads
    .filter(l => l.status === "PROPOSAL_SENT")
    .reduce((sum, l) => sum + (l.closedRevenue || l.company?.mrr || 0), 0);

  const proposalsSentCount = leads.filter(l => l.status === "PROPOSAL_SENT").length;

  const sourceData = leads.reduce((acc, lead) => {
    const source = lead.source || "OUTROS";
    acc[source] = (acc[source] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);

  const getSourceColor = (source: string) => {
    const s = source.toLowerCase();
    if (s.includes('whatsapp')) return '#22c55e'; // emerald-500
    if (s.includes('instagram')) return '#ec4899'; // pink-500
    if (s.includes('linkedin')) return '#0ea5e9'; // sky-500
    if (s.includes('facebook')) return '#3b82f6'; // blue-500
    if (s.includes('google') || s.includes('ads')) return '#ef4444'; // red-500
    if (s.includes('email') || s.includes('e-mail')) return '#f59e0b'; // amber-500
    return '#818cf8'; // indigo-400 (default)
  };

  const chartData = Object.keys(sourceData).map(key => {
    const formattedName = key.replace(/_/g, " ");
    return {
      name: formattedName.charAt(0).toUpperCase() + formattedName.slice(1).toLowerCase(),
      originalKey: key,
      leads: sourceData[key],
      color: getSourceColor(key)
    };
  });

  const scoreData = [
    { range: '0-25', filterKey: '0-25', value: leads.filter(l => l.score <= 25).length, color: '#93c5fd' }, // blue-300
    { range: '26-50', filterKey: '26-50', value: leads.filter(l => l.score > 25 && l.score <= 50).length, color: '#3b82f6' }, // blue-500
    { range: '51-75', filterKey: '51-75', value: leads.filter(l => l.score > 50 && l.score <= 75).length, color: '#f59e0b' }, // amber-500
    { range: '76-100', filterKey: '76-100', value: leads.filter(l => l.score > 75 && l.score <= 100).length, color: '#f97316' }, // orange-500
    { range: '100+', filterKey: '100+', value: leads.filter(l => l.score > 100).length, color: '#ef4444' } // red-500
  ];

  // Avg Response Time Calculation
  const contactedLeads = leads.filter(l => l.contactedAt && new Date(l.contactedAt).getTime() > new Date(l.createdAt).getTime());
  const avgResponseTimeMin = contactedLeads.length > 0 
    ? contactedLeads.reduce((sum, l) => sum + (new Date(l.contactedAt!).getTime() - new Date(l.createdAt).getTime()) / 60000, 0) / contactedLeads.length 
    : 0;
  
  const gaugeMax = 60; // 60 minutes SLA scale
  const gaugeValue = Math.min(avgResponseTimeMin, gaugeMax);
  const gaugeColor = avgResponseTimeMin <= 5 ? '#10b981' : avgResponseTimeMin <= 15 ? '#f59e0b' : '#ef4444'; // Emerald for <5m, Amber for <15m, Red for >15m
  const gaugeData = [
    { name: 'Time', value: gaugeValue, fill: gaugeColor },
    { name: 'Empty', value: gaugeMax - gaugeValue, fill: '#f1f5f9' } // slate-100
  ];

  // Goal Metrics
  const revenueGoal = settings?.revenueGoal || 100000;
  const contactGoal = settings?.contactGoal || 50;
  const contactedLeadsCount = leads.filter(l => l.status === "CONTACTED").length;
  
  const revenueProgress = Math.min((closedRevenue / revenueGoal) * 100, 100);
  const contactProgress = Math.min((contactedLeadsCount / contactGoal) * 100, 100);

  return (
    <div className="space-y-8 animate-in fade-in duration-500 pb-10">
      
      {/* Greeting & Goals Banner */}
      <div className="bg-gradient-to-r from-indigo-600 to-violet-600 rounded-2xl p-8 shadow-lg text-white relative overflow-hidden">
        {/* Background decorations */}
        <div className="absolute top-0 right-0 -mt-10 -mr-10 w-40 h-40 bg-white opacity-10 rounded-full blur-2xl"></div>
        <div className="absolute bottom-0 right-40 -mb-10 w-32 h-32 bg-white opacity-10 rounded-full blur-xl"></div>
        
        <div className="relative z-10 flex flex-col lg:flex-row lg:items-center justify-between gap-6">
          <div className="max-w-xl">
            <h1 className="text-3xl font-bold tracking-tight text-white">Bem-vindo de volta, Admin! 👋</h1>
            <p className="text-indigo-100 mt-2 text-base leading-relaxed mb-5">
              Desejamos excelentes vendas hoje. Mantenha o foco no funil, atue rápido nas pendências de SLA e vamos juntos superar as metas do trimestre!
            </p>
            <button 
              onClick={() => setShowGoalModal(true)}
              className="bg-white/10 hover:bg-white/20 text-white px-4 py-2 rounded-lg text-sm font-medium transition-colors border border-white/20 inline-flex items-center gap-2"
            >
              <Target className="w-4 h-4" /> Editar Metas
            </button>
          </div>
          
          <div className="flex gap-6 items-center bg-white/10 p-5 rounded-xl backdrop-blur-sm border border-white/20 shrink-0">
            {/* Revenue Goal */}
            <div className="space-y-1.5 w-32 md:w-40">
              <div className="flex justify-between text-xs font-medium text-indigo-100">
                <span>Meta Receita</span>
                <span>{Math.round(revenueProgress)}%</span>
              </div>
              <div className="h-2.5 w-full bg-indigo-900/50 rounded-full overflow-hidden">
                <div className="h-full bg-emerald-400 rounded-full transition-all duration-1000" style={{ width: `${revenueProgress}%` }}></div>
              </div>
              <p className="text-xs font-bold text-white">R$ {(closedRevenue/1000).toFixed(1)}k / R$ {(revenueGoal/1000).toFixed(0)}k</p>
            </div>
            
            <div className="w-px h-10 bg-white/20"></div>

            {/* Contacts Goal */}
            <div className="space-y-1.5 w-32 md:w-40">
              <div className="flex justify-between text-xs font-medium text-indigo-100">
                <span>Contatos Feitos</span>
                <span>{Math.round(contactProgress)}%</span>
              </div>
              <div className="h-2.5 w-full bg-indigo-900/50 rounded-full overflow-hidden">
                <div className="h-full bg-amber-400 rounded-full transition-all duration-1000" style={{ width: `${contactProgress}%` }}></div>
              </div>
              <p className="text-xs font-bold text-white">{contactedLeadsCount} / {contactGoal} leads</p>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: KPIs */}
        <div className="lg:col-span-2 grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
          {/* Total Leads */}
          <div 
            onClick={() => setFilterType('all')}
            className={`bg-white rounded-xl border border-slate-200 p-6 shadow-sm hover:shadow-md transition-all relative overflow-hidden group cursor-pointer ${filterType === 'all' ? 'ring-2 ring-indigo-500 bg-slate-50' : ''}`}
          >
            <div className="flex justify-between items-start">
              <div>
                <p className="text-sm font-medium text-slate-500">Total Leads</p>
                <h3 className="text-3xl font-bold text-slate-900 mt-2">{leads.length}</h3>
              </div>
              <div className="p-2.5 bg-blue-50 text-blue-600 rounded-lg group-hover:scale-110 transition-transform">
                <Users className="w-5 h-5" />
              </div>
            </div>
          </div>

          {/* Forecasted Revenue */}
          <div 
            onClick={() => setFilterType('forecasted')}
            className={`bg-white rounded-xl border border-slate-200 p-6 shadow-sm hover:shadow-md transition-all relative overflow-hidden group cursor-pointer ${filterType === 'forecasted' ? 'ring-2 ring-indigo-500 bg-slate-50' : ''}`}
          >
            <div className="flex justify-between items-start">
              <div>
                <p className="text-sm font-medium text-slate-500">Faturamento Previsto</p>
                <h3 className="text-3xl font-bold text-slate-900 mt-2">
                  R$ {forecastedRevenue.toLocaleString('pt-BR', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}
                </h3>
              </div>
              <div className="p-2.5 bg-indigo-50 text-indigo-600 rounded-lg group-hover:scale-110 transition-transform">
                <DollarSign className="w-5 h-5" />
              </div>
            </div>
            <p className="mt-4 text-xs text-slate-500">Baseado no MRR do funil ativo</p>
          </div>

          {/* Propostas Enviadas */}
          <div 
            onClick={() => setFilterType('proposals')}
            className={`bg-white rounded-xl border border-slate-200 p-6 shadow-sm hover:shadow-md transition-all relative overflow-hidden group cursor-pointer ${filterType === 'proposals' ? 'ring-2 ring-indigo-500 bg-slate-50' : ''}`}
          >
            <div className="flex justify-between items-start">
              <div>
                <p className="text-sm font-medium text-slate-500">Propostas Enviadas</p>
                <h3 className="text-3xl font-bold text-slate-900 mt-2">
                  R$ {proposalsSentRevenue.toLocaleString('pt-BR', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}
                </h3>
              </div>
              <div className="p-2.5 bg-sky-50 text-sky-600 rounded-lg group-hover:scale-110 transition-transform">
                <FileText className="w-5 h-5" />
              </div>
            </div>
            <p className="mt-4 text-xs text-slate-500">{proposalsSentCount} {proposalsSentCount === 1 ? 'proposta enviada' : 'propostas enviadas'}</p>
          </div>

          {/* Closed Won Revenue */}
          <div 
            onClick={() => setFilterType('closed')}
            className={`bg-white rounded-xl border border-slate-200 p-6 shadow-sm hover:shadow-md transition-all relative overflow-hidden group cursor-pointer ${filterType === 'closed' ? 'ring-2 ring-indigo-500 bg-slate-50' : ''}`}
          >
            <div className="flex justify-between items-start">
              <div>
                <p className="text-sm font-medium text-slate-500">Vendas Fechadas</p>
                <h3 className="text-3xl font-bold text-slate-900 mt-2">
                  R$ {closedRevenue.toLocaleString('pt-BR', { minimumFractionDigits: 0, maximumFractionDigits: 0 })}
                </h3>
              </div>
              <div className="p-2.5 bg-emerald-50 text-emerald-600 rounded-lg group-hover:scale-110 transition-transform">
                <Trophy className="w-5 h-5" />
              </div>
            </div>
            <p className="mt-4 text-xs text-slate-500">{closedCount} {closedCount === 1 ? 'lead concluído' : 'leads concluídos'}</p>
          </div>

          {/* Pending Tasks */}
          <div 
            onClick={() => setFilterType('pending')}
            className={`bg-white rounded-xl border border-slate-200 p-6 shadow-sm hover:shadow-md transition-all relative overflow-hidden group cursor-pointer ${filterType === 'pending' ? 'ring-2 ring-indigo-500 bg-slate-50' : ''}`}
          >
            <div className="flex justify-between items-start">
              <div>
                <p className="text-sm font-medium text-slate-500">Tarefas Pendentes</p>
                <h3 className="text-3xl font-bold text-slate-900 mt-2">{pendingTasksCount}</h3>
              </div>
              <div className="p-2.5 bg-purple-50 text-purple-600 rounded-lg group-hover:scale-110 transition-transform">
                <Target className="w-5 h-5" />
              </div>
            </div>
            <p className="mt-4 text-xs text-slate-500">Leads aguardando ação</p>
          </div>

          {/* Contacted */}
          <div 
            onClick={() => setFilterType('contacted')}
            className={`bg-white rounded-xl border border-slate-200 p-6 shadow-sm hover:shadow-md transition-all relative overflow-hidden group cursor-pointer ${filterType === 'contacted' ? 'ring-2 ring-indigo-500 bg-slate-50' : ''}`}
          >
            <div className="flex justify-between items-start">
              <div>
                <p className="text-sm font-medium text-slate-500">Contatados</p>
                <h3 className="text-3xl font-bold text-slate-900 mt-2">
                  {leads.filter(l => l.status === "CONTACTED").length}
                </h3>
              </div>
              <div className="p-2.5 bg-emerald-50 text-emerald-600 rounded-lg group-hover:scale-110 transition-transform">
                <CheckCircle2 className="w-5 h-5" />
              </div>
            </div>
          </div>

          {/* High Ticket */}
          <div 
            onClick={() => setFilterType('hot')}
            className={`bg-white rounded-xl border border-slate-200 p-6 shadow-sm hover:shadow-md transition-all relative overflow-hidden group cursor-pointer ${filterType === 'hot' ? 'ring-2 ring-indigo-500 bg-slate-50' : ''}`}
          >
            <div className="flex justify-between items-start">
              <div>
                <p className="text-sm font-medium text-slate-500">Hot Leads</p>
                <h3 className="text-3xl font-bold text-slate-900 mt-2">
                  {leads.filter(l => l.score >= 50).length}
                </h3>
              </div>
              <div className="p-2.5 bg-orange-50 text-orange-600 rounded-lg group-hover:scale-110 transition-transform">
                <Flame className="w-5 h-5" />
              </div>
            </div>
          </div>

          {/* SLA Breaches */}
          <div 
            onClick={() => setFilterType('breach')}
            className={`bg-white rounded-xl border border-slate-200 p-6 shadow-sm hover:shadow-md transition-all relative overflow-hidden group cursor-pointer ${filterType === 'breach' ? 'ring-2 ring-red-500 bg-slate-50' : 'ring-1 ring-transparent hover:ring-red-100'}`}
          >
            <div className="flex justify-between items-start">
              <div>
                <p className="text-sm font-medium text-slate-500">Falhas SLA (&gt;5m)</p>
                <h3 className={`text-3xl font-bold mt-2 ${slaBreaches.length > 0 ? 'text-red-600' : 'text-slate-900'}`}>
                  {slaBreaches.length}
                </h3>
              </div>
              <div className={`p-2.5 rounded-lg transition-transform group-hover:scale-110 ${slaBreaches.length > 0 ? 'bg-red-50 text-red-600' : 'bg-slate-50 text-slate-400'}`}>
                <AlertTriangle className="w-5 h-5" />
              </div>
            </div>
          </div>
        </div>

        {/* Right Column: Charts */}
        <div className="bg-white rounded-xl border border-slate-200 shadow-sm flex flex-col h-full overflow-hidden">
          <div className="grid grid-cols-1 divide-y divide-slate-100 flex-1">
            {/* Origem Chart */}
            <div className="p-6 flex flex-col min-h-[220px]">
              <h3 className="text-base font-semibold text-slate-800 mb-4">Origem de Aquisição</h3>
              <div className="flex-1 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 25 }}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                    <XAxis 
                      dataKey="name" 
                      axisLine={false} 
                      tickLine={false} 
                      tick={{ fontSize: 11, fill: '#475569', fontWeight: 500 }} 
                      dy={10}
                      interval={0}
                    />
                    <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 11, fill: '#94a3b8' }} />
                    <Tooltip cursor={{fill: '#f1f5f9'}} contentStyle={{ borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)', fontSize: '12px', fontWeight: 600 }} />
                    <Bar 
                      dataKey="leads" 
                      radius={[6, 6, 0, 0]} 
                      barSize={32} 
                      cursor="pointer"
                      // eslint-disable-next-line @typescript-eslint/no-explicit-any
                      onClick={(data: any) => setFilterType(`source:${data.originalKey}`)}
                    >
                      {chartData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Score Chart - Histogram */}
            <div className="p-5 flex flex-col min-h-[220px]">
              <h3 className="text-base font-semibold text-slate-800 mb-2">Qualificação (Distribuição de Score)</h3>
              <div className="flex-1 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={scoreData} margin={{ top: 10, right: 10, left: -20, bottom: 25 }} barCategoryGap={2}>
                    <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f1f5f9" />
                    <XAxis 
                      dataKey="range" 
                      axisLine={false} 
                      tickLine={false} 
                      tick={{ fontSize: 11, fill: '#475569', fontWeight: 500 }} 
                      dy={10}
                    />
                    <YAxis axisLine={false} tickLine={false} tick={{ fontSize: 11, fill: '#94a3b8' }} />
                    <Tooltip cursor={{fill: '#f1f5f9'}} contentStyle={{ borderRadius: '12px', border: '1px solid #e2e8f0', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)', fontSize: '12px', fontWeight: 600 }} />
                    <Bar 
                      dataKey="value" 
                      radius={[4, 4, 0, 0]} 
                      barSize={32} 
                      cursor="pointer"
                      // eslint-disable-next-line @typescript-eslint/no-explicit-any
                      onClick={(data: any) => setFilterType(`score:${data.filterKey}`)}
                    >
                      {scoreData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Bar>
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Gauge Chart - Tempo de Resposta */}
            <div className="p-5 flex flex-col min-h-[200px] relative">
              <div className="flex justify-between items-start mb-2">
                <h3 className="text-base font-semibold text-slate-800">Tempo Médio de Resposta</h3>
                <span className={`text-xs font-bold px-2 py-1 rounded-md ${avgResponseTimeMin <= 5 ? 'bg-emerald-50 text-emerald-600' : avgResponseTimeMin <= 15 ? 'bg-amber-50 text-amber-600' : 'bg-red-50 text-red-600'}`}>
                  {avgResponseTimeMin.toFixed(1)} min
                </span>
              </div>
              <div className="flex-1 w-full flex items-center justify-center relative -mt-4">
                <ResponsiveContainer width="100%" height={140}>
                  <PieChart>
                    <Pie
                      data={gaugeData}
                      cx="50%"
                      cy="100%"
                      startAngle={180}
                      endAngle={0}
                      innerRadius={65}
                      outerRadius={85}
                      paddingAngle={0}
                      dataKey="value"
                      stroke="none"
                      isAnimationActive={true}
                    >
                      {gaugeData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.fill} />
                      ))}
                    </Pie>
                  </PieChart>
                </ResponsiveContainer>
                {/* Center text for Gauge */}
                <div className="absolute bottom-2 left-1/2 -translate-x-1/2 text-center">
                  <div className="text-sm font-medium text-slate-500">SLA: 15 min</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Main Table */}
      <div className="bg-white border border-slate-200 rounded-xl shadow-sm overflow-hidden flex flex-col">
        <div className="px-6 py-5 border-b border-slate-200 flex items-center justify-between bg-slate-50/50">
          <div className="flex items-center gap-3">
            <h2 className="text-base font-semibold text-slate-800">Pipeline de Leads Recentes</h2>
            {filterType !== 'all' && (
              <span className="bg-indigo-100 text-indigo-700 text-xs px-2.5 py-1 rounded-full font-medium">
                Filtrado
              </span>
            )}
          </div>
          {sortedLeads.length > 5 && (
            <button 
              onClick={() => setShowAll(!showAll)}
              className="text-sm font-medium text-indigo-600 hover:text-indigo-700 flex items-center gap-1 transition-colors"
            >
              {showAll ? "Ver Menos" : "Ver Todos"} <ArrowRight className="w-4 h-4"/>
            </button>
          )}
        </div>

        {loading ? (
          <div className="p-8 space-y-4">
            {[1,2,3].map(i => (
              <div key={i} className="animate-pulse flex items-center gap-4">
                <div className="h-10 w-10 bg-slate-200 rounded-full"></div>
                <div className="flex-1 space-y-2">
                  <div className="h-4 bg-slate-200 rounded w-1/4"></div>
                  <div className="h-3 bg-slate-200 rounded w-1/3"></div>
                </div>
                <div className="h-8 w-24 bg-slate-200 rounded-full"></div>
              </div>
            ))}
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="min-w-full text-left border-collapse">
              <thead>
                <tr className="border-b border-slate-200 text-xs font-semibold text-slate-500 uppercase tracking-wider bg-slate-50/50">
                  <th className="px-6 py-4 cursor-pointer hover:bg-slate-100 group transition-colors select-none" onClick={() => handleSort('score')}>Score {getSortIcon('score')}</th>
                  <th className="px-6 py-4 cursor-pointer hover:bg-slate-100 group transition-colors select-none" onClick={() => handleSort('name')}>Lead {getSortIcon('name')}</th>
                  <th className="px-6 py-4 cursor-pointer hover:bg-slate-100 group transition-colors select-none" onClick={() => handleSort('role')}>Perfil / Tamanho {getSortIcon('role')}</th>
                  <th className="px-6 py-4 cursor-pointer hover:bg-slate-100 group transition-colors select-none" onClick={() => handleSort('source')}>Origem {getSortIcon('source')}</th>
                  <th className="px-6 py-4 cursor-pointer hover:bg-slate-100 group transition-colors select-none" onClick={() => handleSort('status')}>Situação {getSortIcon('status')}</th>
                  <th className="px-6 py-4 cursor-pointer hover:bg-slate-100 group transition-colors select-none whitespace-nowrap" onClick={() => handleSort('closedRevenue')}>Proposta (R$) {getSortIcon('closedRevenue')}</th>
                  <th className="px-6 py-4 cursor-pointer hover:bg-slate-100 group transition-colors select-none whitespace-nowrap" onClick={() => handleSort('closedRevenue')}>Venda (R$) {getSortIcon('closedRevenue')}</th>
                  <th className="px-6 py-4 cursor-pointer hover:bg-slate-100 group transition-colors select-none whitespace-nowrap" onClick={() => handleSort('updatedAt')}>Tempo Proposta {getSortIcon('updatedAt')}</th>
                  <th className="px-6 py-4 text-right cursor-pointer hover:bg-slate-100 group transition-colors select-none" onClick={() => handleSort('createdAt')}>Tempo Limite {getSortIcon('createdAt')}</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {sortedLeads.slice(0, showAll ? sortedLeads.length : 5).map((lead) => {
                  const ageInMin = Math.floor((new Date().getTime() - new Date(lead.createdAt).getTime()) / 60000);
                  const isBreach = lead.status === "PENDING" && ageInMin > 5;
                  
                  return (
                    <tr key={lead.id} className="hover:bg-slate-50/80 transition-colors group cursor-pointer" onClick={() => setSelectedLead(lead)}>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex items-center justify-center px-2.5 py-1 text-xs font-bold rounded-full ${
                          lead.score >= 50 
                            ? 'bg-orange-100 text-orange-700 border border-orange-200/50' 
                            : 'bg-slate-100 text-slate-600 border border-slate-200'
                        }`}>
                          {lead.score >= 50 && <Flame className="w-3 h-3 mr-1" />}
                          {lead.score} pts
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center gap-3">
                          <div className="w-8 h-8 rounded-full bg-indigo-100 text-indigo-600 flex items-center justify-center font-bold text-xs">
                            {lead.name.charAt(0)}
                          </div>
                          <div>
                            <div className="text-sm font-semibold text-slate-900 group-hover:text-indigo-600 transition-colors">{lead.name}</div>
                            <div className="text-xs text-slate-500">{lead.email}</div>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-medium text-slate-800">{lead.role || 'N/A'}</div>
                        <div className="text-xs text-slate-500 mt-0.5">{lead.companySize || 'N/A'}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="text-xs font-medium text-slate-500 bg-slate-100 px-2 py-1 rounded">
                          {lead.source ? lead.source.replace(/_/g, " ") : "OUTROS"}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium border ${
                          lead.status === 'CONTACTED' 
                            ? 'bg-emerald-50 border-emerald-200 text-emerald-700' 
                            : 'bg-amber-50 border-amber-200 text-amber-700'
                        }`}>
                          {lead.status}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-700 font-medium">
                        {lead.status === 'PROPOSAL_SENT' ? `R$ ${(lead.closedRevenue || lead.company?.mrr || 0).toLocaleString('pt-BR')}` : '-'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-emerald-600 font-semibold">
                        {lead.status === 'COMPLETED' ? `R$ ${(lead.closedRevenue || lead.company?.mrr || 0).toLocaleString('pt-BR')}` : '-'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-slate-600">
                        {lead.status === 'PROPOSAL_SENT' ? (
                           <span className="bg-sky-50 text-sky-700 px-2.5 py-1 rounded-md font-medium border border-sky-100">
                             {formatTimeElapsed(lead.updatedAt)}
                           </span>
                        ) : '-'}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm">
                        {isBreach ? (
                          <div className="flex items-center justify-end gap-1.5 text-red-600 font-bold bg-red-50/50 px-2 py-1 rounded-md inline-flex border border-red-100" title={`${ageInMin} minutos excedidos`}>
                            <AlertTriangle className="w-4 h-4" />
                            <span>{formatTimeElapsed(lead.createdAt)}</span>
                          </div>
                        ) : (
                          <span className="text-slate-500 font-medium">{formatTimeElapsed(lead.createdAt)}</span>
                        )}
                      </td>
                    </tr>
                  );
                })}
                {sortedLeads.length === 0 && (
                  <tr>
                    <td colSpan={9} className="px-6 py-16 text-center text-slate-500">
                      <div className="flex flex-col items-center justify-center">
                        <Users className="w-8 h-8 text-slate-300 mb-2" />
                        <p className="text-sm">Nenhum lead encontrado para este filtro.</p>
                      </div>
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {selectedLead && (
        <LeadModal lead={selectedLead} onClose={() => setSelectedLead(null)} />
      )}

      {/* Goal Edit Modal */}
      {showGoalModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/50 backdrop-blur-sm flex items-center justify-center animate-in fade-in">
          <div className="bg-white rounded-2xl w-[400px] p-6 shadow-xl relative animate-in zoom-in-95 duration-200">
            <h2 className="text-lg font-bold text-slate-800 mb-4">Configurar Metas</h2>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Meta de Receita (R$)</label>
                <input 
                  type="number" 
                  value={editingRevenueGoal}
                  onChange={(e) => setEditingRevenueGoal(Number(e.target.value))}
                  className="w-full border-slate-300 rounded-lg shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Meta de Contatos (Leads)</label>
                <input 
                  type="number" 
                  value={editingContactGoal}
                  onChange={(e) => setEditingContactGoal(Number(e.target.value))}
                  className="w-full border-slate-300 rounded-lg shadow-sm focus:border-indigo-500 focus:ring-indigo-500"
                />
              </div>
            </div>

            <div className="mt-6 flex justify-end gap-3">
              <button 
                onClick={() => setShowGoalModal(false)}
                className="px-4 py-2 text-sm font-medium text-slate-600 hover:text-slate-800"
              >
                Cancelar
              </button>
              <button 
                onClick={async () => {
                  try {
                    await apiFetch("/api/settings/proposals", {
                      method: "PUT",
                      headers: { "Content-Type": "application/json" },
                      body: JSON.stringify({
                        ...(settings || {}),
                        revenueGoal: editingRevenueGoal,
                        contactGoal: editingContactGoal
                      })
                    });
                    fetchSettings();
                    setShowGoalModal(false);
                  } catch (e) {
                    console.error(e);
                  }
                }}
                className="px-4 py-2 bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium rounded-lg"
              >
                Salvar Metas
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
