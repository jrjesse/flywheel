# Sales Automation System

Sistema de ingestão e automação de funil comercial com **Spring Boot 4**, **PostgreSQL**, **Kafka** e camada de segurança production-ready (JWT, multi-tenant, webhooks autenticados, criptografia AES-GCM).

## Stack

| Camada | Tecnologia |
|--------|------------|
| Backend | Java 21, Spring Boot 4.0, Spring Security, JWT |
| Banco | PostgreSQL 15 |
| Mensageria | Apache Kafka |
| Billing | Stripe |
| Infra local | Docker Compose (Postgres + Kafka) |

> O frontend (Next.js 16 + React 19) está em `frontend/` — integrado ao monorepo com auth JWT e UI por perfil.

## Features

- **Ingestão Webhook** autenticada por tenant (leads, WhatsApp, Google Forms)
- **Autenticação JWT** com RBAC (`ADMIN`, `MANAGER`, `AGENT`, `VIEWER`)
- **Multi-tenant** — isolamento de dados por tenant
- **Kanban de leads** — status, interações, propostas PDF
- **Fila WhatsApp** — debounce, SLA, transbordo, heartbeat de agentes
- **Criptografia AES-GCM** para tokens e credenciais em repouso
- **Audit log** — rastreabilidade de ações (LGPD)
- **Rate limiting** — proteção contra abuso

---

## Quick Start (desenvolvimento)

### 1. Subir infraestrutura

```bash
docker compose up -d
```

### 2. Configurar variáveis

Copie o template e ajuste se necessário:

```bash
cp .env.example .env
```

Em dev, os defaults em `application-dev.properties` são suficientes.

### 3. Rodar o backend

```bash
cd backend
./mvnw spring-boot:run
```

API disponível em `http://localhost:8080`.

### 4. Registrar o primeiro tenant

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "tenantName": "Minha Empresa",
    "document": "12345678000199",
    "documentType": "CNPJ",
    "email": "admin@empresa.com",
    "password": "senha-segura-123",
    "displayName": "Admin"
  }'
```

**Guarde a resposta** — ela contém:
- `token` — JWT para chamadas autenticadas
- `webhookSecret` — exibido **apenas uma vez**, necessário para ingestão de leads

### 5. Login (sessões seguintes)

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@empresa.com","password":"senha-segura-123"}'
```

### 6. Chamada autenticada

```bash
curl http://localhost:8080/api/leads \
  -H "Authorization: Bearer SEU_JWT_AQUI"
```

### 7. Frontend (opcional)

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

UI em `http://localhost:3000` — login, kanban, pool de leads, equipe (admin), fila WhatsApp.

### 8. Smoke test (API)

```bash
./scripts/smoke-test.sh http://localhost:8080
```

### 9. Stack staging (Docker)

```bash
docker compose -f docker-compose.staging.yml up --build -d
```

---

## Autenticação e autorização

### Roles

| Role | Permissões |
|------|------------|
| `ADMIN` | Configurações, integrações, audit log, propostas, atribuir leads |
| `MANAGER` | Propostas, gestão de leads, fila, atribuir leads |
| `AGENT` | Kanban (leads próprios), interações, fila, claim de leads |
| `VIEWER` | Leitura de todos os leads |

### Gestão de equipe (ADMIN)

O administrador pode convidar vendedores e gerentes via API:

```bash
# Convidar vendedor (role AGENT)
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer SEU_JWT_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "vendedor@empresa.com",
    "displayName": "João Vendedor",
    "password": "senha-segura-123",
    "role": "AGENT"
  }'

# Listar equipe do tenant
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer SEU_JWT_ADMIN"

# Alterar role
curl -X PATCH http://localhost:8080/api/users/{userId}/role \
  -H "Authorization: Bearer SEU_JWT_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"role": "MANAGER"}'

# Desativar vendedor
curl -X PATCH http://localhost:8080/api/users/{userId}/deactivate \
  -H "Authorization: Bearer SEU_JWT_ADMIN"
```

Roles permitidas no convite: `AGENT` (vendedor), `MANAGER`, `VIEWER`. Apenas o registro inicial cria `ADMIN`.

### Escopo por vendedor (Fase 3)

Leads podem ser atribuídos a um vendedor (`assignedToUserId`). Regras de visibilidade:

| Role | O que vê |
|------|----------|
| `AGENT` | Apenas leads atribuídos a si |
| `ADMIN`, `MANAGER`, `VIEWER` | Todos os leads do tenant |

```bash
# Pool de leads sem dono (ADMIN, MANAGER, AGENT)
curl http://localhost:8080/api/leads/unassigned \
  -H "Authorization: Bearer SEU_JWT"

# Vendedor assume um lead do pool
curl -X PATCH http://localhost:8080/api/leads/{leadId}/claim \
  -H "Authorization: Bearer SEU_JWT_VENDEDOR"

# Admin/gerente atribui lead a um vendedor
curl -X PATCH http://localhost:8080/api/leads/{leadId}/assign \
  -H "Authorization: Bearer SEU_JWT_ADMIN" \
  -H "Content-Type: application/json" \
  -d '{"userId": "UUID-DO-VENDEDOR"}'
```

Leads criados via webhook entram sem atribuição. Ao registrar interação ou alterar status, o vendedor logado recebe o lead automaticamente se ainda estiver sem dono.

### Endpoints públicos (sem JWT)

| Endpoint | Proteção |
|----------|----------|
| `POST /api/auth/register` | Aberto (criar tenant) |
| `POST /api/auth/login` | Aberto |
| `POST /api/webhooks/leads` | `X-Webhook-Secret` |
| `POST /api/webhooks/whatsapp/{tenantId}` | HMAC `X-Hub-Signature-256` |
| `POST /api/webhooks/google-forms/{token}` | Token na URL |
| `POST /api/webhooks/stripe` | Assinatura Stripe |

Todos os demais endpoints exigem `Authorization: Bearer {jwt}`.

---

## Webhooks

### Ingestão de leads

```bash
curl -X POST http://localhost:8080/api/webhooks/leads \
  -H "X-Webhook-Secret: SEU_WEBHOOK_SECRET" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@empresa.com",
    "phone": "+5511999999999",
    "companyName": "Acme Corp",
    "role": "CEO",
    "source": "LANDING_PAGE"
  }'
```

### WhatsApp (Meta HMAC)

```
POST /api/webhooks/whatsapp/{tenantId}?leadId=123&text=Olá
Header: X-Hub-Signature-256: sha256={hmac_do_body}
```

### Google Forms

```
POST /api/webhooks/google-forms/{token}
Body: LeadIngestionRequest JSON
```

O token é gerado em `PUT /api/settings/google-forms/{clientId}` (campo `webhookToken` na resposta, exibido uma vez).

---

## Variáveis de ambiente (produção)

Ver [`.env.example`](.env.example). Obrigatórias em prod:

| Variável | Descrição |
|----------|-----------|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `JWT_SECRET` | Mínimo 32 caracteres |
| `CRYPTO_MASTER_KEY` | Mínimo 32 caracteres (AES-GCM) |
| `DATABASE_URL` | JDBC PostgreSQL |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | Credenciais do banco |
| `CORS_ALLOWED_ORIGINS` | Origens permitidas (sem `*`) |
| `STRIPE_API_KEY` / `STRIPE_WEBHOOK_SECRET` | Billing |

---

## Deploy em produção

### Checklist

- [ ] Rodar script de migração SQL (se houver dados existentes)
- [ ] Rotacionar todos os secrets que já estiveram no git
- [ ] Definir variáveis de ambiente (secret manager recomendado)
- [ ] `SPRING_PROFILES_ACTIVE=prod` (`ddl-auto=validate`)
- [ ] HTTPS na frente da API (TLS 1.2+)
- [ ] PostgreSQL e Kafka não expostos publicamente
- [ ] Smoke test: register → login → GET /api/leads → webhook com secret

### Build Docker

```bash
cd backend
docker build -t sales-automation-api .
```

---

## Migração de dados existentes

Se você já tinha dados antes da implementação de segurança, execute:

```bash
psql -U admin -d salesdb -f backend/scripts/migrate-security-prd.sql
```

O script:
1. Cria tenant padrão para registros órfãos
2. Adiciona `tenant_id` e `assigned_to_user_id` em leads
3. Adiciona `tenant_id` em interações e notificações
3. Migra `webhook_token` → `webhook_token_hash` (Google Forms)
4. Cria tabelas `users`, `tenant_webhook_secrets`, `audit_logs`

**Após a migração SQL**, ainda é necessário:
- Criar usuários admin (`POST /api/auth/register` ou insert manual com BCrypt)
- Gerar webhook secrets por tenant (via register ou script admin)
- Rotacionar secrets expostos no histórico do git

---

## Breaking changes (API)

| Antes | Depois |
|-------|--------|
| `POST /api/leads/webhook` | `POST /api/webhooks/leads` + `X-Webhook-Secret` |
| `POST /api/queue/webhook` | `POST /api/webhooks/whatsapp/{tenantId}` + HMAC |
| `POST /api/tenants` | `POST /api/auth/register` |
| API aberta | JWT obrigatório |
| Config retorna tokens | Config retorna `hasAccessToken: true` |

---

## Testes

```bash
cd backend
./mvnw test -Dspring.profiles.active=test
```

Inclui testes de segurança (`SecurityIntegrationTest`) e criptografia (`AesGcmCryptoServiceTest`).

CI: [`.github/workflows/security.yml`](.github/workflows/security.yml)

---

## Estrutura do backend

```
backend/src/main/java/com/antigravity/sales/
├── api/controller/     # REST (leads, auth, webhooks, settings)
├── core/               # Domínio, serviços, repositórios
├── queue/              # Fila WhatsApp, agentes, SLA jobs
├── billing/            # Stripe, faturas
├── messaging/          # Kafka producer/consumer
└── security/           # JWT, CORS, rate limit, crypto, audit
```

---

## Diferencial — Kanban Module

Painel Kanban dinâmico para gestão de leads com drag-and-drop, atualização de status em tempo real e histórico completo de interações, com controle de SLA operacional.
