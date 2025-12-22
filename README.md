# Memex: Distributed Knowledge Retrieval System

Memex is a microservices-based backend system designed for high-performance ingestion, storage, and retrieval of unstructured data. It utilizes an event-driven architecture to decouple ingestion (RabbitMQ) from indexing (Elasticsearch) and storage (MinIO).

The infrastructure is fully automated using **Terraform** for provisioning and **Docker** for container orchestration, capable of running on AWS Free Tier (`t3.micro`) through optimized resource management.

## System Architecture

* **Core Service:** Java 17 (Spring Boot 3.x)
* **Search Engine:** Elasticsearch 8.11 (optimized for low-memory environments)
* **Message Broker:** RabbitMQ (Asynchronous ingestion)
* **Object Storage:** MinIO (S3-compatible blob storage)
* **Infrastructure:** AWS EC2 (Terraform) + CI/CD (GitHub Actions)

---

## Quick Start: Local Development

Run the entire distributed system locally with a single command.

### Prerequisites
* Docker Desktop installed and running.
* Java 17+ (optional, only if running app outside Docker).

### 1. Clone & Configure
```bash
git clone [https://github.com/nathanstanislavsky/memex.git](https://github.com/nathanstanislavsky/memex.git)
cd memex