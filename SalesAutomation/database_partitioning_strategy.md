# Estratégia de Particionamento (PostgreSQL)

Para implementarmos o particionamento na tabela `interaction_status_logs`, precisamos seguir dois passos: desativar a auto-criação do Hibernate para essa tabela e rodar o SQL manualmente.

## 1. Configuração no Spring Boot (JPA)

Atualmente o Hibernate cria a tabela automaticamente por causa do `ddl-auto=update`. Como ele não entende partições nativas, a boa prática é informar ao Hibernate que essa tabela já existe e não deve ser gerada por ele, ou simplesmente gerenciar o banco via **Flyway/Liquibase**.

Se você for manter o `ddl-auto=update`, adicione essa propriedade na entidade `InteractionStatusLog.java` (requer Hibernate mais recente, ou criar a tabela manualmente antes do Spring subir):
O mais recomendado na Fase de Produção é migrar para o **Flyway**.

---

## 2. Script SQL de Criação (DDL)

Este é o script puro em PostgreSQL para criar a tabela "Pai" particionada por mês e as tabelas "Filhas" (Partições).

```sql
-- 1. Criar a Tabela Principal declarando que ela é particionada pela coluna 'transitioned_at'
CREATE TABLE interaction_status_logs (
    id UUID NOT NULL,
    interaction_id UUID NOT NULL,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    transitioned_at TIMESTAMP NOT NULL,
    reason VARCHAR(255),
    
    -- Para particionamento por data, a coluna de data DEVE fazer parte da Primary Key
    PRIMARY KEY (id, transitioned_at),
    CONSTRAINT fk_interaction FOREIGN KEY (interaction_id) REFERENCES whatsapp_interaction_queue(id)
) PARTITION BY RANGE (transitioned_at);

-- 2. Criar as Partições Iniciais (Mão na Massa sem pg_partman)
-- Partição de Abril de 2026
CREATE TABLE interaction_status_logs_2026_04 PARTITION OF interaction_status_logs
    FOR VALUES FROM ('2026-04-01 00:00:00') TO ('2026-05-01 00:00:00');

-- Partição de Maio de 2026
CREATE TABLE interaction_status_logs_2026_05 PARTITION OF interaction_status_logs
    FOR VALUES FROM ('2026-05-01 00:00:00') TO ('2026-06-01 00:00:00');

-- Índices (Criados na tabela pai, são herdados pelas partições)
CREATE INDEX idx_logs_transition_date ON interaction_status_logs(transitioned_at);
CREATE INDEX idx_logs_interaction_id ON interaction_status_logs(interaction_id);
```

> [!WARNING]
> Repare que a `PRIMARY KEY` agora é composta por `(id, transitioned_at)`. O Postgres exige que a chave de particionamento faça parte de qualquer restrição de unicidade na tabela pai.

---

## 3. Automação Avançada com `pg_partman`

Criar partições na mão todo mês não é escalável. A extensão `pg_partman` faz isso sozinha.

### Instalação (se for Docker)
Você precisará de uma imagem do Postgres que já tenha o `pg_partman` instalado (ex: construir uma imagem customizada via `Dockerfile`).

### Configuração do pg_partman
Uma vez instalado no banco, basta rodar esses comandos SQL uma única vez:

```sql
-- Habilita a extensão
CREATE SCHEMA partman;
CREATE EXTENSION pg_partman SCHEMA partman;

-- Manda o partman assumir o controle da nossa tabela e criar partições Mensais
SELECT partman.create_parent(
    p_parent_table := 'public.interaction_status_logs',
    p_control := 'transitioned_at',
    p_type := 'native',
    p_interval:= '1 month',
    p_premake := 2 -- Cria partições com 2 meses de antecedência
);

-- Configura a Retenção (Dropar partições mais velhas que 6 meses automaticamente)
UPDATE partman.part_config 
SET retention = '6 months', retention_keep_table = false 
WHERE parent_table = 'public.interaction_status_logs';
```

### O Job Interno do Postgres
Para que o `pg_partman` faça a limpeza (Drop das tabelas velhas) e criação (Tabelas do futuro), basta rodar essa função em uma *Cron Task* no próprio banco (ex: via `pg_cron`) ou em um `@Scheduled` no Spring Boot que faça a chamada nativa:

```sql
-- Executar isso 1x por dia de madrugada
CALL partman.run_maintenance_proc();
```

---

## Resumo Arquitetural
Com essa estrutura configurada:
1. O Spring Boot faz os `INSERTS` na tabela `interaction_status_logs`.
2. O Postgres magicamente direciona o dado para a tabela `_2026_04`.
3. Todo dia 1º de cada mês, a tabela vazia do mês já vai estar lá pronta (premake).
4. Todo mês, a tabela que completou 6 meses de idade some do banco, liberando gigabytes de espaço. Sua performance no Dashboard continua sempre otimizada!
