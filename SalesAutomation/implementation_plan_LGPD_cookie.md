# Plano de Implementação: Consentimento de Cookies LGPD

Este plano descreve como vamos implementar o gerenciamento de cookies para o front-end, cumprindo os requisitos da LGPD (Lei Geral de Proteção de Dados) para um SaaS em produção, além de seguir as diretrizes de design do *Flywheel CRM*.

## Resumo do Problema
SaaS em produção exige o consentimento explícito dos usuários para uso de cookies não essenciais (Analíticos, Marketing, etc.). Precisamos de uma interface amigável e uma lógica sólida que controle o carregamento de scripts de terceiros com base no consentimento.

> [!IMPORTANT]
> A LGPD estipula que **não se pode pré-marcar** opções de cookies não essenciais, e o usuário deve ter uma maneira fácil de rejeitá-los. 

## Proposta de Mudanças

### 1. Componentes da Interface (UI)
Vamos criar um componente principal chamado `CookieConsent.tsx` no diretório de componentes globais (`src/components/layout/` ou similar). O componente terá dois estados de visualização:
- **Banner Direto (Minimalista):** Aparece na parte inferior da tela com um texto claro, um botão principal de "Aceitar Todos" e outro de "Gerenciar Preferências".
- **Modal de Preferências:** Ao clicar em "Gerenciar Preferências", abre-se um modal (dialog) exibindo as categorias:
  - Estritamente Necessários (Sempre ativos, inalteráveis)
  - Analíticos (Desativado por padrão)
  - Marketing (Desativado por padrão)

> [!NOTE]
> Estilo: Utilizaremos **Flat Design**, com botões preenchidos em `#2563EB` (Primary) e `#F97316` (CTA) conforme definido no Design System gerado pelo workflow `ui-ux-pro-max`. Evitaremos sombras complexas ou modais poluídos, focando em botões `cursor-pointer` com microinterações rápidas (150-200ms).

### 2. Gerenciamento de Estado
Vamos criar um hook customizado `useCookieConsent.ts` na pasta `src/hooks/` (ou onde utilitários são armazenados).
- O estado de consentimento será armazenado em `localStorage` sob a chave `flywheel_cookie_consent`.
- A estrutura do objeto salvo será: `{ necessary: true, analytics: false, marketing: false, version: "1.0", date: "..." }`.

### 3. Integração com Layout
#### [MODIFY] `src/app/layout.tsx`
Adicionaremos o componente `<CookieConsent />` antes do fechamento da tag `</body>`. Isso garantirá que ele renderize em qualquer página da aplicação até que a decisão seja tomada.

## Open Questions

> [!WARNING]
> **Integração de Scripts:** Existem scripts de terceiros específicos (como Google Analytics, Meta Pixel, Hotjar) que já precisamos inserir neste momento baseados no consentimento, ou deseja que eu crie apenas o "esqueleto" que gerencia e guarda as permissões para futuras integrações?

## Plano de Verificação

### Teste Manual
1. Abrir a aplicação em uma aba anônima (sem o localStorage prévio).
2. Verificar se o banner aparece no rodapé com o estilo Flat e minimalista aprovado.
3. Clicar em "Gerenciar Preferências", interagir com os toggles e salvar.
4. Recarregar a página e garantir que o banner não apareça mais.
5. Inspecionar o `localStorage` no navegador para verificar a estrutura de dados correta do consentimento.
