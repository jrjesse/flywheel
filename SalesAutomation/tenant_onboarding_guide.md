# Manual de Onboarding (Setup por Empresa)

Bem-vindo! Este documento é o manual oficial de configuração para conectar uma nova empresa (Tenant) à nossa plataforma de Automação de Vendas e Fila do WhatsApp. Siga este passo a passo cronológico para ativar o número da empresa na API Oficial da Meta.

---

## Passo 1: Preparação no Meta Business Manager

Antes de abrir o nosso sistema, a empresa precisa ter uma conta no **Gerenciador de Negócios da Meta**.

1. Acesse [business.facebook.com](https://business.facebook.com/).
2. Vá em **Configurações do Negócio** > **Usuários** > **Usuários do Sistema**.
3. Crie um novo usuário do sistema com função de **Administrador**.
4. Salve este usuário, nós o usaremos no Passo 3 para gerar o Token.

---

## Passo 2: Criação do App no Meta for Developers

1. Acesse o portal [Meta for Developers](https://developers.facebook.com/).
2. Clique em **Meus Aplicativos** > **Criar Aplicativo**.
3. Escolha o tipo **Negócios (Business)** ou **Outro**.
4. No painel do aplicativo recém-criado, desça a tela e adicione o produto **WhatsApp**.
5. O sistema pedirá para vincular a uma conta do Gerenciador de Negócios (WABA). Selecione a conta da empresa criada no Passo 1.
6. Adicione o número de telefone real da empresa e faça a verificação via SMS.

---

## Passo 3: Coletando as Credenciais (As 4 Chaves)

Você precisará anotar 4 informações do painel da Meta para inserir no nosso sistema.

| Informação | Onde Encontrar | Para que serve |
| :--- | :--- | :--- |
| **Phone Number ID** | *Painel do WhatsApp > Configuração da API* | Identificador único do número de telefone disparador. |
| **WABA ID** | *Painel do WhatsApp > Configuração da API* | ID da Conta do WhatsApp Business. |
| **App Secret** | *Configurações > Básico* | Chave de segurança para validar a assinatura SHA-256 de webhooks. |
| **Permanent Access Token** | *Business Manager > Usuários do Sistema > Gerar Token* | **Atenção:** Ao gerar, marque as permissões `whatsapp_business_messaging` e `whatsapp_business_management`. **Nunca use o token temporário de 24h** fornecido na página de API. |

---

## Passo 4: Configuração no Nosso Sistema (Dashboard)

Com as chaves em mãos, é hora de plugar a empresa no nosso software.

1. Acesse o Dashboard do seu sistema e clique em **Configurações** no menu lateral.
2. Na seção **Credenciais da API (Meta)**, cole o *Access Token*, *Phone Number ID*, *WABA ID* e *App Secret*.
3. Clique em **Testar Conexão**. 
   - Se ficar <span style="color: green;">**Verde**</span>, o sistema conseguiu se comunicar com a Meta!
4. O campo **Webhook Verify Token** já vem preenchido com um código aleatório seguro. Não apague, apenas copie-o.
5. Na seção Webhook Info (tela preta), clique em **Copiar URL**.
6. Clique em **Salvar Configurações**.

---

## Passo 5: Ativando o Webhook na Meta (Recebendo Mensagens)

Para o sistema receber as mensagens dos leads em tempo real, precisamos dizer para a Meta para onde enviar os dados.

1. Volte ao portal [Meta for Developers](https://developers.facebook.com/).
2. Vá em **WhatsApp** > **Configuração** na barra lateral.
3. Na seção de **Webhooks**, clique em **Editar**.
4. Em *URL de Retorno* (Callback URL), cole a **URL** que você copiou no Passo 4.
5. Em *Token de Verificação* (Verify Token), cole o **Webhook Verify Token** que estava na nossa tela.
6. Clique em **Verificar e Salvar**.
7. Logo abaixo, em *Campos do Webhook*, clique em Gerenciar e **inscreva-se (Subscribe)** no evento `messages`.

> [!IMPORTANT]  
> Se a Meta exibir um erro ao tentar salvar o Webhook, certifique-se de que o nosso Backend (`WhatsAppConfigController`) está rodando publicamente na internet (ex: via servidor de Produção ou Ngrok) e que você apertou o botão "Salvar Configurações" no nosso painel antes de testar na Meta.

---

## Passo 6: Ajuste de Regras de Negócio (Opcional)

A empresa já está conectada. Agora, basta ajustar a operação do Call Center/Vendas:

- **Janela de Silêncio (Debounce):** Define quanto tempo o sistema espera o lead parar de digitar para mandar o bloco de mensagens agrupado para o atendente. (Padrão: 45s).
- **Tempo Limite de SLA:** Se um chat ficar `AGUARDANDO_ATENDIMENTO` mais do que esse tempo, ele se torna um Transbordo (Vermelho) no painel. (Padrão: 5 min).
- **Capacidade de Atendimento:** Máximo de chats simultâneos por corretor/vendedor. (Padrão: 5).

> [!TIP]  
> **Tudo Pronto!** A empresa já pode acessar o módulo de `Prospector B2B` ou a `Fila WhatsApp` e começar a conversar com clientes usando a arquitetura oficial da Meta sem risco de banimentos.
