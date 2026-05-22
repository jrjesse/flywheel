# Alertas de Churn & Notificações (Sino)

Implementamos com sucesso um sistema global de notificações para que nenhum risco de churn passe despercebido. Abaixo está o resumo do que foi construído:

## Resumo das Modificações

### 1. Backend: Persistência de Alertas
- **Novo Model `SystemNotification`**: Criada a entidade no banco de dados para salvar os alertas gerados, garantindo que mesmo ao fechar o navegador, o alerta continuará lá até ser lido.
- **Novos Endpoints**: A nova rota `/api/notifications` permite gerar, listar e marcar notificações como lidas de forma persistente e escalável.

### 2. Frontend: Modal do Lead (Gatilho)
- **Botão "Alertar Churn"**: Inserido na parte inferior do `LeadModal.tsx` (ao lado do input de histórico). 
- Ao clicar nele, a aplicação faz duas coisas simultaneamente:
  - Salva uma anotação automática ("⚠️ ALERTA DE CHURN: Cliente reportou insatisfação.") no histórico daquele lead.
  - Dispara um alerta global para o sino do Header contendo o nome e a empresa do lead afetado.

### 3. Frontend: O "Sino" (Header)
- **Dropdown Inteligente**: O ícone do sino agora é interativo. Se houver notificações, uma bolinha vermelha (**badge**) exibirá a quantidade.
- Ao clicar no sino, um painel exibe a lista de alertas pendentes.
- Cada alerta traz uma descrição e a hora em que ocorreu. 
- Ao clicar no botão de "✔️ Marcar como Lido" ao lado de cada notificação, ela some do painel e o badge diminui em tempo real!

## Como Validar (Testes Manuais)

> [!IMPORTANT]
> **Reinicie a API:** Como criamos uma nova tabela (`system_notifications`), será necessário reiniciar o Spring Boot (`./mvnw spring-boot:run`) no seu terminal para que o Hibernate crie a tabela automaticamente.

1. **Abra o Dashboard** e clique em qualquer Lead (para abrir o modal).
2. Localize o **botão vermelho (Churn)** na parte inferior do histórico e clique nele.
3. Repare que um novo registro foi adicionado no Histórico do Lead instantaneamente.
4. Feche o modal e olhe para o topo da tela (**Header**): o **sino** agora tem um alerta vermelho.
5. Clique no sino para ver os detalhes da notificação.
6. Clique no botão de confirmação (`v`) para marcar o alerta como resolvido/lido.
