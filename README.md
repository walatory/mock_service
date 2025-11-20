# 🚀 Mock Platform

> **The "Swiss Army Knife" for Microservice Mocking.**  
> Spin up dynamic, port-isolated mock services in seconds. No restarts. No boilerplate.

---

**Mock Platform** isn't just another request mocker. It's a full-blown **Virtual Service Orchestrator**.  
Designed for microservice developers who need to simulate complex environments without the overhead of deploying real services.

## ✨ Why You'll Love It

### ⚡ **Real Multi-Port Architecture**
Unlike other mockers that route everything through `localhost:8080/service-a`, Mock Platform spins up **actual HTTP servers** on distinct ports (e.g., `8081`, `8082`). 
- Your gateway config doesn't need to change.
- Your firewall rules don't need to change.
- It behaves exactly like the real deal.

### 🧠 **Brainy Responses with SpEL**
Static JSON is boring. Use **Spring Expression Language (SpEL)** to make your mocks come alive.
- **Echo Request Data**: `{"message": "You said: #{#body}"}`
- **Conditional Logic**: `{"status": "#{#req.getParameter('type') == 'vip' ? 'GOLD' : 'SILVER'}"}`
- **Dynamic IDs**: `{"id": "#{T(java.util.UUID).randomUUID().toString()}"}`

### ☁️ **Native Eureka Integration**
Building a Spring Cloud architecture? 
- Mock Platform **automatically registers** your virtual services with Eureka.
- Your other microservices will discover and call them transparently.
- Seamlessly replace a buggy service with a stable mock in production debugging.

### 🔥 **Hot-Swap Everything**
- Add a new service? **Instant.**
- Change a response rule? **Instant.**
- Stop a service? **Instant.**
- **Zero restarts required.**

### 🛠️ **Zero-Config UI**
A built-in, lightweight dashboard to manage your entire virtual landscape. No complex YAML files to edit (unless you want to).

---

## 🚀 Getting Started

### 1. Run It
```bash
java -jar mock-platform.jar
```

### 2. Create It
Open `http://localhost:8080` and create a service:
- **Name**: `PAYMENT-SERVICE`
- **Port**: `9000`

### 3. Mock It
Add a rule:
- **Method**: `POST`
- **URL**: `/pay`
- **Response**:
  ```json
  {
    "transactionId": "#{T(java.util.UUID).randomUUID()}",
    "status": "SUCCESS",
    "amount": "#{#req.getParameter('amount')}"
  }
  ```

### 4. Test It
```bash
curl -X POST "http://localhost:9000/pay?amount=100"
```
**Response:**
```json
{
  "transactionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "SUCCESS",
  "amount": "100"
}
```

---

## 📦 Installation

### Prerequisites
- Java 17+
- Maven 3.6+

### Build from Source
```bash
git clone https://github.com/yourusername/mock-platform.git
cd mock-platform
mvn clean package
```

## 🤝 Contributing
We love PRs! If you have an idea for a cool feature (like delay simulation, chaos monkey mode, or gRPC support), send it our way.

## 📄 License
MIT © 2025 Mock Platform Contributors
