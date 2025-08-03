# Worker with Java an Go for Order Processing with Data Enrichment and Resilience

## 🛠️ Project Overview

This project implements a **Java-based Worker** designed to process customer orders in an efficient and reliable way.  
The main responsibilities of the Worker are:

- 🔄 **Consume messages** from a **Kafka topic** containing basic order data.
- 📡 **Enrich the information** by calling **external APIs written in Go**.
- 🗄️ **Store the processed and enriched orders** in a **MongoDB** database.

This architecture promotes **resilience**, **scalability**, and a clear **separation of responsibilities** between data processing and data enrichment layers.

## 📦 Tech Stack

This project uses the following technologies:

- ☕ **Java 21** – Core language for implementing the worker logic.
- 🐘 **Kafka** – Message broker used to receive order events.
- 🌐 **Go 1.22 (Golang)** – External APIs for data enrichment.
- 🍃 **MongoDB** – NoSQL database to persist enriched order data.
- 🧠 **Redis** – Used for caching or temporary data storage.
- 🐳 **Docker** – Containerization of all components for consistent environments.
- 📦 **Docker Compose** – Service orchestration for local development and testing.

### 🔧 Developer Tools

- 📊 **Mongo Express** – Web-based UI to inspect and manage MongoDB data.
- 📉 **Kafka UI** – Visual interface to monitor Kafka topics and messages.
- 📂 **Redis Commander** – Simple web UI to view and interact with Redis keys.

## ✅ Prerequisites

Before running the project, make sure you have the following installed:

- 🐳 [Docker](https://www.docker.com/) – Required to run the services in containers.
- 📦 [Docker Compose](https://docs.docker.com/compose/) – To orchestrate multi-container applications.
- 🧠 (Optional) Familiarity with Kafka, MongoDB, and Redis is recommended for debugging and testing purposes.

> 💡 Tip: If you're on Windows or macOS, consider installing [Docker Desktop](https://www.docker.com/products/docker-desktop/) which includes Docker and Docker Compose.

## 🧱 Project Architecture

Both the **Java** and **Go** services follow the principles of **Clean Architecture**, ensuring separation of concerns, testability, and scalability.

### ☕ Java Worker Architecture

The Java service is structured into clearly defined layers:

```bash
├── Dockerfile                        # Container definition for the worker
├── src/main/java/com/cristhianfdx/orderworker/
├── config/
│   ├── AppConfig.java                  # General application configuration
│   ├── ExternalAPIProperties.java      # Configuration for external APIs
│   └── RetryProperties.java            # Retry mechanism properties
│
├── dto/
│   ├── CustomerDTO.java               # DTO for customer data
│   ├── CustomerStatusEnum.java        # Enum for customer status
│   ├── OrderMessageDTO.java           # DTO representing the order message
│   └── ProductDTO.java                # DTO for product data
│
├── exceptions/
│   ├── CustomerNotFoundException.java    # Thrown when customer is not found
│   ├── ExternalApiException.java         # Error when calling external APIs
│   ├── GeneralException.java             # Generic system-level exception
│   ├── InactiveCustomerException.java    # Thrown when customer is inactive
│   ├── OrderAlreadyExists.java           # Duplicate order exception
│   └── ProductNotFoundException.java     # Product not found exception
│
├── kafka/
│   └── OrderConsumer.java             # Kafka consumer for order messages
│
├── model/
│   ├── Customer.java                  # Domain entity: Customer
│   ├── Order.java                     # Domain entity: Order
│   └── Product.java                   # Domain entity: Product
│
├── provider/
│   └── EnrichmentClient.java         # External API client for data enrichment
│
├── repository/
│   └── OrderRepository.java          # Data access layer (MongoDB)
│
├── service/
│   ├── FailedMessageService.java     # Handles failed Kafka messages and save in Redis
│   ├── LockService.java              # Handles locking/concurrency
│   └── OrderProcessorService.java    # Core order processing business logic
│
└── OrderWorkerApplication.java       # Main Spring Boot application entry point

```

🌐 **Go API Architecture**

```bash
go-api/
├── cmd/
│   └── main.go                      # Entry point — starts the API server
│
├── data/
│   ├── customers.json               # Mock customer data source
│   └── products.json                # Mock product data source
│
├── docs/                            # API documentation or specification files (if any)
│
├── internal/                        # Core logic (internal only to the module)
│   ├── adapter/
│   │   └── jsondatabase/
│   │       ├── json_customer_repository.go   # Adapts customer data from JSON to the domain
│   │       └── json_product_repository.go    # Adapts product data from JSON to the domain
│   │
│   ├── api/
│   │   ├── handler/
│   │   │   ├── customer_handler.go           # HTTP handler for customer routes
│   │   │   └── product_handler.go            # HTTP handler for product routes
│   │   └── router.go                         # HTTP router setup
│   │
│   ├── application/
│   │   └── (business logic layer: use cases live here)
│   │
│   ├── domain/
│   │   └── (entity definitions, interfaces for repositories)
│   │
│   └── shared/utils/
│       └── json_reader.go                    # Utility to read JSON files into Go structs
│
├── .gitignore
├── Dockerfile                       # Container definition for the API
├── go.mod
├── go.sum

```

## 🚀 Getting Started

Follow these steps to run the project locally:

1. **Clone the repository:**

```bash
git clone git@github.com:cristhianfdx/order-processing-system.git
```

2. **Navigate into the project directory:**

```bash
cd order-processing-system
```

3. **Start the services using Docker Compose:**

```bash
docker-compose up --build
```

## 🧪 Observability & UIs

This project provides web UIs for easier visualization and debugging:

- **Kafka UI:** [http://localhost:8081](http://localhost:8081)
- **Mongo Express:** [http://localhost:8082](http://localhost:8082)
- **Redis Commander:** [http://localhost:8083](http://localhost:8083)
- **Go API Swagger:** [http://localhost:3000/swagger/index.html](http://localhost:3000/swagger/index.html)

You can use these tools to inspect databases, queues, and messages while the system is running.

## 📄 Evidence of Functionality

This repository includes a PDF document with screenshots and logs that demonstrate the correct behavior of both the `go-api` and `order-worker` components.

You can find the file here:

📁 [`/acceptance-test.pdf`](./acceptance-test.pdf)
