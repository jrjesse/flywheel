# Plano de Implementação: Campos de Enriquecimento (MRR e Icebreaker/Bio)

Você tocou em um ponto fundamental. Ferramentas como o *Clay* brilham justamente por enviar dados super ricos (como *Icebreakers* gerados por IA e faturamento recorrente estimado - MRR). Atualmente a nossa API já possui a coluna genérica `bio` na tabela `Lead`, mas ela não estava sendo ativamente consumida pela API de Webhook nem filtrada no Prospector B2B, e a tabela `Company` não possuía a variável específica de MRR.

## User Review Required

> [!IMPORTANT]
> **Aprovação de Regras de Negócio:**
> Eu propus uma lógica de *Scoring* no plano abaixo (ex: dar +20 pontos se o lead vier com um *Icebreaker* pronto). Isso faz sentido para o seu funil? O MRR será salvo como número decimal (`BigDecimal`), permitindo buscas exatas (ex: "Empresas com MRR maior que R$ 10.000").

## Proposed Changes

### 1. Banco de Dados e Entidades
#### [MODIFY] `Company.java`
* Adicionar a coluna `private BigDecimal mrr;` para armazenar a receita recorrente mensal estimada da empresa.

#### [MODIFY] `LeadRequest.java`
* Adicionar os campos `private String bio;` (para receber o texto do Icebreaker gerado pela IA no Clay) e `private BigDecimal mrr;` para que a integração de Webhook possa receber esses dados.

### 2. Backend (Lógica de Negócio e API)
#### [MODIFY] `LeadController.java`
* Atualizar a rota `/webhook` para ler o `request.getBio()` e salvar no Lead, e ler o `request.getMrr()` e atrelar à Company.

#### [MODIFY] `LeadScoringService.java`
* **Nova Classificação (Scoring):**
  * Se o Lead tiver `bio` (Icebreaker preenchido), ganha **+10 pontos** (lead pronto para abordagem rápida).
  * Se a Company tiver `mrr` > 10.000, ganha **+30 pontos**. Se for > 50.000, **+50 pontos**.

#### [MODIFY] `LeadSpecification.java` (Barramento de Busca)
* Adicionar o parâmetro `minMrr` ao filtro. Assim o sistema poderá fazer queries do tipo `WHERE company.mrr >= X`.

### 3. Frontend (Prospector e Kanban)
#### [MODIFY] `frontend/src/app/leads/search/page.tsx`
* Adicionar um novo **Input Numérico** na barra de filtros superior para "Min MRR".
* Exibir a tag de MRR nos *cards* dos leads pesquisados.

#### [MODIFY] `frontend/src/components/leads/LeadModal.tsx`
* No modal de detalhes do Lead (quando o vendedor abre o Kanban), exibir uma área de destaque chamada **"Icebreaker / Bio"** com o texto sugerido pela IA.
* Exibir o **MRR** formatado em Real (ou Dólar) na área de dados da empresa.

---

## Verification Plan
1. Fazer um `POST` simulando o Clay no `/api/leads/webhook` enviando `"mrr": 20000` e `"bio": "Vi que vocês lançaram o produto X..."`.
2. Verificar no Prospector B2B se o filtro de "MRR Mínimo 15000" retorna apenas esse lead.
3. Abrir o Kanban e confirmar se a pontuação (Score) dele subiu agressivamente e se o texto do Icebreaker aparece formatado na interface.
