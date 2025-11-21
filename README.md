#  High-Scale Link Distribution System

![Java](https://img.shields.io/badge/Java-25-orange.svg) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg) ![Redis](https://img.shields.io/badge/Redis-Caching-red.svg) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Durable-blue.svg) ![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)

[🇧🇷 Leia em Português](./README-pt.md)

A high-performance, distributed URL shortener built to handle massive read traffic and concurrent write operations. Designed with a focus on **scalability**, **low latency**, and **data consistency** using industry-standard caching patterns.
##  System Architecture



This system moves beyond basic CRUD by implementing advanced backend patterns to solve specific scaling bottlenecks:

### 1. Distributed ID Generation (Collision-Free)
* **Problem:** Random UUIDs cause collisions and require expensive DB checks.
* **Solution:** Implemented a **Distributed Counter** using Redis (`INCR`) combined with **Base62 Encoding**.
* **Result:** Guaranteed uniqueness, sequential IDs (`q0V`, `q0W`), and **O(1)** generation time without database locks.

### 2. Caching Strategy (Cache-Aside Pattern)
* **Problem:** Hitting PostgreSQL for every redirect (Read) creates high latency and disk I/O bottlenecks.
* **Solution:** All redirects are cached in **Redis** with a TTL (Time-To-Live).
* **Result:** Sub-millisecond read latency for hot links. Database traffic is reduced by ~90%.

### 3. Async Analytics (Write-Behind Pattern)
* **Problem:** Incrementing click counters in the DB synchronously (`UPDATE links...`) locks rows and slows down redirects.
* **Solution:** Clicks are counted atomically in Redis in real-time. A background scheduler flushes these counts to PostgreSQL in batches every 10 seconds.
* **Result:** The "Redirect" API remains lightning-fast, decoupling user latency from database write performance.

### 4. Security (Rate Limiting)
* **Problem:** API abuse (spam bots) can exhaust resources.
* **Solution:** Implemented a **Fixed-Window Rate Limiter** using Redis expiry keys. Blocks IPs exceeding 10 requests/minute.

---

##  Tech Stack

* **Language:** Java 25 (OpenJDK)
* **Framework:** Spring Boot 3.x
* **Database:** PostgreSQL 15 (Alpine)
* **Cache/Broker:** Redis (Alpine)
* **Containerization:** Docker & Docker Compose

---

##  Getting Started

You do not need Java or Maven installed locally. The entire system is containerized.

### Prerequisites
* Docker Desktop (or Docker Engine + Compose)

### Installation
1.  Clone the repository:
    ```bash
    git clone [https://github.com/YOUR_USERNAME/high-scale-link-shortener.git](https://github.com/nathan-padilha-costa/high-scale-link-shortener.git)
    cd high-scale-link-shortener
    ```

2.  Start the infrastructure:
    ```bash
    docker compose up --build
    ```
    *Wait until you see the log: `Started DemoApplication in ... seconds`*

---

##  API Documentation

### 1. Shorten a Link
**POST** `/api/v1/shorten`

Creates a new short link. Returns the generated short code.

```bash
curl -X POST http://localhost:8080/api/v1/shorten \
     -H "Content-Type: application/json" \
     -d '{"longUrl": "[https://www.google.com](https://www.google.com)"}'
---

```

**Response:**
```json
{
  "shortCode": "q0V",
  "longUrl": "[https://www.google.com](https://www.google.com)",
  "clickCount": 0
}
```

### 2. Redirect (Open in Browser)
**GET** `http://localhost:8080/{shortCode}`

Redirects the user to the original URL (HTTP 302 Found).

### 3. View Real-Time Analytics
**GET** `/api/v1/shorten/{shortCode}/stats`

Fetches the hybrid click count (Real-time Redis buffer + Database persisted count).

```bash
curl http://localhost:8080/api/v1/shorten/q0V/stats
```

---

##  Testing Performance

### Rate Limiter Test
To verify the security, run this loop in your terminal to simulate a spam attack. It attempts to create 12 links rapidly.

```bash
for i in {1..12}; do
    curl -X POST http://localhost:8080/api/v1/shorten \
         -H "Content-Type: application/json" \
         -d '{"longUrl": "[https://google.com](https://google.com)"}'
    echo ""
done
```
*Result:* Request #11 will be blocked with `429 Too Many Requests`.

---

##  Future Improvements
* **Horizontal Scaling:** Deploy behind a Load Balancer (Nginx) with multiple Spring Boot replicas.
* **Metrics:** Integrate Prometheus/Grafana to visualize Redis cache hit/miss rates.
* **User Accounts:** Add JWT Authentication for user-specific link management.
