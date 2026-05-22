# Integrations Module Plan

## Overview
Create a centralized "Hub de Integrações" to allow tenants to manually configure integrations with Google Forms, WhatsApp Official API, and Instagram API. This will provide a single scalable screen to manage all external inbound/outbound connections for the CRM.

## Project Type
WEB + BACKEND

## Success Criteria
- Users can navigate to `/settings/integrations` via Sidebar.
- Users can view connection cards for WhatsApp, Instagram, and Google Forms.
- Users can manually input and save API tokens, WABA IDs, etc., for each integration.
- The backend successfully stores these configurations per tenant.
- Existing WhatsApp configuration (`WhatsAppChannelConfig`) is integrated into this new hub.

## Tech Stack
- Frontend: Next.js (App Router), Tailwind CSS, Lucide Icons.
- Backend: Spring Boot, Spring Data JPA (PostgreSQL).

## File Structure

### Frontend
- [MODIFY] `frontend/src/components/layout/Sidebar.tsx` -> Change "Config. WhatsApp" to "Integrações" pointing to `/settings/integrations`.
- [DELETE] `frontend/src/app/settings/whatsapp/page.tsx` -> Replaced by the hub.
- [NEW] `frontend/src/app/settings/integrations/page.tsx` -> The main hub layout with connection cards.
- [NEW] `frontend/src/components/integrations/IntegrationCard.tsx` -> UI Component for a single integration.
- [NEW] `frontend/src/components/integrations/WhatsAppSettingsModal.tsx` -> Modal to edit WhatsApp tokens.
- [NEW] `frontend/src/components/integrations/InstagramSettingsModal.tsx` -> Modal to edit Instagram tokens.
- [NEW] `frontend/src/components/integrations/GoogleFormsSettingsModal.tsx` -> Modal to configure Webhook/Forms mapping.

### Backend
- [NEW] `backend/src/main/java/com/antigravity/sales/queue/model/InstagramChannelConfig.java` -> Entity for Instagram.
- [NEW] `backend/src/main/java/com/antigravity/sales/queue/model/GoogleFormsConfig.java` -> Entity for Google Forms.
- [NEW] `backend/src/main/java/com/antigravity/sales/queue/repository/InstagramChannelConfigRepository.java`
- [NEW] `backend/src/main/java/com/antigravity/sales/queue/repository/GoogleFormsConfigRepository.java`
- [NEW] `backend/src/main/java/com/antigravity/sales/queue/api/InstagramConfigController.java`
- [NEW] `backend/src/main/java/com/antigravity/sales/queue/api/GoogleFormsConfigController.java`

## Task Breakdown

### Task 1: Database Entities & Repositories
- **Agent:** backend-specialist
- **Skill:** database-design
- **INPUT:** Need tables to store tokens for Instagram and Google Forms.
- **OUTPUT:** `InstagramChannelConfig`, `GoogleFormsConfig` JPA entities and repositories.
- **VERIFY:** Application starts successfully without Hibernate errors.

### Task 2: Backend Config Controllers & Services
- **Agent:** backend-specialist
- **Skill:** api-patterns
- **INPUT:** Entities created in Task 1.
- **OUTPUT:** REST API endpoints (GET/POST) for saving/fetching Instagram and Google Forms configs.
- **VERIFY:** Endpoints respond to standard HTTP requests locally.

### Task 3: Frontend Sidebar & Hub UI Component
- **Agent:** frontend-specialist
- **Skill:** frontend-design
- **INPUT:** Sidebar pointing to `/settings/whatsapp`.
- **OUTPUT:** Sidebar updated to `/settings/integrations`. `page.tsx` grid layout with 3 `IntegrationCard` components.
- **VERIFY:** UI renders beautifully with modern cards and icons.

### Task 4: Frontend Settings Modals (Manual Entry)
- **Agent:** frontend-specialist
- **Skill:** frontend-design
- **INPUT:** `IntegrationCard` clicks.
- **OUTPUT:** Modals/Drawers where users paste their tokens (WhatsApp, Instagram, Google Forms) and save to backend.
- **VERIFY:** Form submissions hit the backend API successfully.

## ✅ PHASE X COMPLETE
- [ ] Lint: Pass
- [ ] Security: No critical issues
- [ ] Build: Success
