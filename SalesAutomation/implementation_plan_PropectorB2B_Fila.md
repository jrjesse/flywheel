# Integração: Prospector B2B -> Fila WhatsApp

Você tocou em um ponto excelente. Atualmente, quando o módulo Prospector B2B encontra um lead e envia a primeira abordagem (mudando o status do Lead para `CONTACTED` / "WhatsApp Enviado"), isso é uma ação **Outbound** (Ativa). 

A Fila que construímos na Fase 4 foi desenhada primariamente para **Inbound** (Receptiva - aguardando mensagens). Para integrarmos perfeitamente os dois mundos e termos um "reflexo" no Dashboard da Fila, precisamos definir a semântica dessa integração.

## User Review Required

> [!IMPORTANT]
> **Aprovação Necessária:** Revise as opções de comportamento abaixo e responda às perguntas na seção Socratic Gate para definirmos a melhor regra de negócio antes de codificar.

---

## Open Questions (Socratic Gate)

Como você prefere que esse reflexo funcione no Dashboard da Fila? Tenho 3 abordagens arquiteturais para essa integração:

**Opção 1: O "Follow-Up" (SLA de Resposta)**
Quando o Prospector envia o WhatsApp, nós criamos um registro na tabela `whatsapp_interaction_queue` com um **novo status**, por exemplo: `AGUARDANDO_RESPOSTA`. 
* *Efeito:* Ele não entra no SLA de "Transbordo" do atendente, mas fica visível no Dashboard para a equipe saber que há mensagens pendentes no "mar" aguardando o lead responder. Se o lead responder, muda para `RECEBIDO` ou `AGUARDANDO_ATENDIMENTO`.

**Opção 2: O "Disparo Concluído" (Apenas Métricas)**
Quando o Prospector envia, criamos um registro na Fila já com o status `FINALIZADO`.
* *Efeito:* Ele entra apenas para engordar a métrica de "Concluídos Hoje" e de "Volume" no Dashboard, mas não aparece na lista de "Aguardando".

**Opção 3: Atribuição Direta (O Atendente assume)**
O Prospector envia a mensagem e já cria o registro como `EM_ATENDIMENTO` atribuído ao Atendente/Robô que disparou.
* *Efeito:* Ele consome a capacidade de 5 chats do Atendente, mantendo a conversa "presa" com ele.

> [!WARNING]
> **Qual das 3 opções acima reflete melhor o que você quer ver no módulo da fila quando um lead fica como "WhatsApp Enviado"?**

---

## Proposed Changes (Fluxo Técnico Geral)

Independente da opção escolhida, a alteração técnica será a seguinte:

1. **Backend (`LeadController` ou `NotificationConsumer`)**: 
   - No exato momento em que `lead.setStatus("CONTACTED")` é chamado, vamos invocar o nosso `QueueService`.
   - Adicionaremos um novo método: `queueService.registerOutboundInteraction(lead, "Template de Prospecção")`.
   
2. **Backend (`QueueService`)**:
   - Este método vai instanciar um `InteractionQueue` com o status escolhido acima e salvar no banco, garantindo que a rastreabilidade (Logs) comece a contar a partir do momento do disparo.

3. **Frontend (`QueueDashboard`)**:
   - Ajustaremos as métricas e a "Lista de Espera" para refletir esse novo status ou volume, dependendo da sua escolha no Socratic Gate.

---

## Verification Plan
1. Alterar o status de um lead manualmente ou via evento do Prospector para `CONTACTED`.
2. Verificar no PostgreSQL se uma `InteractionQueue` foi gerada automaticamente.
3. Checar a página `/queue` (Dashboard) e validar se o número de chats (seja Concluídos ou Aguardando Resposta) foi incrementado corretamente em tempo real.
