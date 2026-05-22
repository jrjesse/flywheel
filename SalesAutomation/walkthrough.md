# Walkthrough - Implementação do Módulo de Fila de WhatsApp

A Fase 4 (Implementação Backend) do nosso módulo de observabilidade e fila foi concluída com sucesso! Todo o código necessário para garantir a integridade dos dados, tratamento de concorrência e *self-healing* foi criado na nova pasta `com/antigravity/sales/queue`.

## Resumo das Entregas

> [!NOTE]
> **Organização do Código**
> Foi criado um subdomínio limpo `queue` dentro da arquitetura contendo: `model`, `repository`, `service`, `job` e `api`.

### 1. Entidades de Domínio e Banco de Dados (JPA)
A estrutura do esquema foi implementada com mapeamento completo das regras.
- `InteractionQueue.java`: Guarda o "fio condutor" `Lead`, métricas de tempo (`created_at`, `started_at`, `finished_at`) e o `status`.
- `InteractionStatusLog.java`: Registra quem mudou e por que, gerando o histórico essencial para Dashboard de TME e TMA.
- `AgentStatus.java`: Criado utilizando `@Version` para Optimistic Locking e acompanhamento de sessão.

### 2. Solução de Atomicidade e Race Conditions
Conforme discutido no seu Code Review, garantimos a segurança contra sobrecarga nos atendentes.
- Em `AgentStatusRepository.java`, o incremento de chamadas foi feito com uma Query JPQL Atômica de `UPDATE`, que injeta a condição `activeChatsCount < maxCapacity`. Nenhuma concorrência fará um atendente receber 6 conversas se o limite for 5.

### 3. Debounce Buffer (Janela de Silêncio) e Self-Healing
A lógica principal foi dividida entre o Serviço de Entrada e um Job assíncrono para garantir estabilidade caso o servidor reinicie.
- `QueueService.processIncomingMessage()`: Anexa a mensagem e reseta os *timestamps* (ou cria uma fila nova como `RECEBIDO`).
- `SlaAndSelfHealingJob.selfHealDebounce()`: Roda a cada 10 segundos, buscando transações que "silenciaram" por mais de 45 segundos e joga para o roteador (`QueueService.attemptRoute()`).

### 4. Heartbeat e Proteção contra Desconexão
A observabilidade exige saber quando um atendente "caiu".
- `QueueController.heartbeat()`: O front-end do Dashboard deve fazer um ping a cada X segundos nesta rota.
- `AgentHeartbeatJob.checkAgentHeartbeats()`: Um cron varre o banco a cada 30 segundos. Se o `lastHeartbeat` for maior que 2 minutos, o atendente é posto como Offline e todos os chats `EM_ATENDIMENTO` dele são jogados para `TRANSBORDADO` com log de "Agent Disconnected" para re-enfileiramento.

## Próximos Passos (Front-End)
A API já está pronta para uso! Agora a responsabilidade passa para a aplicação em React (Next.js):
1. O Front-end do atendente precisa criar um loop de `setInterval` chamando o `/heartbeat` da API enquanto a aba estiver aberta.
2. É necessário desenvolver os painéis do **Dashboard**, consumindo os dados consolidados das entidades para plotar o TMA, TME e a saúde em tempo real da fila.

> [!TIP]
> A lógica das métricas via SQL para o Dashboard será uma simples agregação da tabela de Logs (`AVG(transitioned_at)`). Podemos desenvolver essa query dedicada no futuro!
