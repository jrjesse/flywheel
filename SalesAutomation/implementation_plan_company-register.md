# Company Onboarding - Architecture Plan

Ótimas escolhas! Com as suas respostas, defini a seguinte arquitetura técnica:
- **Backend:** Criação da tabela `Tenant` (separada dos `Leads`) para armazenar os dados dos assinantes.
- **Frontend State:** Construção de um hook customizado `useWizardState` que sincroniza com o `localStorage` do navegador para manter os dados a salvo.
- **Validação Rigorosa:** O Step 1 contará com o algoritmo completo de Módulo 11 adaptado para a Tabela ASCII (onde A=10, B=11) conforme o novo padrão da Receita Federal.

Criei o planejamento técnico completo das tarefas em `company-register.md` na raiz do seu projeto.

## User Review Required (Edge Cases)

Como escolhemos a rota de uma validação rigorosa e a criação de uma nova entidade central (`Tenant`), as regras do *Socratic Gate* exigem a validação final destes dois **Edge Cases** antes de eu escrever o código:

### 1. Tipo de Documento (Edge Case)
Como o sistema aceitará tanto CPF (11 números) quanto o Novo CNPJ (14 caracteres alfanuméricos), como a interface deve se comportar?
- **[ ] Opção A (Automático):** O sistema detecta automaticamente enquanto o usuário digita (se tem letras ou mais de 11 dígitos, aplica o algoritmo de CNPJ).
- **[ ] Opção B (Toggle):** Um botão switch na tela ("Sou Pessoa Física" / "Sou Empresa") para mudar a máscara e a validação forçadamente.

### 2. Autenticação do Usuário (Edge Case)
A tela `/company-register` será a primeira coisa que o usuário verá no sistema. Quando ele clicar no botão "Finalizar" no Passo 4 (Endereço), ele deve:
- **[ ] Opção A:** Apenas salvar a empresa no banco e ser redirecionado para a tela de Login/Criação de Usuário.
- **[ ] Opção B:** O Wizard deve pedir Email e Senha (em algum dos passos) para que no Passo 4 ele já saia com o `Tenant` e o `User` criados e logados?

👉 **Por favor, responda a estas 2 questões finais.** Com seu 'Ok', iniciarei a **Task 1: Backend API** imediatamente!
