# Sistema de Distribuição de Links de Alta Escala

![Java](https://img.shields.io/badge/Java-25-orange.svg) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg) ![Redis](https://img.shields.io/badge/Redis-Caching-red.svg) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Durable-blue.svg) ![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)

[🇺🇸 Read in English](./README.md)

Um encurtador de URL distribuído e de alto desempenho, projetado para suportar tráfego de leitura massivo e operações de escrita concorrentes. Desenvolvido com foco em **escalabilidade**, **baixa latência** e **consistência de dados**, utilizando padrões de cache da indústria.

## Arquitetura do Sistema






Este sistema vai além do CRUD básico, implementando padrões avançados de backend para resolver gargalos específicos de escalabilidade:

### 1. Geração Distribuída de IDs (Sem Colisão)
* **Problema:** UUIDs aleatórios causam colisões e exigem verificações lentas no banco de dados.
* **Solução:** Implementação de um **Contador Distribuído** usando Redis (`INCR`) combinado com **Codificação Base62**.
* **Resultado:** Unicidade garantida, IDs sequenciais (`q0V`, `q0W`) e tempo de geração **O(1)** sem bloqueios de banco de dados (locks).

### 2. Estratégia de Cache (Padrão Cache-Aside)
* **Problema:** Consultar o PostgreSQL para cada redirecionamento (Leitura) cria alta latência e gargalos de I/O de disco.
* **Solução:** Todos os redirecionamentos são armazenados em cache no **Redis** com um TTL (Tempo de Vida).
* **Resultado:** Latência de leitura inferior a 1ms para links acessados frequentemente ("hot links"). O tráfego no banco de dados é reduzido em ~90%.

### 3. Analytics Assíncrono (Padrão Write-Behind)
* **Problema:** Incrementar contadores de cliques no banco de dados de forma síncrona (`UPDATE links...`) bloqueia linhas e desacelera o redirecionamento.
* **Solução:** Os cliques são contados atomicamente no Redis em tempo real. Um agendador (scheduler) em segundo plano envia esses contadores para o PostgreSQL em lotes a cada 10 segundos.
* **Resultado:** A API de redirecionamento permanece extremamente rápida, desacoplando a latência do usuário da performance de escrita do banco.

### 4. Segurança (Rate Limiting)
* **Problema:** Abuso da API (bots de spam) pode exaurir recursos.
* **Solução:** Implementação de um **Rate Limiter de Janela Fixa** usando chaves com expiração no Redis. Bloqueia IPs que excedem 10 requisições/minuto.

---
##  Tech Stack (Tecnologias)

* **Linguagem:** Java 25 (OpenJDK)
* **Framework:** Spring Boot 3.x
* **Banco de Dados:** PostgreSQL 15 (Alpine)
* **Cache/Broker:** Redis (Alpine)
* **Containerização:** Docker & Docker Compose

---

##  Como Iniciar

Você não precisa ter Java ou Maven instalados localmente. Todo o sistema é containerizado.

### Pré-requisitos
* Docker Desktop (ou Docker Engine + Compose)

### Instalação
1.  Clone o repositório:
    ```bash
    git clone [https://github.com/nathan-padilha-costa/high-scale-link-shortener.git](https://github.com/nathan-padilha-costa/high-scale-link-shortener.git)
    cd high-scale-link-shortener
    ```

2.  Inicie a infraestrutura:
    ```bash
    docker compose up --build
    ```
    *Aguarde até ver o log: `Started DemoApplication in ... seconds`*

---

##  Documentação da API

### 1. Encurtar um Link
**POST** `/api/v1/shorten`

Cria um novo link encurtado. Retorna o código curto gerado.

```bash
curl -X POST http://localhost:8080/api/v1/shorten \
     -H "Content-Type: application/json" \
     -d '{"longUrl": "[https://www.google.com](https://www.google.com)"}'
```

**Resposta:**
```json
{
  "shortCode": "q0V",
  "longUrl": "[https://www.google.com](https://www.google.com)",
  "clickCount": 0
}
```
### 2. Redirecionar (Abrir no Navegador)
**GET** `http://localhost:8080/{shortCode}`

Redireciona o usuário para a URL original (HTTP 302 Found).

### 3. Ver Analytics em Tempo Real
**GET** `/api/v1/shorten/{shortCode}/stats`

Busca a contagem híbrida de cliques (Buffer em tempo real no Redis + contagem persistida no banco).

```bash
curl http://localhost:8080/api/v1/shorten/q0V/stats
```

---
##  Testando Performance

### Teste de Rate Limiter
Para verificar a segurança, execute este loop no seu terminal para simular um ataque de spam. Ele tenta criar 12 links rapidamente.

```bash
for i in {1..12}; do
    curl -X POST http://localhost:8080/api/v1/shorten \
         -H "Content-Type: application/json" \
         -d '{"longUrl": "[https://google.com](https://google.com)"}'
    echo ""
done
```
*Resultado:* A requisição nº 11 será bloqueada com `429 Too Many Requests`.

---

##  Melhorias Futuras
* **Escalonamento Horizontal:** Deploy atrás de um Load Balancer (Nginx) com múltiplas réplicas do Spring Boot.
* **Métricas:** Integrar Prometheus/Grafana para visualizar taxas de acerto/erro do cache Redis.
* **Contas de Usuário:** Adicionar autenticação JWT para gerenciamento de links por usuário.
