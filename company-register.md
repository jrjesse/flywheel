# Company Onboarding (company-register)

## Overview
A 4-step wizard for new companies (Tenants) to set up their workspace in the CRM. The wizard collects Name/Document, Phone, Niche, and Address. The progress is saved in `localStorage` to prevent data loss. The backend will persist this data in a new `Tenant` entity.

## Project Type
WEB + BACKEND

## Success Criteria
- User can navigate to `/company-register`.
- The wizard displays 4 distinct steps with "Next" and "Go Back" buttons.
- State is preserved across reloads using `localStorage`.
- Step 1 requires valid CPF or the new Alphanumeric CNPJ (modulo 11 algorithm with letter-to-number mapping).
- Submitting step 4 sends a POST request to backend.
- Backend saves data in a new `Tenant` table.

## Tech Stack
- Frontend: Next.js (App Router), Tailwind CSS, React Context/Hooks (`localStorage`).
- Backend: Spring Boot, Spring Data JPA (PostgreSQL).

## File Structure

### Frontend
- [NEW] `frontend/src/app/company-register/page.tsx` -> The main wizard orchestrator.
- [NEW] `frontend/src/app/company-register/layout.tsx` -> Minimal layout (no sidebar) for onboarding focus.
- [NEW] `frontend/src/components/onboarding/Step1Company.tsx`
- [NEW] `frontend/src/components/onboarding/Step2Phone.tsx`
- [NEW] `frontend/src/components/onboarding/Step3Niche.tsx`
- [NEW] `frontend/src/components/onboarding/Step4Address.tsx`
- [NEW] `frontend/src/hooks/useLocalStorage.ts`
- [NEW] `frontend/src/utils/documentValidation.ts` -> Alphanumeric CNPJ logic.

### Backend
- [NEW] `backend/src/main/java/com/antigravity/sales/core/model/Tenant.java`
- [NEW] `backend/src/main/java/com/antigravity/sales/core/repository/TenantRepository.java`
- [NEW] `backend/src/main/java/com/antigravity/sales/api/controller/TenantController.java`
- [NEW] `backend/src/main/java/com/antigravity/sales/api/dto/TenantRequest.java`

## Task Breakdown

### Task 1: Backend Tenant Entity & API
- **Agent:** backend-specialist
- **Skill:** database-design, api-patterns
- **INPUT:** Need `Tenant` entity (name, document, phone, niche, address) and `POST /api/tenants`.
- **OUTPUT:** JPA Entity, Repository, and Controller.
- **VERIFY:** POST request creates a new Tenant in the database.

### Task 2: Frontend Validation Logic
- **Agent:** frontend-specialist
- **Skill:** clean-code
- **INPUT:** Need validation for new Alphanumeric CNPJ (Base 14 chars, A=10, B=11...).
- **OUTPUT:** `documentValidation.ts` file with `validateAlphanumericCNPJ` and `validateCPF` functions.
- **VERIFY:** Passes standard valid CNPJs (numeric and alphanumeric) and fails invalid ones.

### Task 3: Frontend Wizard Layout & State
- **Agent:** frontend-specialist
- **Skill:** frontend-design
- **INPUT:** Needs a 4-step wizard architecture that saves to `localStorage`.
- **OUTPUT:** `company-register/page.tsx`, `layout.tsx`, and state logic.
- **VERIFY:** User can move between steps. Reloading the page keeps the user on the current step with data intact.

### Task 4: Frontend Wizard Steps
- **Agent:** frontend-specialist
- **Skill:** frontend-design
- **INPUT:** 4 UI components for the steps.
- **OUTPUT:** `Step1Company` (with CNPJ/CPF format), `Step2Phone`, `Step3Niche`, `Step4Address`.
- **VERIFY:** Step 4 "Finish" button triggers backend POST and clears `localStorage` upon success.

## ✅ PHASE X COMPLETE
- [ ] Lint: Pass
- [ ] Security: No critical issues
- [ ] Build: Success
