# Teste de Inserção de Leads (Webhook)

Para ver o seu banco de dados, o *Lead Scoring* e o *Prospector B2B* brilharem na prática, execute os comandos abaixo no seu Terminal. Cada comando enviará um perfil B2B diferente para o nosso barramento de entrada (`POST /api/leads/webhook`).

Certifique-se de que o backend Spring Boot está rodando em `localhost:8080`.

---

### 1. O "Tubarão" (CTO de Enterprise com MRR Altíssimo)
Este lead deve atingir um *Score* muito alto devido ao seu cargo (C-Level) e MRR (>$50.000).

```bash
curl -X POST http://localhost:8080/api/leads/webhook \
-H "Content-Type: application/json" \
-d '{
  "name": "Roberto Nogueira",
  "email": "roberto.nogueira@techcorp.com.br",
  "phone": "+5511999998888",
  "role": "CTO",
  "companyName": "TechCorp S/A",
  "companySize": "ENTERPRISE",
  "mrr": 125000.00,
  "bio": "Vi que a TechCorp adquiriu a startup de pagamentos na semana passada. Parabéns pelo movimento agressivo de mercado!",
  "socialMedias": [
    {
      "type": "LINKEDIN",
      "url": "linkedin.com/in/robertonogueiracto"
    },
    {
      "type": "GITHUB",
      "url": "github.com/rnogueira"
    }
  ]
}'
```

---

### 2. A "Oportunidade Morna" (Gerente de Marketing B2B)
Este lead é de uma empresa de médio porte, mas com um bom Icebreaker da Inteligência Artificial.

```bash
curl -X POST http://localhost:8080/api/leads/webhook \
-H "Content-Type: application/json" \
-d '{
  "name": "Camila Oliveira",
  "email": "camila@growthz.com",
  "phone": "+5541988887777",
  "role": "Gerente de Marketing",
  "companyName": "GrowthZ",
  "companySize": "MEDIUM",
  "mrr": 25000.00,
  "bio": "Adorei o último post sobre estratégias de Inbound. Gostaria de te mostrar uma solução de outbound que pode casar perfeitamente com a campanha de vocês.",
  "socialMedias": [
    {
      "type": "LINKEDIN",
      "url": "linkedin.com/in/camilaoliveiramkt"
    }
  ]
}'
```

---

### 3. O "Pequeno Negócio" (Founder sem MRR declarado)
Este lead cairá com um Score mais baixo, demonstrando que o algoritmo de filtragem funciona. Ele não possui `bio` e a empresa é micro.

```bash
curl -X POST http://localhost:8080/api/leads/webhook \
-H "Content-Type: application/json" \
-d '{
  "name": "João Souza",
  "email": "joao@padeirotech.com.br",
  "phone": "+5521977776666",
  "role": "Founder",
  "companyName": "Padeiro Tech",
  "companySize": "MICRO",
  "socialMedias": [
    {
      "type": "INSTAGRAM",
      "url": "instagram.com/padeirotech"
    }
  ]
}'
```

---

### 4. A "Baleia Escondida" (VP de Vendas)
Lead de empresa grande, com MRR mediano e focado em setor comercial.

```bash
curl -X POST http://localhost:8080/api/leads/webhook \
-H "Content-Type: application/json" \
-d '{
  "name": "Fernando Batista",
  "email": "fbatista@industrialive.com",
  "phone": "+5531955554444",
  "role": "VP de Vendas",
  "companyName": "Industria Live",
  "companySize": "LARGE",
  "mrr": 80000.00,
  "bio": "Notei que vocês abriram vagas para 5 novos executivos de vendas. Como estão lidando com o gargalo na geração de listas B2B para esse time?",
  "socialMedias": [
    {
      "type": "LINKEDIN",
      "url": "linkedin.com/in/fernandobatistavendas"
    }
  ]
}'
```

---

## Como Validar:
Após executar esses 4 blocos de código no seu terminal:
1. Abra a aplicação no navegador (`http://localhost:3000`).
2. Acesse a aba **Prospector B2B**.
3. A tabela deverá estar populada com os 4 perfis.
4. Digite **"15000"** no novo filtro de **MRR Mínimo Estimado** e comprove: O "João" vai desaparecer.
5. Clique no card do **Roberto Nogueira**. O *Score* dele estará altíssimo, a tag "MRR $125.000" estará brilhando e a caixa Azul de *Icebreaker (IA)* aparecerá pronta para o seu vendedor copiar o texto!
