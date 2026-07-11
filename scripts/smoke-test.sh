#!/usr/bin/env bash
# Smoke test — Sales Automation API
# Usage: ./scripts/smoke-test.sh [BASE_URL]
# Example: ./scripts/smoke-test.sh http://localhost:8080

set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
RANDOM_SUFFIX=$(date +%s)
ADMIN_EMAIL="smoke-admin-${RANDOM_SUFFIX}@test.local"
ADMIN_PASS="smoke-pass-123456"
VENDOR_EMAIL="smoke-vendor-${RANDOM_SUFFIX}@test.local"
VENDOR_PASS="vendor-pass-123456"

fail() { echo "FAIL: $1" >&2; exit 1; }
ok() { echo "OK: $1"; }

echo "=== Smoke test against ${BASE_URL} ==="

# Health / register
REGISTER_BODY=$(cat <<EOF
{
  "tenantName": "Smoke Corp ${RANDOM_SUFFIX}",
  "document": "${RANDOM_SUFFIX}000199",
  "documentType": "CNPJ",
  "email": "${ADMIN_EMAIL}",
  "password": "${ADMIN_PASS}",
  "displayName": "Smoke Admin"
}
EOF
)

REGISTER_RES=$(curl -sf -X POST "${BASE_URL}/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "${REGISTER_BODY}") || fail "register failed"

ADMIN_TOKEN=$(echo "$REGISTER_RES" | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")
WEBHOOK_SECRET=$(echo "$REGISTER_RES" | python3 -c "import sys,json; print(json.load(sys.stdin)['webhookSecret'])")
ok "register"

# Login
LOGIN_RES=$(curl -sf -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${ADMIN_EMAIL}\",\"password\":\"${ADMIN_PASS}\"}") || fail "login failed"
LOGIN_TOKEN=$(echo "$LOGIN_RES" | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")
[[ -n "$LOGIN_TOKEN" ]] || fail "empty login token"
ok "login"

# List leads (empty)
curl -sf "${BASE_URL}/api/leads" -H "Authorization: Bearer ${ADMIN_TOKEN}" > /dev/null || fail "list leads"
ok "list leads"

# Webhook ingest
curl -sf -X POST "${BASE_URL}/api/webhooks/leads" \
  -H "Content-Type: application/json" \
  -H "X-Webhook-Secret: ${WEBHOOK_SECRET}" \
  -d '{"name":"Smoke Lead","email":"lead@test.local"}' > /dev/null || fail "webhook ingest"
ok "webhook ingest"

# Invite vendor
INVITE_RES=$(curl -sf -X POST "${BASE_URL}/api/users" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${VENDOR_EMAIL}\",\"displayName\":\"Smoke Vendor\",\"password\":\"${VENDOR_PASS}\",\"role\":\"AGENT\"}") || fail "invite vendor"
VENDOR_ID=$(echo "$INVITE_RES" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])")
ok "invite vendor"

# Vendor login
VENDOR_LOGIN=$(curl -sf -X POST "${BASE_URL}/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"${VENDOR_EMAIL}\",\"password\":\"${VENDOR_PASS}\"}")
VENDOR_TOKEN=$(echo "$VENDOR_LOGIN" | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

# Vendor sees 0 leads initially
VENDOR_LEADS=$(curl -sf "${BASE_URL}/api/leads" -H "Authorization: Bearer ${VENDOR_TOKEN}")
COUNT=$(echo "$VENDOR_LEADS" | python3 -c "import sys,json; print(len(json.load(sys.stdin)))")
[[ "$COUNT" == "0" ]] || fail "vendor should see 0 leads before claim (got ${COUNT})"
ok "vendor isolation"

# Unassigned pool
UNASSIGNED=$(curl -sf "${BASE_URL}/api/leads/unassigned" -H "Authorization: Bearer ${VENDOR_TOKEN}")
LEAD_ID=$(echo "$UNASSIGNED" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d[0]['id'] if d else '')")
[[ -n "$LEAD_ID" ]] || fail "no unassigned lead"
ok "unassigned pool"

# Claim
curl -sf -X PATCH "${BASE_URL}/api/leads/${LEAD_ID}/claim" \
  -H "Authorization: Bearer ${VENDOR_TOKEN}" > /dev/null || fail "claim failed"
ok "claim lead"

# Protected route without token
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/api/leads")
[[ "$HTTP_CODE" == "401" || "$HTTP_CODE" == "403" ]] || fail "expected 401/403 without token (got ${HTTP_CODE})"
ok "auth enforced"

echo ""
echo "=== All smoke tests passed ==="
