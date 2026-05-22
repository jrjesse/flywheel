# Guia de Deploy e Implantação da Plataforma

Este documento descreve as estratégias recomendadas para colocar a plataforma de Automação de Vendas (Next.js + Spring Boot + Kafka + PostgreSQL) em ambiente de Produção.

Existem duas abordagens principais: **On-Premise (Servidor Único/VPS)** ideal para PMEs e MVPs, e a **Arquitetura Cloud Nativa (AWS/GCP)** ideal para alta escalabilidade e resiliência.

---

## 1. Arquitetura Base da Aplicação
Nossa stack é dividida nos seguintes componentes vitais:
1. **Frontend:** Next.js (React) - Porta padrão: `3000`
2. **Backend:** Spring Boot (Java) - Porta padrão: `8080`
3. **Banco de Dados:** PostgreSQL - Porta padrão: `5432`
4. **Message Broker:** Apache Kafka & Zookeeper - Porta padrão: `9092`

> [!IMPORTANT]
> **Requisito de Memória:** O Kafka e o Spring Boot (Java) consomem bastante memória. É obrigatório um servidor com no mínimo **8GB de RAM** (recomendado 16GB) para rodar tudo no mesmo ecossistema sem travamentos.

---

## Opção A: Deploy On-Premise (Servidor Físico ou VPS Única)

Esta é a opção mais barata e rápida. Consiste em alugar uma VPS (ex: Hetzner, DigitalOcean, Linode) ou usar um servidor físico na sede da empresa e encapsular tudo usando o `docker-compose`.

### Passo a Passo

**1. Preparação do Servidor (Linux Ubuntu 22.04/24.04)**
Acesse a máquina via SSH e instale o Docker:
```bash
sudo apt update
sudo apt install docker.io docker-compose git nginx certbot python3-certbot-nginx -y
sudo systemctl enable --now docker
```

**2. Clonar o Repositório**
```bash
git clone https://seu-repositorio.com/antigravitykit.git
cd antigravitykit
```

**3. Criar o Arquivo Docker Compose de Produção**
Na raiz do projeto, você deve ter um `docker-compose.prod.yml`. O arquivo deve inicializar o PostgreSQL, o Zookeeper, o Kafka, o Backend (construído com Jib ou Dockerfile) e o Frontend.

**4. Subir a Aplicação**
```bash
docker-compose -f docker-compose.prod.yml up -d --build
```

**5. Configurar o Proxy Reverso (Nginx) e SSL**
Edite o arquivo `/etc/nginx/sites-available/default`:
```nginx
server {
    server_name app.suaempresa.com;

    location / {
        proxy_pass http://localhost:3000; # Frontend
        proxy_set_header Host $host;
    }

    location /api/ {
        proxy_pass http://localhost:8080/api/; # Backend API
        proxy_set_header Host $host;
    }
}
```
Ative o certificado HTTPS gratuito:
```bash
sudo certbot --nginx -d app.suaempresa.com
```

---

## Opção B: Deploy Escalável em Nuvem (AWS ou GCP)

Para operações críticas, com dezenas de instâncias de "Prospector B2B" e tráfego pesado de Webhooks da Meta, a recomendação é **desacoplar** a infraestrutura usando serviços gerenciados.

### 1. Banco de Dados Gerenciado
* **AWS:** Amazon RDS for PostgreSQL.
* **GCP:** Cloud SQL for PostgreSQL.
* *Por que?* Backups automáticos diários, réplicas de leitura e alta disponibilidade (Multi-AZ). O Backend não corre o risco de cair por gargalo de I/O de disco.

### 2. Mensageria (Kafka) Gerenciada
* **AWS:** Amazon MSK (Managed Streaming for Apache Kafka).
* **Cloud Agnóstico:** Confluent Cloud (A melhor opção do mercado, conecta fácil na AWS/GCP).
* *Por que?* O Kafka on-premise é notoriamente complexo de manter. O serviço gerenciado evita que os tópicos corrompam.

### 3. Hospedagem do Backend (Spring Boot)
* **Recomendação Padrão:** Containerização com **AWS ECS (Fargate)** ou **Google Cloud Run**.
* O Spring Boot é "conteinerizado" e você paga apenas pelo tempo/uso. Ele escala horizontalmente subindo novas réplicas conforme a fila de mensagens do Kafka aumenta (usando métricas customizadas).

### 4. Hospedagem do Frontend (Next.js)
* **Recomendação Padrão:** **Vercel** ou **AWS Amplify**.
* Como usamos Next.js, fazer o deploy na Vercel garante que todo o lado "Server-Side Rendering" seja executado perfeitamente na borda (Edge) com CDN global automática. Basta apontar o repositório Git para a Vercel e configurar a URL da API do AWS ECS.

---

## Checklist de Segurança (Para Ambas as Opções)

> [!WARNING]  
> **Exposição de Portas:**
> O banco de dados (5432) e o Kafka (9092) **NUNCA** devem ser expostos para a internet pública (0.0.0.0). Eles devem escutar apenas a `localhost` ou a Rede Privada Virtual (VPC) da AWS/GCP.

- **Senhas Fortes:** Nunca use `postgres / root` em produção. Utilize o Vault (AWS Secrets Manager ou GCP Secret Manager) para injetar as chaves da aplicação.
- **Firewall (UFW / Security Groups):** Mantenha abertas estritamente as portas `80` (HTTP) e `443` (HTTPS) para o tráfego externo.
- **Proteção contra DDoS (Webhooks):** A rota `/api/leads/webhook` deve possuir proteção básica contra inundação (Rate Limiting) via Nginx ou Cloudflare, já que ferramentas como o *Clay* podem enviar picos (bursts) de milhares de requisições de uma vez.
