# Memex: Distributed Knowledge Retrieval System

Memex is a microservices-based backend system designed for high-performance ingestion, storage, and retrieval of unstructured data. It utilizes an event-driven architecture to decouple ingestion (RabbitMQ) from indexing (Elasticsearch) and storage (MinIO).

The infrastructure is fully automated using Terraform for provisioning and Docker for container orchestration, capable of running on AWS Free Tier (t3.micro) through optimized resource management.

## System Architecture

- **Core Service**: Java 17 (Spring Boot 3.x)
- **Search Engine**: Elasticsearch 8.11 (optimized for low-memory environments)
- **Message Broker**: RabbitMQ (Asynchronous ingestion)
- **Object Storage**: MinIO (S3-compatible blob storage)
- **Monitoring**: Prometheus + Grafana for metrics collection and visualization
- **Infrastructure**: AWS EC2 (Terraform) + CI/CD (GitHub Actions)

## Quick Start: Local Development

Run the entire distributed system locally with a single command.

### Prerequisites

- Docker Desktop installed and running
- Java 17+ (optional, only if running app outside Docker)
- Node.js 18+ (for frontend development)

### 1. Clone & Configure

```bash
git clone https://github.com/nathanstanislavsky/memex.git
cd memex
```

### 2. Launch the Stack

This uses `docker-compose.yml` to build the Java app from source and spin up all dependencies.

```bash
docker compose up --build
```

### 3. Start Frontend

If you want to run the frontend locally (recommended for development):

```bash
cd frontend
npm install
npm run dev
```

**Frontend API Configuration**: The frontend connects to the backend API. By default, it expects the backend on port 8081 (when running locally outside Docker). To change this:

1. Create a `.env.local` file in the `frontend` directory
2. Add: `VITE_API_BASE_URL=http://localhost:8080` (for Docker setup) or `VITE_API_BASE_URL=http://localhost:8081` (for local backend)

### 4. Verify Connectivity

- **API Health**: http://localhost:8080/actuator/health (Docker) or http://localhost:8081/actuator/health (local)
- **Frontend**: http://localhost:5173 (if running locally)
- **RabbitMQ Dashboard**: http://localhost:15672 (User: `guest` / Pass: `guest`)
- **MinIO Console**: http://localhost:9001 (User: `admin` / Pass: `password`)
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (User: `admin` / Pass: `admin`)

## Cloud Deployment (AWS)

This section details how to provision a production-grade environment on AWS using Infrastructure as Code (IaC).

### Prerequisites

- AWS CLI configured with credentials
- Terraform installed
- An SSH Key pair generated locally (`~/.ssh/memex_key`)

### 1. Provision Infrastructure

Use Terraform to build the Virtual Private Cloud (VPC), Security Groups, and EC2 Server.

```bash
cd terraform
terraform init
terraform apply --auto-approve
```

> **Note**: Copy the `server_ip` output at the end of this process.

### 2. Connect to the Server

SSH into your new instance using the IP from the previous step.

```bash
ssh -i ~/.ssh/memex_key ubuntu@<SERVER_IP>
```

### 3. Initialize Production Environment

Because this runs on a t3.micro (1GB RAM), we must enable Swap Memory to prevent Elasticsearch from crashing the server. Run these commands inside the server:

```bash
# 1. Create 2GB Swap File
sudo fallocate -l 2G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile

# 2. Verify Swap is Active (Look for 'Swap: 2.0Gi')
free -h

# 3. Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.2/docker-compose-linux-x86_64" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 4. Deploy the Stack

Create a production compose file on the server (`nano docker-compose.yml`) and paste the configuration below. This configuration pulls pre-built images from Docker Hub instead of building from source.

<details>
<summary><strong>Click to view production docker-compose.yml</strong></summary>

```yaml
version: '3.8'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.1
    container_name: memex-es
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms400m -Xmx400m"
    ports:
      - "9200:9200"
    volumes:
      - es-data:/usr/share/elasticsearch/data
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:9200/_cluster/health || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 30
      start_period: 60s

  rabbitmq:
    image: rabbitmq:3-management
    container_name: memex-rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      - RABBITMQ_DEFAULT_USER=guest
      - RABBITMQ_DEFAULT_PASS=guest
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "ping"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 30s

  minio:
    image: quay.io/minio/minio
    container_name: memex-minio
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      - MINIO_ROOT_USER=admin
      - MINIO_ROOT_PASSWORD=password
    command: server /data --console-address ":9001"
    volumes:
      - minio-data:/data
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s

  app:
    image: nathanstanislavsky/memex-backend:latest
    container_name: memex-app
    ports:
      - "8080:8080"
    environment:
      - SPRING_RABBITMQ_HOST=rabbitmq
      - SPRING_RABBITMQ_PORT=5672
      - SPRING_RABBITMQ_USERNAME=guest
      - SPRING_RABBITMQ_PASSWORD=guest
      - SPRING_ELASTICSEARCH_URIS=http://elasticsearch:9200
      - S3_ENDPOINT=http://minio:9000
      - S3_ACCESS_KEY=admin
      - S3_SECRET_KEY=password
      - S3_BUCKET_NAME=memex-files
    depends_on:
      elasticsearch:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
      minio:
        condition: service_healthy

volumes:
  es-data:
  minio-data:
```

</details>

Start the fleet:

```bash
sudo docker-compose up -d
```

### 5. Cleanup (Cost Saving)

To avoid AWS charges, destroy the infrastructure when finished:

```bash
# On your local machine (inside /terraform folder)
terraform destroy --auto-approve
```

## CI/CD Pipeline

This repository includes a GitHub Actions workflow (`.github/workflows/deploy.yml`) that triggers on every push to `main`.

The pipeline includes:

- **Checkout**: Pulls the latest code
- **Build**: Compiles the Java application with Maven
- **Containerize**: Builds the Docker image
- **Publish**: Pushes the image to Docker Hub, making it immediately available for the production server to pull

## Monitoring with Prometheus and Grafana

Memex includes built-in monitoring capabilities using Prometheus for metrics collection and Grafana for visualization.

### Services

- **Prometheus**: Scrapes metrics from the Spring Boot application and RabbitMQ exporter
- **Grafana**: Provides dashboards to visualize system performance
- **RabbitMQ Exporter**: Exposes RabbitMQ metrics in Prometheus format

### Setup Grafana Dashboard

#### Step 1: Access Grafana

1. Open your browser and go to: http://localhost:3000
2. Login with:
   - Username: `admin`
   - Password: `admin`
3. You'll be prompted to change the password (you can skip this for now)

#### Step 2: Add Prometheus as Data Source

1. Click on the Configuration icon (gear) in the left sidebar
2. Select **Data Sources**
3. Click **Add data source**
4. Select **Prometheus**
5. Configure:
   - **URL**: `http://prometheus:9090` (use service name since they're on the same network)
   - Click **Save & Test**
   - You should see "Data source is working"

#### Step 3: Import the Dashboard

1. Click on the plus icon (plus) in the left sidebar
2. Select **Import**
3. Click **Upload JSON file**
4. Select the file: `grafana-dashboard.json` from the project root
5. Click **Load**
6. Select the Prometheus data source you just created
7. Click **Import**

#### Step 4: Generate Traffic and View Metrics

Run the test script to generate API requests:

```bash
./test-api.sh
```

Or manually test:

```bash
# Search requests
curl "http://localhost:8080/api/search?q=test"
curl "http://localhost:8080/api/search?q=document"

# Upload a file
curl -X POST -F "files=@/path/to/your/file.txt" http://localhost:8080/api/upload

# Generate load
for i in {1..20}; do
  curl -s "http://localhost:8080/api/search?q=load${i}" > /dev/null
  sleep 0.1
done
```

Once you have traffic, the dashboard will show:

- **HTTP Request Latency**: Maximum request latency over time (`http_server_requests_seconds_max`)
- **RabbitMQ Message Activity**: Total messages published to queues (`rabbitmq_queue_messages_published_total`)

Note: Queue depth (`rabbitmq_queue_messages_ready`) will typically show 0 because messages are consumed immediately. Monitor message activity instead to see queue usage.

### Available Metrics

#### Spring Boot Metrics (from `/actuator/prometheus`)

- `http_server_requests_seconds_max` - Maximum request latency
- `http_server_requests_seconds_sum` - Total request time
- `http_server_requests_seconds_count` - Request count
- `jvm_memory_used_bytes` - JVM memory usage
- `process_cpu_usage` - CPU usage

#### RabbitMQ Metrics (from RabbitMQ Exporter)

- `rabbitmq_queue_messages_ready` - Messages ready in queue
- `rabbitmq_queue_messages_unacked` - Unacknowledged messages
- `rabbitmq_queue_messages_published_total` - Total published messages
- `rabbitmq_queue_messages_delivered_total` - Total delivered messages

### Troubleshooting

**No metrics showing up?**

1. Check Prometheus targets: http://localhost:9090/targets
   - Both `memex-backend` and `rabbitmq` should be UP
2. Verify metrics endpoint: http://localhost:8080/actuator/prometheus
3. Verify RabbitMQ exporter: http://localhost:9419/metrics

**Grafana can't connect to Prometheus?**

- Make sure both services are on the same Docker network (`memex-network`)
- Use `http://prometheus:9090` as the URL (not `localhost`)

