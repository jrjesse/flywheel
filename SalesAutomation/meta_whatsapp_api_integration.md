# Guia de Integração Oficial - Meta WhatsApp Cloud API

Este documento detalha o passo-a-passo e a arquitetura necessária para conectar o botão **"Puxar Chat"** (e toda a fila) com a API oficial do WhatsApp da Meta.

---

## 1. Pré-Requisitos e Setup na Meta

Antes de escrever qualquer código, você precisará configurar o ambiente no portal de desenvolvedores do Facebook/Meta.

1. Acesse o [Meta for Developers](https://developers.facebook.com/).
2. Crie um aplicativo selecionando o tipo **Negócios (Business)**.
3. Adicione o produto **WhatsApp** ao aplicativo.
4. Ao adicionar, o sistema criará uma conta WABA (WhatsApp Business Account) e te fornecerá um **Número de Teste** (ou você pode registrar um número real).
5. Gere o **Token de Acesso Permanente** (Permanent Access Token) associado a um *System User* no seu Meta Business Manager (nunca use o token temporário de 24h para produção).

> [!IMPORTANT]
> **Janela de 24 Horas:**
> A API da Meta possui uma regra estrita: você só pode enviar mensagens livres (texto livre) se o lead enviou uma mensagem para você nas últimas 24 horas. Caso contrário, você é obrigado a enviar um **Message Template** (Modelo de Mensagem previamente aprovado pela Meta).

---

## 2. A Mecânica do Botão "Puxar Chat"

Quando o supervisor ou atendente clica em **"Puxar Chat"** no nosso Frontend, o fluxo técnico deve ser o seguinte:

### No Frontend (React/Next.js)
```typescript
const puxarChat = async (interactionId: string) => {
  // 1. Faz uma chamada POST para a nossa API do Spring Boot
  await fetch(`/api/queue/interaction/${interactionId}/pull`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ agentId: myAgentId })
  });
  
  // 2. Redireciona o atendente para a tela de Mensageria
  router.push(`/chat/${interactionId}`);
}
```

### No Backend (Spring Boot)
1. O backend recebe o ID da interação e atualiza a tabela `whatsapp_interaction_queue` de `AGUARDANDO_ATENDIMENTO` para `EM_ATENDIMENTO`, atribuindo o `agent_id`.
2. Em seguida, o backend faz uma chamada HTTP POST diretamente para a **Cloud API da Meta**, avisando o cliente que ele está sendo atendido (opcional).

---

## 3. Enviando uma Mensagem via Meta API (Java Spring)

Para mandar uma mensagem de volta para o cliente, o seu backend deve fazer uma requisição POST para o Endpoint oficial da Meta.

**Endpoint Base:** 
`https://graph.facebook.com/v19.0/{PHONE_NUMBER_ID}/messages`

**Exemplo de Implementação com `RestTemplate` ou `WebClient`:**
```java
public void enviarMensagemWhatsApp(String telefoneDestino, String mensagemText) {
    String url = "https://graph.facebook.com/v19.0/" + myPhoneNumberId + "/messages";
    
    // Header com o Token de Acesso
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(metaAccessToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    // Payload Oficial da Meta
    String jsonPayload = """
    {
      "messaging_product": "whatsapp",
      "recipient_type": "individual",
      "to": "%s",
      "type": "text",
      "text": {
        "preview_url": false,
        "body": "%s"
      }
    }
    """.formatted(telefoneDestino, mensagemText);

    HttpEntity<String> request = new HttpEntity<>(jsonPayload, headers);
    restTemplate.postForEntity(url, request, String.class);
}
```

---

## 4. Recebendo Mensagens (Configurando o Webhook)

A Meta envia as mensagens do Lead para o seu sistema através de Webhooks. Nós já criamos o controlador `/api/queue/webhook`, mas para a Meta aprová-lo, você precisa implementar um **Endpoint GET** no mesmo endereço para validação.

> [!WARNING]
> A Meta faz uma requisição `GET` com um `hub.challenge` que seu servidor deve retornar intacto para provar que a URL é sua.

**Exemplo no `QueueController`:**
```java
@GetMapping("/webhook")
public ResponseEntity<String> verifyWebhook(
        @RequestParam("hub.mode") String mode,
        @RequestParam("hub.verify_token") String token,
        @RequestParam("hub.challenge") String challenge) {
    
    // O verify_token é uma senha que você define no painel da Meta
    if ("subscribe".equals(mode) && "MEU_TOKEN_SECRETO".equals(token)) {
        return ResponseEntity.ok(challenge); // Retorna apenas o challenge em plain text
    }
    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
}
```

A partir daí, as mensagens enviadas pelos Leads chegarão como `POST /api/queue/webhook` contendo um payload JSON gigante da Meta, de onde você irá extrair:
- Número de telefone de origem (seu `Lead`).
- O corpo do texto (`text.body`).
- Passar essa extração para o nosso método `queueService.processIncomingMessage(...)`.

---

## Próximos Passos Recomendados

1. **Criar a Conta Meta App** e pegar o `Phone_Number_ID` e o `Access_Token`.
2. Adicionar o **GET de Validação do Webhook** no backend.
3. Expor o servidor local para a internet (usando **Ngrok**) e cadastrar a URL no painel da Meta para receber Webhooks na máquina local.
4. Integrar o endpoint oficial de envio de mensagens no serviço que atende o botão **"Puxar Chat"**.
