# PingMe - Distributed Backend Architecture

- ### The front-end repo for this project can be found at [PingMe-NextJs](https://github.com/HakshayReddyKana/PingMe-Next.Js)

This repository contains the backend infrastructure and application code for **PingMe**, a highly scalable, real-time chat application. The backend is designed as an **Enterprise-Grade Distributed System**, utilizing the exact same architectural blueprints used by industry leaders like Netflix, Discord, and Uber to achieve zero-downtime deployments, horizontal scalability, and stateful WebSocket synchronization.

---

## 🏗️ High-Level System Architecture

### 1. Horizontally Scalable Distributed Fleet
The backend is completely stateless (excluding the database/cache layer), allowing it to scale infinitely. 
- **Application Load Balancer (ALB):** Intelligently routes and round-robins incoming HTTPS and WSS (WebSocket Secure) internet traffic across a dynamic fleet of servers.
- **Auto Scaling Group (ASG):** Automatically monitors CPU utilization via CloudWatch alarms. If the system experiences high load, the ASG dynamically provisions new EC2 instances and registers them with the ALB.

### 2. Distributed WebSocket Pub/Sub (The "Stateful Scaling" Problem)
WebSockets are inherently stateful. If User A connects to Server 1, and User B connects to Server 2, they cannot naturally chat with each other.
- **AWS ElastiCache (Redis):** Integrated as a centralized high-speed Pub/Sub message broker.
- When User A sends a message to Server 1, Server 1 publishes the payload to Redis. Redis instantly broadcasts the message across the AWS datacenter to Server 2, which pushes it down the WebSocket to User B in real-time.

### 3. Zero-Downtime CI/CD Pipeline
Deployments are entirely automated and orchestrate 100% zero-downtime updates.
- **GitHub Actions:** Pushes to the `main` branch automatically trigger a CI/CD pipeline that compiles the Spring Boot application, builds a highly optimized Docker container, and securely pushes it to **AWS ECR (Elastic Container Registry)**.
- **Rolling Instance Refresh:** The pipeline triggers an ASG Instance Refresh. AWS gracefully spins up new servers with the updated code, waits for them to pass Health Checks, routes traffic to them, and then safely drains and terminates the old servers.

### 4. Automated Infrastructure Provisioning
No manual server configuration is required.
- **EC2 Launch Templates & `cloud-init`:** Every time the Auto Scaling Group provisions a new server, a complex user-data bash script executes. It automatically installs Docker, authenticates with AWS IAM Service Roles, pulls the latest image from ECR, and boots the container entirely hands-free.

### 5. Enterprise-Grade Security & Secrets Management
- **AWS Secrets Manager:** Hardcoded credentials and `.env` files are eliminated from the server. The infrastructure dynamically fetches the production database credentials and JWT signing keys via the AWS API only at server boot time.
- **End-to-End SSL/TLS Encryption:** Free public SSL certificates provisioned via **AWS Certificate Manager (ACM)** are attached to the Load Balancer, automatically decrypting incoming HTTPS/WSS traffic and enforcing secure connections.
- **VPC & Security Groups:** The internal AWS RDS PostgreSQL database is strictly firewalled, permitting access exclusively from the internal Auto Scaling Group fleet.

---

## 🛠️ Tech Stack
* **Core Application:** Java 21, Spring Boot 3, Spring WebSockets (STOMP Protocol), Spring Security, Spring Data JPA.
* **Database & Caching:** PostgreSQL (AWS RDS), Redis (AWS ElastiCache).
* **Cloud Infrastructure (AWS):** EC2, Application Load Balancer (ALB), Auto Scaling Groups (ASG), Elastic Container Registry (ECR), Secrets Manager, Certificate Manager (ACM), CloudWatch, IAM.
* **DevOps:** Docker, GitHub Actions, Shell Scripting (`cloud-init`).

---

## 🔒 Security Implementations
- **Stateless Authentication:** Secure, stateless JWT (JSON Web Token) authentication for all REST API endpoints and STOMP WebSocket handshake connections.
- **CORS Protection:** Strict Cross-Origin Resource Sharing (CORS) configurations that explicitly whitelist only the official Next.js frontend domain.
- **Rate Limiting:** Request throttling implemented at the application edge to prevent DDoS and brute-force attacks.

---

*This architecture was engineered to demonstrate a deep understanding of Cloud-Native systems, Infrastructure as Code, CI/CD automation, and solving complex distributed networking challenges.*
