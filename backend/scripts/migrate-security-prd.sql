-- =============================================================================
-- Security PRD Migration Script
-- Sales Automation API — PostgreSQL 15+
--
-- Run BEFORE switching to spring.profiles.active=prod (ddl-auto=validate).
--
-- Usage:
--   psql -U admin -d salesdb -f backend/scripts/migrate-security-prd.sql
--
-- IMPORTANT:
--   1. Back up the database before running.
--   2. Replace :DEFAULT_TENANT_ID with your real tenant UUID after step 1,
--      or use the UUID returned by the INSERT in step 1.
--   3. Existing admin users must be created via POST /api/auth/register
--      OR manually in the users table (see step 8).
--   4. Webhook secrets for existing tenants must be regenerated (step 9).
-- =============================================================================

BEGIN;

-- ---------------------------------------------------------------------------
-- 0. Extensions (SHA-256 for Google Forms token hashing)
-- ---------------------------------------------------------------------------
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ---------------------------------------------------------------------------
-- 1. Default tenant for orphan records (skip if tenants already exist)
-- ---------------------------------------------------------------------------
INSERT INTO tenants (id, name, document, document_type, created_at)
SELECT
    gen_random_uuid(),
    'Tenant Migrado (Legacy)',
    '00000000000000',
    'CNPJ',
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM tenants LIMIT 1);

-- Capture default tenant id (use this value in backfill below)
-- SELECT id FROM tenants ORDER BY created_at ASC LIMIT 1;

-- ---------------------------------------------------------------------------
-- 2. leads — add tenant_id
-- ---------------------------------------------------------------------------
ALTER TABLE leads
    ADD COLUMN IF NOT EXISTS tenant_id UUID;

UPDATE leads l
SET tenant_id = (SELECT id FROM tenants ORDER BY created_at ASC LIMIT 1)
WHERE l.tenant_id IS NULL;

ALTER TABLE leads
    ALTER COLUMN tenant_id SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_leads_tenant_id ON leads (tenant_id);
CREATE INDEX IF NOT EXISTS idx_leads_tenant_status ON leads (tenant_id, status);

ALTER TABLE leads
    ADD COLUMN IF NOT EXISTS assigned_to_user_id UUID;

CREATE INDEX IF NOT EXISTS idx_leads_assigned_to_user ON leads (tenant_id, assigned_to_user_id);

-- ---------------------------------------------------------------------------
-- 3. lead_interactions — add tenant_id + created_by_user_id
-- ---------------------------------------------------------------------------
ALTER TABLE lead_interactions
    ADD COLUMN IF NOT EXISTS tenant_id UUID,
    ADD COLUMN IF NOT EXISTS created_by_user_id UUID;

UPDATE lead_interactions li
SET tenant_id = l.tenant_id
FROM leads l
WHERE li.lead_id = l.id
  AND li.tenant_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_lead_interactions_tenant_id ON lead_interactions (tenant_id);

-- created_by_user_id stays NULL for legacy rows (not editable under new rules)

-- ---------------------------------------------------------------------------
-- 4. system_notifications — add tenant_id
-- ---------------------------------------------------------------------------
ALTER TABLE system_notifications
    ADD COLUMN IF NOT EXISTS tenant_id UUID;

UPDATE system_notifications sn
SET tenant_id = l.tenant_id
FROM leads l
WHERE sn.lead_id = l.id
  AND sn.tenant_id IS NULL;

UPDATE system_notifications
SET tenant_id = (SELECT id FROM tenants ORDER BY created_at ASC LIMIT 1)
WHERE tenant_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_system_notifications_tenant_id ON system_notifications (tenant_id);

-- ---------------------------------------------------------------------------
-- 5. google_forms_config — webhook_token → webhook_token_hash
-- ---------------------------------------------------------------------------
ALTER TABLE google_forms_config
    ADD COLUMN IF NOT EXISTS webhook_token_hash VARCHAR(255);

-- Migrate plaintext tokens to SHA-256 hex (matches WebhookTokenHasher in Java)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'google_forms_config' AND column_name = 'webhook_token'
    ) THEN
        UPDATE google_forms_config
        SET webhook_token_hash = encode(digest(webhook_token, 'sha256'), 'hex')
        WHERE webhook_token IS NOT NULL
          AND webhook_token_hash IS NULL;

        ALTER TABLE google_forms_config DROP COLUMN webhook_token;
    END IF;
END $$;

-- ---------------------------------------------------------------------------
-- 6. New security tables (safe IF NOT EXISTS via Hibernate naming)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    display_name    VARCHAR(255) NOT NULL,
    tenant_id       UUID NOT NULL,
    role            VARCHAR(20) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_tenant_id ON users (tenant_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);

CREATE TABLE IF NOT EXISTS tenant_webhook_secrets (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id    UUID NOT NULL UNIQUE,
    secret_hash  VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS audit_logs (
    id            BIGSERIAL PRIMARY KEY,
    tenant_id     UUID,
    user_id       UUID,
    action        VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id   VARCHAR(100),
    ip_address    VARCHAR(45),
    user_agent    TEXT,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_tenant_id ON audit_logs (tenant_id, created_at DESC);

-- ---------------------------------------------------------------------------
-- 7. Integration configs — ensure client_id aligns with tenant
--    (client_id should equal tenant UUID; fix mismatches manually if needed)
-- ---------------------------------------------------------------------------
UPDATE whatsapp_channel_config w
SET client_id = (SELECT id FROM tenants ORDER BY created_at ASC LIMIT 1)
WHERE w.client_id IS NULL;

UPDATE instagram_channel_config i
SET client_id = (SELECT id FROM tenants ORDER BY created_at ASC LIMIT 1)
WHERE i.client_id IS NULL;

UPDATE google_forms_config g
SET client_id = (SELECT id FROM tenants ORDER BY created_at ASC LIMIT 1)
WHERE g.client_id IS NULL;

-- ---------------------------------------------------------------------------
-- 8. Verification queries (run manually after COMMIT)
-- ---------------------------------------------------------------------------
-- SELECT COUNT(*) AS leads_sem_tenant FROM leads WHERE tenant_id IS NULL;
-- SELECT COUNT(*) AS interactions_sem_tenant FROM lead_interactions WHERE tenant_id IS NULL;
-- SELECT COUNT(*) AS notifications_sem_tenant FROM system_notifications WHERE tenant_id IS NULL;
-- SELECT id, name FROM tenants;

COMMIT;

-- ---------------------------------------------------------------------------
-- POST-MIGRATION (application-level, not SQL)
-- ---------------------------------------------------------------------------
--
-- A) Create admin user for each existing tenant:
--    POST /api/auth/register  (if tenant is new)
--    OR insert BCrypt hash manually into users table.
--
-- B) Generate webhook secret per tenant:
--    Register a new tenant via /api/auth/register (returns webhookSecret once)
--    OR call AuthService.generateWebhookSecret() in a one-time admin script.
--
-- C) Rotate all secrets that were ever committed to git:
--    - JWT_SECRET
--    - CRYPTO_MASTER_KEY
--    - DATABASE_PASSWORD
--    - STRIPE_API_KEY / STRIPE_WEBHOOK_SECRET
--
-- D) Set environment variables (see .env.example) and start with:
--    SPRING_PROFILES_ACTIVE=prod
