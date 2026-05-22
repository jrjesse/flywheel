# Goal Description

The objective is to enrich the Lead data with a "Source" (Origem) attribute to track where leads are coming from (e.g., Landing Page, Website, Instagram, etc.). Additionally, the Dashboard will be upgraded to feature advanced analytics: a Bar Chart visualizing lead distribution by source, a forecasted revenue metric (based on Lead MRR and Pipeline status), and a pending tasks overview based on the Kanban board.

## User Review Required

> [!IMPORTANT]
> **Recharts Library:** We will need to install the `recharts` library on the frontend to render the Bar Chart. Are you okay with adding this dependency?
> 
> **Source Options:** The plan is to allow any string as a source, but primarily expect values like `LANDING_PAGE`, `WEBSITE`, `INSTAGRAM_CAMPAIGN`, `LINKEDIN`. Do you want to enforce specific Enums, or keep it as a flexible text field?
>
> **Revenue Forecast Logic:** I will sum the `mrr` (Monthly Recurring Revenue) of all leads that are currently in the Kanban pipeline (e.g., `PENDING`, `NEGOTIATING`) to generate the Forecasted Revenue. Is this logic aligned with your expectations?

## Proposed Changes

---

### Backend Components

#### [MODIFY] backend/src/main/java/com/antigravity/sales/core/model/Lead.java
- Add `private String source;` attribute to track the origin of the lead.

#### [MODIFY] backend/src/main/java/com/antigravity/sales/api/dto/LeadRequest.java
- Add `private String source;` to allow incoming webhooks to specify the lead source.

---

### Frontend Components

#### [MODIFY] frontend/package.json
- Install `recharts` for data visualization (`npm install recharts`).

#### [MODIFY] frontend/src/app/page.tsx
- **Typing Updates**: Update the `Lead` interface to include `source` and `company: { mrr: number }`.
- **Metrics Calculation**:
  - Implement logic to aggregate leads by `source` for the chart.
  - Calculate "Forecasted Revenue" by summing the `mrr` of leads in active Kanban stages.
  - Calculate "Pending Tasks" (Leads in `PENDING` status waiting for action).
- **UI Enhancements**:
  - Add a new "Faturamento Previsto" (Forecasted Revenue) KPI Card.
  - Add a "Tarefas Pendentes" (Pending Tasks) KPI Card.
  - Render a `BarChart` from `recharts` displaying the volume of leads per source.

## Verification Plan

### Automated Tests
- Run backend tests (`./mvnw clean test`) to ensure the new field doesn't break Webhook ingestion or existing tests.

### Manual Verification
- Send a test payload via `curl` including `"source": "INSTAGRAM_CAMPAIGN"`.
- Verify the lead appears in the Dashboard.
- Verify the Bar Chart reflects the new lead under the "Instagram" column.
- Verify the "Forecasted Revenue" updates if the lead is moved to the Kanban with an MRR value.
