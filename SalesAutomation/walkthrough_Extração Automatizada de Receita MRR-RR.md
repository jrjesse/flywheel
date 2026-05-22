# Entrega: Extração Automatizada de Receita (MRR/ARR)

A funcionalidade de extração do valor real da venda foi implementada com sucesso no Backend e integrada perfeitamente à Inteligência Visual do Dashboard.

## O que foi feito:

### 1. Novo Motor Regex no Backend
Foi criado o utilitário `RevenueParserService.java`. Ele opera interceptando o fluxo de gravação do Histórico de Interação. A inteligência dele analisa os textos livres digitados pelos consultores e busca pelos seguintes padrões:
- **Prioridade 1 (Anualizado):** Padrões como `RRMx12 = 30.000,00`.
- **Prioridade 2 (Mensal):** Padrões como `R$ 2.500,00` ou `R$2.500`.

Sempre que a prioridade 1 for encontrada, o motor descarta a 2, consolidando sempre o maior valor anualizado se houver menção explícita no texto.

### 2. Modificação no Banco de Dados
A entidade `Lead` foi alterada e agora possui o campo `closedRevenue`. O servidor Spring Boot atualizou silenciosamente o banco PostgreSQL adicionando essa coluna (`closed_revenue`).

### 3. Integração com a UI (Dashboard)
A lógica da grid financeira no React (`page.tsx`) foi otimizada:
- **Antes:** Somava o MRR base cego.
- **Agora:** Se o backend capturou um valor negociado (`lead.closedRevenue`), a UI passa a utilizar esse valor primariamente. Caso o Lead seja movido para concluído sem nenhuma nota com valores (processo silencioso), o sistema cai de volta para o comportamento antigo e soma o MRR estipulado no cadastro da empresa para não deixar o Dashboard zerado.

## Como testar agora mesmo:
1. Abra um Card na coluna do Kanban.
2. Na aba Histórico, escreva exatamente o seu exemplo: *"Finalizando atendimento e concluido a venda recorente de R$2.500,00 . RRMx12 = 30.000,00."*
3. Salve o histórico.
4. Mova o card para a coluna **Concluído**.
5. Observe no Dashboard "Visão Geral", o card **"Vendas Fechadas"** pular de valor somando exatos `R$ 30.000,00` a mais na balança.

> [!TIP]
> Essa funcionalidade tira o fardo do preenchimento estruturado de campos por parte dos vendedores. Basta deixar que a máquina leia o que o vendedor fala e ela deduz os KPIs financeiros. Estratégia digna de Revenue Ops corporativo!
