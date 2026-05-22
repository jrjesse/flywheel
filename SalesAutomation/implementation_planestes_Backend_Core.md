# Plano de Implementação de Testes (Backend Core)

Este plano foca em sair do 0% de cobertura e atingir estabilidade nas rotas e regras de negócio mais críticas do Backend, utilizando **JUnit 5, Mockito e JaCoCo** para métricas de cobertura.

## User Review Required

> [!IMPORTANT]
> **Escopo da Fase 1 de Testes:**
> Focaremos 100% no Backend (Spring Boot) nesta rodada, pois é lá que mora o Motor de Regras e a injeção do Webhook. Os testes de Front-end (Playwright/Cypress) serão deixados para uma etapa posterior, focada em UI. Você está de acordo em priorizar o Backend agora?

## Proposed Changes

### 1. Configuração de Cobertura (JaCoCo)
#### [MODIFY] `backend/pom.xml`
- Adicionar o plugin `jacoco-maven-plugin`. Ele vai gerar o relatório HTML mostrando exatamente quais linhas de código não estão sendo cobertas (a meta inicial é cobrir >80% dos Services e Controllers).

### 2. Testes Unitários de Lógica de Negócio
#### [NEW] `backend/src/test/java/.../core/service/LeadScoringServiceTest.java`
- **Cenários a Cobrir:**
  - `testScoreForEnterpriseWhale()`: Verificar se um CTO com MRR 100.000 ganha exatamente a pontuação máxima (50+40+50 = 140).
  - `testScoreForSmallBusiness()`: Verificar empresa MICRO e cargo operacional.
  - `testScoreWithIcebreakerBio()`: Validar a soma dos +10 pontos se o campo `bio` vier preenchido.

### 3. Testes de Integração (WebMVC)
#### [NEW] `backend/src/test/java/.../api/controller/LeadControllerTest.java`
- **Cenários a Cobrir:**
  - `testWebhookIngestionSuccess()`: Enviar um JSON de Webhook válido e usar `@MockBean` para verificar se os Repositories e o Kafka foram acionados.
  - `testSearchLeadsWithMrrFilter()`: Chamar o endpoint `GET /api/leads/search?minMrr=10000` e garantir que o Pageable retorna Status 200 OK e converte os parâmetros corretamente.

### 4. Testes de Fila (Queue Logic)
#### [NEW] `backend/src/test/java/.../queue/service/InteractionQueueServiceTest.java`
- **Cenários a Cobrir:**
  - `testAssignLeadToAvailableAgent()`: Simular que há agentes "AVAILABLE" e verificar se o sistema cria o `InteractionQueue` com status `WAITING`.

---

## Verification Plan
1. Após escrever os testes, executarei o comando:
   ```bash
   ./mvnw clean test jacoco:report
   ```
2. Vou analisar o painel do JaCoCo (gerado em `target/site/jacoco/index.html`) e comprovarei no Walkthrough que alcançamos cobertura substancial nos Controllers e Services críticos.
