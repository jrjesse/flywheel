"use client";
import { apiFetch } from "@/lib/api";

import { useState, useEffect, useCallback, useMemo } from "react";
import { Search, Filter, SlidersHorizontal, Building2, Briefcase, Play, CheckCircle2, ArrowUpDown, ArrowUp, ArrowDown, Flame } from "lucide-react";

export default function LeadSearchPage() {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const [leads, setLeads] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);

    // Filter states
    const [q, setQ] = useState("");
    const [companyName, setCompanyName] = useState("");
    const [jobFunction, setJobFunction] = useState("");
    const [companySize, setCompanySize] = useState("");
    const [minMrr, setMinMrr] = useState("");

    const [sortConfig, setSortConfig] = useState<{ key: string, direction: 'asc' | 'desc' } | null>(null);

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

    const sortedLeads = useMemo(() => {
        let sortableLeads = [...leads];
        if (sortConfig !== null) {
            sortableLeads.sort((a, b) => {
                let aValue = a[sortConfig.key];
                let bValue = b[sortConfig.key];
                
                if (sortConfig.key === 'companyName') {
                    aValue = a.company?.companyName;
                    bValue = b.company?.companyName;
                } else if (sortConfig.key === 'role') {
                    aValue = a.professionalInfo?.jobTitle || a.role;
                    bValue = b.professionalInfo?.jobTitle || b.role;
                }

                if (aValue === null || aValue === undefined) aValue = '';
                if (bValue === null || bValue === undefined) bValue = '';

                if (aValue < bValue) return sortConfig.direction === 'asc' ? -1 : 1;
                if (aValue > bValue) return sortConfig.direction === 'asc' ? 1 : -1;
                return 0;
            });
        }
        return sortableLeads;
    }, [leads, sortConfig]);

    const fetchLeads = useCallback(async () => {
        setLoading(true);
        try {
            const params = new URLSearchParams();
            if (q) params.append("q", q);
            if (companyName) params.append("companyName", companyName);
            if (jobFunction) params.append("jobFunction", jobFunction);
            if (companySize) params.append("size", companySize);
            if (minMrr) params.append("minMrr", minMrr);
            
            // Backend returns Page<Lead>, so we access data.content
            const res = await apiFetch(`/api/leads/search?${params.toString()}`);
            if (res.ok) {
                const data = await res.json();
                setLeads(data.content || []);
            }
        } catch (error) {
            console.error(error);
        } finally {
            setLoading(false);
        }
    }, [q, companyName, jobFunction, companySize, minMrr]);
    
    // Initial fetch
    useEffect(() => {
        fetchLeads();
    }, [fetchLeads]);

    const moveToFunnel = async (id: number) => {
        try {
            const res = await apiFetch(`/api/leads/${id}/status`, {
                method: "PATCH",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ status: "PENDING" }),
            });
            if (res.ok) {
                // Atualiza localmente a interface para refletir a mudança instantaneamente
                setLeads(prev => prev.map(l => l.id === id ? { ...l, status: "PENDING" } : l));
            }
        } catch (error) {
            console.error("Erro ao mover lead para o funil:", error);
        }
    };

    return (
        <div className="flex h-[calc(100vh-4rem)]">
            
            {/* Sidebar Filters */}
            <div className="w-72 bg-white border-r border-slate-200 flex flex-col h-full z-10 shrink-0 shadow-[1px_0_5px_rgba(0,0,0,0.02)]">
                <div className="p-5 border-b border-slate-100 flex items-center justify-between">
                    <div className="flex items-center gap-2 text-slate-800 font-bold text-sm tracking-tight">
                        <SlidersHorizontal className="w-4 h-4 text-indigo-600"/>
                        Construtor de Filtros
                    </div>
                </div>
                
                <div className="flex-1 overflow-y-auto p-5 space-y-7">
                    {/* Name/Email */}
                    <div className="space-y-2">
                        <label className="text-[10px] font-bold text-slate-500 uppercase tracking-widest pl-1">Palavra-chave</label>
                        <div className="relative">
                            <Search className="absolute left-3 top-2.5 w-4 h-4 text-slate-400"/>
                            <input 
                                type="text" 
                                value={q}
                                onChange={(e)=> setQ(e.target.value)}
                                placeholder="Nome, e-mail, bio..."
                                className="w-full pl-9 pr-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all font-medium text-slate-700 placeholder:text-slate-400 placeholder:font-normal"
                            />
                        </div>
                    </div>

                    <div className="h-px bg-slate-100"></div>

                    {/* Company */}
                    <div className="space-y-2">
                        <label className="text-[10px] font-bold text-slate-500 uppercase tracking-widest pl-1 flex items-center gap-1.5">
                            <Building2 className="w-3.5 h-3.5"/> Dados Corporativos
                        </label>
                        <input 
                            type="text" 
                            value={companyName}
                            onChange={(e)=> setCompanyName(e.target.value)}
                            placeholder="Nome da empresa..."
                            className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all font-medium text-slate-700 placeholder:text-slate-400 placeholder:font-normal mb-3"
                        />
                        <select 
                            value={companySize} 
                            onChange={(e)=> setCompanySize(e.target.value)}
                            className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 font-medium text-slate-700"
                        >
                            <option value="">Qualquer porte (Funcionários)</option>
                            <option value="MICRO">1-10 (Micro)</option>
                            <option value="SMALL">11-50 (Pequena)</option>
                            <option value="MEDIUM">51-200 (Média)</option>
                            <option value="LARGE">200-500 (Grande)</option>
                            <option value="ENTERPRISE">500+ (Enterprise)</option>
                        </select>
                        <div className="mt-3">
                            <label className="text-[10px] font-bold text-slate-500 uppercase tracking-widest pl-1">MRR Mínimo Estimado</label>
                            <div className="relative mt-1">
                                <span className="absolute left-3 top-2.5 font-bold text-slate-400 text-sm">R$</span>
                                <input 
                                    type="number" 
                                    value={minMrr}
                                    onChange={(e)=> setMinMrr(e.target.value)}
                                    placeholder="Ex: 10000"
                                    className="w-full pl-7 pr-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 transition-all font-medium text-slate-700 placeholder:text-slate-400 placeholder:font-normal"
                                />
                            </div>
                        </div>
                    </div>

                    <div className="h-px bg-slate-100"></div>

                    {/* Professional Info */}
                    <div className="space-y-2">
                        <label className="text-[10px] font-bold text-slate-500 uppercase tracking-widest pl-1 flex items-center gap-1.5">
                            <Briefcase className="w-3.5 h-3.5"/> Perfil Profissional
                        </label>
                        <select 
                            value={jobFunction} 
                            onChange={(e)=> setJobFunction(e.target.value)}
                            className="w-full px-3 py-2 text-sm bg-slate-50 border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-indigo-500/20 focus:border-indigo-500 font-medium text-slate-700"
                        >
                            <option value="">Qualquer departamento</option>
                            <option value="MARKETING">Marketing</option>
                            <option value="SALES">Vendas / Comercial</option>
                            <option value="ENGINEERING">Engenharia / TI</option>
                            <option value="FINANCE">Financeiro</option>
                            <option value="PRODUCT">Produto</option>
                            <option value="OPERATIONS">Operações</option>
                            <option value="HUMAN_RESOURCES">Recursos Humanos</option>
                        </select>
                    </div>

                </div>
            </div>

            {/* Results Grid */}
            <div className="flex-1 bg-slate-50/50 flex flex-col h-full overflow-hidden">
                <div className="px-8 py-5 border-b border-slate-200 bg-white shadow-[0_1px_5px_rgba(0,0,0,0.01)] z-0">
                    <h1 className="text-2xl font-bold tracking-tight text-slate-900">Prospector B2B</h1>
                    <p className="text-sm text-slate-500 mt-1">Refine resultados aplicando múltiplos critérios de segmentação para Outbound e ABM.</p>
                </div>

                <div className="flex-1 overflow-y-auto p-8">
                    {loading ? (
                        <div className="flex justify-center items-center h-40">
                            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600"></div>
                        </div>
                    ) : leads.length === 0 ? (
                        <div className="text-center py-16 bg-white rounded-2xl border border-slate-200 shadow-sm max-w-2xl mx-auto mt-10">
                            <div className="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4 border border-slate-100">
                                <Filter className="w-7 h-7 text-slate-400"/>
                            </div>
                            <h3 className="text-lg font-bold text-slate-700">Nenhum perfil encontrado</h3>
                            <p className="text-sm text-slate-500 mt-1 max-w-sm mx-auto">Não encontramos nenhum Lead que combine com todos os filtros selecionados. Tente remover algumas restrições.</p>
                        </div>
                    ) : (
                        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
                            <table className="w-full text-left text-sm">
                                <thead className="bg-slate-50/80 border-b border-slate-200 select-none">
                                    <tr>
                                        <th className="px-6 py-4 font-bold text-slate-600 text-[11px] uppercase tracking-wider cursor-pointer hover:bg-slate-100 group transition-colors" onClick={() => handleSort('name')}>Lead Identificado {getSortIcon('name')}</th>
                                        <th className="px-6 py-4 font-bold text-slate-600 text-[11px] uppercase tracking-wider cursor-pointer hover:bg-slate-100 group transition-colors" onClick={() => handleSort('role')}>Cargo & Departamento {getSortIcon('role')}</th>
                                        <th className="px-6 py-4 font-bold text-slate-600 text-[11px] uppercase tracking-wider cursor-pointer hover:bg-slate-100 group transition-colors" onClick={() => handleSort('companyName')}>Dados Corporativos {getSortIcon('companyName')}</th>
                                        <th className="px-6 py-4 font-bold text-slate-600 text-[11px] uppercase tracking-wider text-right cursor-pointer hover:bg-slate-100 group transition-colors" onClick={() => handleSort('score')}>Scoring {getSortIcon('score')}</th>
                                        <th className="px-6 py-4 font-bold text-slate-600 text-[11px] uppercase tracking-wider text-center">Ação</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-slate-100">
                                    {sortedLeads.map(lead => (
                                        <tr key={lead.id} className="hover:bg-indigo-50/40 transition-colors group">
                                            <td className="px-6 py-4">
                                                <div className="flex items-center gap-3">
                                                    <div className="w-9 h-9 rounded-full bg-gradient-to-br from-indigo-100 to-indigo-50 text-indigo-600 flex items-center justify-center font-bold text-sm uppercase shadow-sm border border-indigo-100/50">
                                                        {lead.name.substring(0, 2)}
                                                    </div>
                                                    <div>
                                                        <p className="font-bold text-slate-800 tracking-tight">{lead.name}</p>
                                                        <p className="text-xs font-medium text-slate-500">{lead.email}</p>
                                                    </div>
                                                </div>
                                            </td>
                                            <td className="px-6 py-4">
                                                {lead.professionalInfo && lead.professionalInfo.jobTitle ? (
                                                    <div>
                                                        <p className="text-slate-800 font-medium">{lead.professionalInfo.jobTitle}</p>
                                                        <span className="inline-block mt-1.5 px-2 py-0.5 bg-slate-100 border border-slate-200 text-[10px] font-bold text-slate-500 rounded uppercase tracking-wider">
                                                            {lead.professionalInfo.jobFunction || 'SEM SETOR'}
                                                        </span>
                                                    </div>
                                                ) : <span className="text-slate-400 italic font-medium">Não mapeado</span>}
                                            </td>
                                            <td className="px-6 py-4">
                                                {lead.company ? (
                                                    <div>
                                                        <p className="font-bold text-slate-700 flex items-center gap-1.5">
                                                            <Building2 className="w-3.5 h-3.5 text-slate-400"/>
                                                            {lead.company.companyName}
                                                        </p>
                                                        <div className="flex items-center gap-2 mt-1">
                                                            <span className="text-xs text-slate-500 font-medium bg-slate-50 px-1.5 py-0.5 rounded border border-slate-100">{lead.company.companySize || 'Desconhecido'} func.</span>
                                                            {lead.company.mrr && (
                                                                <span className="text-[10px] font-bold text-emerald-600 bg-emerald-50 px-1.5 py-0.5 rounded border border-emerald-100">
                                                                    MRR R$ {Number(lead.company.mrr).toLocaleString('pt-BR')}
                                                                </span>
                                                            )}
                                                        </div>
                                                    </div>
                                                ) : <span className="text-slate-400 italic font-medium">Autônomo</span>}
                                            </td>
                                            <td className="px-6 py-4 text-right">
                                                <span className={`inline-flex items-center justify-center px-2.5 py-1 text-xs font-bold rounded-full ${
                                                  (lead.score || 0) >= 50 
                                                    ? 'bg-orange-100 text-orange-700 border border-orange-200/50' 
                                                    : 'bg-slate-100 text-slate-600 border border-slate-200'
                                                }`}>
                                                    {(lead.score || 0) >= 50 && <Flame className="w-3 h-3 mr-1" />}
                                                    {lead.score || 0} pts
                                                </span>
                                            </td>
                                            <td className="px-6 py-4 text-center">
                                                {(!lead.status || lead.status === 'PROSPECT') ? (
                                                    <button 
                                                        onClick={() => moveToFunnel(lead.id)}
                                                        className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-indigo-600 hover:bg-indigo-700 text-white text-[11px] font-bold uppercase tracking-wider rounded-lg transition-colors shadow-sm active:scale-95 mx-auto"
                                                    >
                                                        <Play className="w-3.5 h-3.5"/> Adicionar ao Funil
                                                    </button>
                                                ) : (
                                                    <div className="inline-flex items-center gap-1.5 px-3 py-1.5 bg-slate-100 text-slate-500 text-[11px] font-bold uppercase tracking-wider rounded-lg border border-slate-200 mx-auto cursor-not-allowed">
                                                        <CheckCircle2 className="w-3.5 h-3.5 text-slate-400"/> No Funil
                                                    </div>
                                                )}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
