# Gerador de Proposta Comercial e Integração Kanban

Este plano detalha a implementação do fluxo de geração de propostas comerciais em PDF, envio por e-mail e atualização automática de status no Kanban.

## User Review Required

> [!WARNING]
> **Biblioteca PDF:** O backend do projeto utiliza **Java (Spring Boot)**. As opções sugeridas (PDF-lib, jsPDF, Puppeteer) são para ecossistema Node/JavaScript. Portanto, para mantermos a consistência com o stack atual e evitar complexidade de microserviços, utilizaremos a biblioteca **OpenPDF** (uma ramificação open-source do iText, sem problemas de licenciamento comercial) para manipular e preencher o PDF no backend Java.

## Open Questions

> [!IMPORTANT]
> Precisamos esclarecer os seguintes pontos antes de iniciar o desenvolvimento:
> 
> 1. **Template PDF Base:** Você mencionou que o template da "Ring Tecnologia" estava em anexo/descrito, mas eu não recebi o arquivo. Por favor, forneça o template em PDF ou confirme se devo criar um arquivo PDF gerado dinamicamente do zero (com o layout descrito) para servir como base inicial.
> 2. **Remetente do E-mail:** O texto do prompt cita duas opções de remetente: `comercial@nomedaempresa.com.br` e `financeiro@nomedaempresa.com.br`. Qual deve ser o padrão utilizado?
> 3. **Destinatário:** O e-mail deve ser enviado para o e-mail cadastrado no próprio Prospect (`lead.email`), correto?
> 4. **Credenciais SMTP:** Para que o `Spring Mail` consiga de fato disparar os e-mails, precisaremos das credenciais do servidor SMTP (host, porta, usuário, senha). Deseja que eu deixe essas configurações mockadas (apenas imprimindo no log que o e-mail foi "enviado") para você configurar depois nas variáveis de ambiente?

## Proposed Changes

### Backend (Java / Spring Boot)

*   **Adicionar Dependências (`pom.xml`)**:
    *   `com.github.librepdf:openpdf` para manipulação e preenchimento de templates PDF.
    *   `spring-boot-starter-mail` para envio de e-mails via SMTP.
    *   `spring-boot-starter-thymeleaf` para processamento do template do corpo do e-mail.
*   **Serviços e Controladores**:
    *   **[NEW] `ProposalPdfService`**: Responsável por ler o template PDF, substituir os valores (Nome do Prospect, Valor da Proposta, Tabela de Condições e Data) e retornar um array de bytes do PDF gerado.
    *   **[NEW] `EmailService`**: Responsável por construir o e-mail profissional em HTML usando Thymeleaf e anexar o PDF gerado.
    *   **[NEW] `ProposalController`**: Endpoint `POST /api/leads/{id}/proposal`. Recebe o valor editado e nome, coordena a geração do PDF, o envio do e-mail e atualiza o `status` do Lead para `PROPOSAL_SENT`.
*   **Configuração (`application.properties`)**:
    *   Adição das propriedades de configuração do `spring.mail.*`.

---

### Frontend (React / Tailwind)

*   **Kanban Board (`leads/page.tsx`)**:
    *   **[MODIFY]**: Atualizar a constante `COLUMNS` para incluir a nova coluna `{ id: "PROPOSAL_SENT", title: "Proposta Enviada" }`. A inserção será feita na ordem lógica do funil.
*   **Modal do Prospect (`LeadModal.tsx`)**:
    *   **[MODIFY]**: Adicionar o botão "Criar Proposta" (com um ícone relevante, ex: `FileText` ou `Send`) na seção de ações (perto de "Alertar Risco de Churn").
*   **Modal de Proposta (`ProposalModal.tsx`)**:
    *   **[NEW]**: Criar um componente modal sobreposto com:
        *   Campo editável de **Prospect** (pré-preenchido com `lead.company.companyName` ou `lead.name`).
        *   Campo numérico/moeda de **Valor da Proposta**.
        *   Botão **Enviar** que dispara a requisição para o backend e, ao obter sucesso, fecha os modais para atualizar o status no Kanban.

## Verification Plan

### Testes Automatizados / Mock
- Verificar se o endpoint `POST /api/leads/{id}/proposal` gera um arquivo PDF em disco (ou na resposta HTTP) contendo as informações substituídas com sucesso.
- O mock do serviço de e-mail registrará no terminal (log) os detalhes da mensagem enviada.

### Verificação Manual (UI)
- Abrir um Lead no Kanban.
- Clicar em "Criar Proposta".
- Preencher os campos no Modal e clicar em "Enviar".
- Verificar se o Modal fecha automaticamente e se o Lead é visualmente movido (atualizado) para a nova coluna "Proposta Enviada" no Kanban.
