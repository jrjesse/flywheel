# Plano de Implementação: Configurações de SMTP e Template PDF

Este documento descreve o plano para criar uma interface de configuração dinâmica para o envio de e-mails (SMTP) e upload do template PDF das propostas comerciais.

## User Review Required
> [!IMPORTANT]
> Verifique se a abordagem de armazenamento do PDF em uma pasta local do servidor (`uploads/templates`) atende aos seus requisitos de infraestrutura.
> Também note que a senha do SMTP será armazenada no banco de dados. Em um sistema de produção ideal, ela deveria ser encriptada, mas para esta implementação inicial armazenaremos em texto plano/base64 simples para agilidade. Confirme se está de acordo.

## Proposed Changes

### Backend

#### [NEW] Entidade e Repositório de Configurações
- Criar `SystemSettings.java` (Entidade JPA) com as propriedades: `id`, `smtpHost`, `smtpPort`, `smtpUsername`, `smtpPassword`, `templateFilePath`.
- Criar `SystemSettingsRepository.java`.

#### [NEW] Upload e Configuração
- Criar `SystemSettingsController.java`:
  - `GET /api/settings`
  - `PUT /api/settings`
  - `POST /api/settings/template` (Recebe um `MultipartFile` e salva na pasta local do servidor, ex: `./uploads/templates/template.pdf`, atualizando o caminho na entidade).

#### [MODIFY] `EmailService.java`
- Remover a injeção estática do `JavaMailSender` do Spring Boot.
- Buscar as configurações do `SystemSettingsRepository` no momento do envio.
- Instanciar um `JavaMailSenderImpl` localmente com os dados dinâmicos do banco (Host, Porta, Usuário, Senha) para despachar a mensagem.

#### [MODIFY] `ProposalPdfService.java`
- Buscar o caminho do PDF armazenado no `SystemSettingsRepository`.
- Ler o arquivo PDF do disco (`FileInputStream`) em vez de ler da pasta `resources/templates` imutável.
- Se nenhum template estiver configurado, utilizar uma lógica de fallback (geração manual ou aviso de erro).

---

### Frontend

#### [NEW] Página de Configurações de Proposta
- Criar `src/app/settings/proposals/page.tsx`.
- Formulário para as credenciais SMTP (Host, Porta, Usuário, Senha).
- Seção de **Upload de Template PDF**.
- Inclusão do texto de instrução detalhado solicitado: *"Abrir esse PDF em um editor (como Adobe Acrobat, Docfly, etc.), criar os campos de texto preenchíveis com os nomes ClientName, CurrentDate e ProposalValue, e substituir o arquivo na pasta. O sistema vai reconhecer automaticamente e preencher os campos certinhos."*

#### [MODIFY] Menu de Navegação / Layout
- Garantir que exista um link fácil no menu lateral ou no header de configurações para acessar a rota `/settings/proposals`.

## Verification Plan
### Automated Tests
- Testar a compilação do Backend.
- Testar o build do Frontend.

### Manual Verification
1. Acessar a nova tela de Configurações > Propostas.
2. Preencher os dados de um servidor SMTP (ex: MailHog local ou SendGrid) e salvar.
3. Fazer o upload de um arquivo PDF.
4. Ir até um Lead no Kanban e clicar em "Criar Proposta".
5. Verificar se o e-mail foi disparado usando as novas credenciais e se o anexo corresponde ao PDF enviado.
