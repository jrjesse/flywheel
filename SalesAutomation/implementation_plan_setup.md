# Plano de Implementação: Módulo de Configurações do WhatsApp (Multitenant)

Este plano descreve o desenvolvimento do novo módulo de configurações solicitado, preparando a plataforma para operação multitenant (múltiplos clientes isolados) e integrando as variáveis de observabilidade e credenciais da Meta.

## User Review Required

> [!IMPORTANT]
> **Aprovação Necessária:** Revise o escopo técnico abaixo e confirme se o comportamento do teste de conexão e os valores padrão atendem à sua expectativa operacional.

## Open Questions

1. **Autenticação Multitenant:** Atualmente, a aplicação não possui um sistema de Login/Tenant implementado. Para essa interface, posso simular um `cliente_id` (ex: UUID fixo ou selecionável para testes) até que o módulo de autenticação geral seja construído?
2. **Criptografia no Banco:** Você deseja que os campos `accessToken` e `appSecret` sejam gravados em texto plano no banco `salesdb` por enquanto, ou devemos já aplicar uma criptografia em repouso simples (ex: `AES` no nível do Java) antes de salvar no banco? *(Para MVPs, o texto plano dentro da infraestrutura protegida é comum, mas o AES é mais seguro).*

---

## Proposed Changes

### 1. Banco de Dados e Backend (Camada de Domínio e API)

Criaremos a fundação multitenant para armazenar as configurações no PostgreSQL:

#### [NEW] Entidade `WhatsAppChannelConfig`
* `id` (UUID)
* `clientId` (UUID) - Identificador do Tenant/Cliente.
* **Credenciais Meta:** `accessToken`, `phoneNumberId`, `wabaId`, `appSecret`, `verifyToken`.
* **Regras da Fila:** `debounceSeconds` (Default: 45), `slaMinutes` (Default: 5), `maxCapacity` (Default: 5).
* Índices de banco garantindo que cada `clientId` tenha apenas uma configuração.

#### [NEW] Serviço `WhatsAppConfigService`
* Responsável por salvar as configurações.
* Implementará o método `testMetaConnection(config)`: Fará um requisição `GET` para a API da Meta (ex: `https://graph.facebook.com/v19.0/{phone_id}`) para checar se o token é válido e retornar um status de Sucesso/Falha.

#### [NEW] Controlador `WhatsAppConfigController`
* `GET /api/settings/whatsapp/{clientId}` (Busca configs).
* `PUT /api/settings/whatsapp/{clientId}` (Atualiza/Salva).
* `POST /api/settings/whatsapp/{clientId}/test` (Valida a conexão em tempo real).

### 2. Interface (Frontend React/Next.js)

Criaremos a nova tela baseada na diretriz de UI premium do `frontend-specialist`:

#### [NEW] `src/app/settings/whatsapp/page.tsx`
A página será dividida em 3 seções interativas usando componentes ricos do Tailwind:

* **Seção 1: Credenciais da API (Meta Cloud)**
  * Inputs mascarados (`type="password"`) para o Token e Secret.
  * Botão de "Testar Conexão" com loading spinner e feedback visual (verde = OK, vermelho = Falha).
  * Tooltips ao lado das labels (Ícone de `Info` usando Lucide React) com instruções claras ("Onde obter no Meta: Business Manager > System Users").

* **Seção 2: Regras da Fila & Observabilidade**
  * **Slider (Range Input):** Para a Janela de Silêncio (0 a 120 segundos).
  * **Input Numérico Estilizado:** Para SLA (minutos) e Capacidade (chats simultâneos).

* **Seção 3: Webhook Info**
  * Painel escuro (estilo terminal) com a URL final gerada dinamicamente: `https://api.seusistema.com/api/queue/webhook/{cliente_id}`.
  * Botão de cópia rápida (Copy to Clipboard).

#### [MODIFY] `Sidebar.tsx`
* Adicionar o link de `Configurações` no menu lateral direcionando para essa nova rota.

---

## Verification Plan
1. Inserir chaves inválidas na tela e clicar em "Testar Conexão" para validar o tratamento de erro.
2. Inserir chaves válidas reais para checar o sinal verde de "Conexão Estabelecida".
3. Salvar os parâmetros do Slider e recarregar a tela para garantir a persistência via API.
