# Hub de Integrações - Architecture Plan

Perfeito! Baseado nas suas escolhas:
1. **Frontend:** Teremos um Hub Centralizado em `/settings/integrations`.
2. **Backend/Auth:** Vamos começar com inserção manual de credenciais (MVP Rápido).
3. **Eventos:** Como não especificou o item 3, vou assumir **Webhooks**, pois o sistema já possui a base de Kafka e isso é ideal para WhatsApp/Instagram.

Eu criei o arquivo de planejamento técnico completo com todas as tarefas e estrutura de pastas em `integrations-module.md` na raiz do seu projeto.

## User Review Required (Edge Cases)

Como o protocolo de segurança (Socratic Gate) exige, para prosseguirmos para o código (Phase 3 e 4), preciso validar estes dois **Edge Cases** finais com você:

### 1. Validação de Credenciais (Edge Case)
Se o usuário colar o Token errado do WhatsApp ou Instagram, a aplicação só descobrirá quando tentar enviar a primeira mensagem. Você quer que eu adicione um botão de **"Testar Conexão"** nos modais de integração (que fará um ping na API para validar a chave antes de salvar)?
- [ ] Sim, quero o botão "Testar Conexão".
- [ ] Não, vamos apenas salvar os tokens no banco e assumir que o usuário colou certo nesta primeira versão.

### 2. Migração da Rota Atual (Edge Case)
Atualmente já existe um botão na Sidebar ("Config. WhatsApp") que leva para a rota `/settings/whatsapp`. Você quer que eu **remova** esse menu antigo e mova a configuração existente para dentro do novo Hub (`/settings/integrations`), ou devemos manter a tela antiga até o novo hub estar totalmente construído e validado?
- [ ] Remover a tela antiga e migrar tudo diretamente para o novo Hub.
- [ ] Manter a tela antiga (Legacy) temporariamente enquanto construímos o Hub.

👉 **Por favor, responda a estas 2 questões.** Com suas respostas, a Fase de Planejamento estará concluída e eu começarei a implementar a **Task 1: Database Entities** imediatamente!
