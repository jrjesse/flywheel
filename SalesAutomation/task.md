# Implementação do Módulo de Fila de WhatsApp

- `[x]` **1. Criação das Entidades de Domínio**
  - `[x]` Criar `InteractionQueue` (Status, Timestamps, Tracking).
  - `[x]` Criar `InteractionStatusLog` para histórico e TMA/TME.
  - `[x]` Criar `AgentStatus` com `@Version` (Optimistic Locking) e `lastHeartbeat` para monitoramento.

- `[x]` **2. Camada de Repositórios (Data Access)**
  - `[x]` `InteractionQueueRepository`.
  - `[x]` `InteractionStatusLogRepository`.
  - `[x]` `AgentStatusRepository` com `@Modifying` query nativa para atomicidade no incremento de capacidade.

- `[x]` **3. Camada de Serviços (Core Logic)**
  - `[x]` `QueueService`: Lógica de entrada, debounce e roteamento Round-Robin.
  - `[x]` `AgentStatusService`: Gerenciamento do status Online/Offline e Heartbeat.

- `[x]` **4. Jobs Assíncronos (Self-Healing e SLA)**
  - `[x]` `SlaMonitorJob`: Varredura de `RECEBIDO` (Self-healing do Debounce) e `AGUARDANDO` (SLA Transbordo).
  - `[x]` `HeartbeatMonitorJob`: Varredura de atendentes caídos e failover (re-enfileiramento) dos seus chats ativos.

- `[x]` **5. Controladores (API)**
  - `[x]` Endpoint Webhook para entrada de novas mensagens do CRM/ZAPI.
  - `[x]` Endpoints para o Agent Dashboard (Heartbeat, Puxar chat).
