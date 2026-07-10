'use client';

import React, { useState } from 'react';
import { CreditCard, Download, ExternalLink, ShieldCheck, FileText } from 'lucide-react';

export default function BillingPage() {
  const [isProcessing, setIsProcessing] = useState(false);

  const handleManageSubscription = () => {
    setIsProcessing(true);
    // In a real app, this would call our backend to generate a Stripe Customer Portal Session URL
    setTimeout(() => {
      alert("Redirecionando para o Portal do Cliente Stripe...");
      setIsProcessing(false);
    }, 1500);
  };

  return (
    <div className="p-8 max-w-5xl mx-auto space-y-8 animate-in fade-in zoom-in duration-500">
      <div className="flex justify-between items-end">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white">Faturamento & Assinatura</h1>
          <p className="text-gray-400 mt-2">Gerencie sua assinatura, métodos de pagamento e visualize notas fiscais.</p>
        </div>
        <div className="flex items-center gap-2 text-emerald-400 text-sm font-medium bg-emerald-400/10 px-3 py-1.5 rounded-full border border-emerald-400/20">
          <ShieldCheck className="w-4 h-4" />
          LGPD Compliant
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Plano Atual */}
        <div className="col-span-2 bg-gray-900/50 backdrop-blur-md border border-gray-800 rounded-2xl p-6">
          <div className="flex justify-between items-start mb-6">
            <div>
              <h2 className="text-xl font-semibold text-white flex items-center gap-2">
                <CreditCard className="w-5 h-5 text-indigo-400" />
                Plano Pro (Mensal)
              </h2>
              <p className="text-gray-400 mt-1">Sua assinatura expira em 24 de Junho, 2026</p>
            </div>
            <div className="text-right">
              <div className="text-2xl font-bold text-white">R$ 199<span className="text-sm font-normal text-gray-400">/mês</span></div>
            </div>
          </div>
          
          <div className="bg-gray-800/50 rounded-xl p-4 mb-6 flex items-center justify-between border border-gray-700/50">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-gray-800 flex items-center justify-center border border-gray-700">
                <span className="text-white font-bold text-xs">VISA</span>
              </div>
              <div>
                <p className="text-white font-medium text-sm">Visa terminando em 4242</p>
                <p className="text-gray-400 text-xs">Expira 12/2028</p>
              </div>
            </div>
            <button className="text-indigo-400 text-sm hover:text-indigo-300 font-medium">Editar</button>
          </div>

          <div className="flex gap-4">
            <button 
              onClick={handleManageSubscription}
              disabled={isProcessing}
              className="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-medium rounded-lg transition-all flex items-center gap-2 disabled:opacity-50"
            >
              {isProcessing ? (
                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
              ) : (
                <ExternalLink className="w-4 h-4" />
              )}
              Gerenciar no Stripe
            </button>
          </div>
        </div>

        {/* Informações Fiscais */}
        <div className="bg-gray-900/50 backdrop-blur-md border border-gray-800 rounded-2xl p-6">
          <h2 className="text-lg font-semibold text-white flex items-center gap-2 mb-4">
            <FileText className="w-5 h-5 text-emerald-400" />
            Dados Fiscais
          </h2>
          <p className="text-xs text-gray-400 mb-4">
            Em conformidade com a LGPD, dados sensíveis são criptografados no banco de dados e mascarados nesta visualização.
          </p>
          
          <div className="space-y-4">
            <div>
              <p className="text-xs text-gray-500 uppercase font-semibold">Razão Social</p>
              <p className="text-sm text-gray-300">Antigravity Technologies Ltda</p>
            </div>
            <div>
              <p className="text-xs text-gray-500 uppercase font-semibold">CNPJ</p>
              <p className="text-sm text-gray-300">45.***.***/0001-**</p>
            </div>
            <div>
              <p className="text-xs text-gray-500 uppercase font-semibold">Endereço de Cobrança</p>
              <p className="text-sm text-gray-300">Av. Paulista, ****, São Paulo - SP</p>
            </div>
            <button className="text-indigo-400 text-sm hover:text-indigo-300 font-medium mt-2">Atualizar Dados</button>
          </div>
        </div>
      </div>

      {/* Histórico de Faturas */}
      <div className="bg-gray-900/50 backdrop-blur-md border border-gray-800 rounded-2xl overflow-hidden">
        <div className="p-6 border-b border-gray-800">
          <h2 className="text-xl font-semibold text-white">Histórico de Faturas</h2>
        </div>
        <table className="w-full text-left text-sm text-gray-400">
          <thead className="bg-gray-800/50 text-xs uppercase text-gray-500">
            <tr>
              <th className="px-6 py-4 font-medium">Data</th>
              <th className="px-6 py-4 font-medium">Valor</th>
              <th className="px-6 py-4 font-medium">Status</th>
              <th className="px-6 py-4 font-medium text-right">Ações</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-800">
            <tr className="hover:bg-gray-800/30 transition-colors">
              <td className="px-6 py-4 font-medium text-gray-300">24 Mai, 2026</td>
              <td className="px-6 py-4">R$ 199,00</td>
              <td className="px-6 py-4">
                <span className="px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">Pago</span>
              </td>
              <td className="px-6 py-4 text-right">
                <button className="text-gray-400 hover:text-white transition-colors" title="Baixar Nota Fiscal">
                  <Download className="w-4 h-4 ml-auto" />
                </button>
              </td>
            </tr>
            <tr className="hover:bg-gray-800/30 transition-colors">
              <td className="px-6 py-4 font-medium text-gray-300">24 Abr, 2026</td>
              <td className="px-6 py-4">R$ 199,00</td>
              <td className="px-6 py-4">
                <span className="px-2.5 py-1 rounded-full text-xs font-medium bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">Pago</span>
              </td>
              <td className="px-6 py-4 text-right">
                <button className="text-gray-400 hover:text-white transition-colors" title="Baixar Nota Fiscal">
                  <Download className="w-4 h-4 ml-auto" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
}
