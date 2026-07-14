<div align="center">

# 🛒 Shoppiq

### Full-Stack E-Commerce Platform with AI-Powered Shopping Assistant

A sophisticated, multi-vendor e-commerce marketplace built with Spring Boot 4.1, featuring an intelligent AI chat assistant powered by LangChain4j and NVIDIA NIM, with Retrieval-Augmented Generation (RAG) for semantic product search.

[![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-1.17.2-FF6B35?style=for-the-badge)](https://docs.langchain4j.dev/)
[![License](https://img.shields.io/badge/License-Source--Available-blue?style=for-the-badge)](LICENSE.md)

</div>

---

## Table of Contents

1. [Features](#features)
2. [Architecture](#architecture)
3. [Tech Stack](#tech-stack)
4. [Quick Start](#quick-start)
5. [Project Structure](#project-structure)
6. [Services & Documentation](#services--documentation)
7. [API Overview](#api-overview)
8. [AI Features](#ai-features)
9. [Security](#security)
10. [Configuration](#configuration)
11. [Contributing](#contributing)
12. [License](#license)

---

## Features

### Core E-Commerce
- **Product Catalog** — Browse products with category filtering, search, and pagination
- **Shopping Cart** — Add, update, remove items with real-time stock validation
- **Checkout Flow** — Secure checkout with address snapshot and promo code support
- **Multi-Payment Gateway** — Razorpay (implemented), Stripe, PayPal, UPI, COD
- **Order Management** — Full order lifecycle: placed → processing → shipped → delivered
- **Seller Portal** — Multi-vendor marketplace with seller registration and approval workflow
- **Admin Dashboard** — Analytics, inventory, orders, users, reports, and bulk operations

### AI-Powered Features
- **RAG-Based Product Search** — Semantic product search using vector embeddings (Qdrant + BGE-small-en-v1.5)
- **Agentic Tool Calling** — AI autonomously calls 6 tools (product lookup, cart, orders, reviews, search, resolve)
- **Streaming Responses** — Token-by-token streaming via Server-Sent Events (SSE)
- **Multi-Model Selection** — Switch between Nemotron 49B, Llama 3.1 8B, Llama 4 Maverick
- **Guest & Authenticated Chat** — Full support for both modes with role-based tool access
- **Auto-Resolve Detection** — AI detects closing intent and resolves conversations automatically
- **Conversation Memory** — Sliding-window memory (20 messages) with per-conversation isolation

### Security & Infrastructure
- **JWT Cookie-Only Auth** — Token never exposed to JavaScript (XSS mitigation)
- **OAuth2 Google Login** — Fully stateless with HMAC-signed cookies
- **Rate Limiting** — Bucket4j token-bucket algorithm with configurable per-path rules
- **CSRF Protection** — SPA-aware CSRF with X-XSRF-TOKEN header
- **Role-Based Access** — Three roles: CUSTOMER, SELLER, ADMIN
- **Account Lockout** — Auto-lockout after 5 failed attempts (30-min duration)

---

## Architecture

### High-Level Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│  Browser (Thymeleaf + Vanilla JS)                               │
│  ├── Chat Widget (floating, IIFE module)                        │
│  ├── Admin Dashboard                                            │
│  └── Seller Portal                                              │
├─────────────────────────────────────────────────────────────────┤
│                        API LAYER                                │
│  Spring Boot 4.1 MVC                                            │
│  ├── Public: /items/**, /categories/**                          │
│  ├── Auth: /auth/**, /user/**                                   │
│  ├── Seller: /seller/**                                         │
│  ├── Admin: /api/admin/**                                       │
│  └── AI: /api/ai/**, /api/ai/guest/**                           │
├─────────────────────────────────────────────────────────────────┤
│                      SERVICE LAYER                              │
│  ├── ItemService, CartService, OrderService                     │
│  ├── PaymentService (Strategy Pattern)                          │
│  ├── UserService, SellerService                                 │
│  ├── EmailService (Strategy Pattern)                            │
│  └── ChatService (LangChain4j Integration)                      │
├─────────────────────────────────────────────────────────────────┤
│                      DATA LAYER                                 │
│  ├── MySQL (JPA + Flyway Migrations)                            │
│  ├── Qdrant (Vector Store for RAG)                              │
│  └── ConcurrentHashMap (Rate Limiting, Chat Memory)             │
├─────────────────────────────────────────────────────────────────┤
│                    AI/ML LAYER                                  │
│  LangChain4j 1.17.2                                             │
│  ├── NVIDIA NIM (LLM Provider)                                  │
│  ├── BGE-small-en-v1.5 (Local Embeddings)                       │
│  ├── Qdrant (Vector Store)                                      │
│  └── Agentic Tool Loop (6 @Tool methods)                        │
└─────────────────────────────────────────────────────────────────┘
```

> 📎 A detailed component-level diagram of the AI chat subsystem is available at [`docs/aiservice/data/shoppiq-ai-chat-architecture.svg`](docs/aiservice/data/shoppiq-ai-chat-architecture.svg).

### Entity Relationship Diagram

```
User (1) ──── (*) Order ──── (*) OrderItem ──── (*) Item
  │                 │                                 │
  │                 │                                 │
  ├── Cart (1:1)    ├── Address                       ├── ItemDetails (1:1)
  │    │            ├── PromoCode                     │   │
  │    │            │                                 │   └── Category
  │    │            └── Payment                       │
  │    │                                              └── ItemReview (*) ──── User
  │    └── CartItem (*) ──── Item
  │
  ├── (*) Address
  ├── (*) ItemReview
  ├── (*) ChatConversation ──── (*) ChatMessage
  └── (*) NotificationPreference

Seller (1:1) ──── User
Store (1:1) ──── Seller
```

---

## Tech Stack

### Backend

| Component | Technology | Version |
|---|---|---|
| Language | Java | 25 |
| Framework | Spring Boot | 4.1.0 |
| Security | Spring Security | 7.1 |
| ORM | Spring Data JPA + Hibernate | — |
| Database | MySQL | 8.x |
| Migrations | Flyway | — |
| Template Engine | Thymeleaf | — |
| JWT | JJWT | 0.13.0 |
| Rate Limiting | Bucket4j | 8.19.0 |
| Validation | Jakarta Validation | — |

### AI/ML

| Component | Technology | Version |
|---|---|---|
| AI Framework | LangChain4j | 1.17.2-beta27 |
| LLM Provider | NVIDIA NIM | — |
| LLM Model | Nemotron 49B / Llama 3.x | — |
| Embeddings | BGE-small-en-v1.5 | 1.12.0-beta20 |
| Vector Store | Qdrant | v1.13.0 |
| Embedding Runtime | ONNX Runtime | 1.26.0 |

### Infrastructure

| Component | Technology |
|---|---|
| Container | Docker + Docker Compose |
| Vector DB | Qdrant (containerized) |
| Primary DB | MySQL (local) |
| Payment | Razorpay / Stripe / PayPal |
| Email | SMTP (Gmail) |

---

## Quick Start

### Prerequisites

- Java 25+
- Maven 3.9+
- MySQL 8.x
- Docker + Docker Compose
- NVIDIA NIM API Key (for AI features)

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/shoppiq.git
cd shoppiq
```

### 2. Set Up Environment Variables

```bash
# Copy the example environment file
cp .env.example .env

# Edit .env with your configuration
# Required:
#   DATASOURCE_URL, DATASOURCE_USERNAME, DATASOURCE_PASSWORD
#   JWT_SECRET
#   GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET
#   NVIDIA_API_KEY (for AI features)
```

### 3. Start Infrastructure

```bash
# Start Qdrant vector database
docker-compose up -d
```

### 4. Set Up Database

```bash
# Create MySQL database
mysql -u root -p -e "CREATE DATABASE shoppiq_schema"

# Flyway will auto-create tables on first run
```

### 5. Build & Run

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run

# Or with AI features enabled
SPRING_PROFILES_ACTIVE=default,ai-enabled ./mvnw spring-boot:run
```

### 6. Access the Application

| URL | Description |
|---|---|
| `http://localhost:8090` | Homepage |
| `http://localhost:8090/admin` | Admin Dashboard |
| `http://localhost:8090/seller` | Seller Portal |
| `http://localhost:8090/api/ai/chat` | AI Chat API |

---

## Project Structure

```
shoppiq/
├── src/main/java/com/pkmprojects/shoppiq/
│   ├── aiservice/                 # AI Chat Service (LangChain4j)
│   │   ├── config/                # ChatServiceConfig, ChatMemoryConfig, RagConfig
│   │   ├── controller/            # AiChatController, AiGuestChatController
│   │   ├── dto/                   # ChatRequest, ChatResponse, DTOs
│   │   ├── entity/                # ChatConversation, ChatMessage
│   │   ├── events/                # ProductEmbeddingEvent, EntityListener
│   │   ├── ingestion/             # ProductCatalogIngester
│   │   ├── repository/            # ChatConversationRepository, ChatMessageRepository
│   │   ├── service/               # ChatService, ShoppiqAssistant
│   │   └── tools/                 # ShoppiqTools (6 @Tool methods)
│   ├── auth/                      # Authentication & Authorization
│   │   ├── controller/            # AuthController
│   │   ├── dto/                   # JwtRequest, JwtResponse
│   │   ├── jwt/                   # JwtAuthenticationFilter
│   │   ├── oauth2/                # OAuth2SuccessHandler, Cookie Services
│   │   ├── service/               # AuthService, CustomUserDetailService
│   │   └── utils/                 # JwtAuthenticationUtils, JwtCookieFactory
│   ├── config/                    # SecurityConfig, CorsConfig, etc.
│   ├── controller/                # REST Controllers (admin/, seller/)
│   ├── dto/                       # Request/Response DTOs
│   ├── email/                     # Email Service (Strategy Pattern)
│   ├── entity/                    # JPA Entities
│   ├── enums/                     # Business Enums
│   ├── exception/                 # Custom Exceptions
│   ├── filter/                    # RateLimitFilter
│   ├── gateway/                   # Payment Gateways (Strategy Pattern)
│   ├── repository/                # JPA Repositories
│   ├── service/                   # Business Services
│   ├── util/                      # Utility Classes
│   └── verification/              # Email Verification
├── src/main/resources/
│   ├── db/migration/              # Flyway SQL Migrations
│   ├── templates/                 # Thymeleaf HTML Templates
│   ├── static/                    # CSS, JS, Images
│   └── application.yaml           # Application Configuration
├── docs/                          # Documentation
│   ├── aiservice/                 # AI Service Documentation
│   ├── authentication/            # Authentication Documentation
│   ├── catalog/                   # Product Catalog Documentation
│   ├── orders/                    # Orders & Cart Documentation
│   ├── payments/                  # Payment Gateway Documentation
│   ├── sellers/                   # Seller Service Documentation
│   ├── admin/                     # Admin Dashboard Documentation
│   ├── email/                     # Email Service Documentation
│   └── security/                  # Security & Infrastructure Documentation
├── docker-compose.yml             # Docker Infrastructure
├── pom.xml                        # Maven Configuration
└── .env.example                   # Environment Variables Template
```

---

## Services & Documentation

### Core Services

| Service | Documentation | Description |
|---|---|---|
| **AI Chat Assistant** | [docs/aiservice/aiservice.md](docs/aiservice/aiservice.md) | LangChain4j RAG + Agentic Tool Calling |
| **Authentication** | [docs/authentication/authentication.md](docs/authentication/authentication.md) | JWT + OAuth2 + Role-Based Access |
| **Product Catalog** | [docs/catalog/catalog.md](docs/catalog/catalog.md) | Items, Categories, Reviews |
| **Orders & Cart** | [docs/orders/orders.md](docs/orders/orders.md) | Shopping Cart, Checkout, Order Lifecycle |
| **Payment Gateway** | [docs/payments/payments.md](docs/payments/payments.md) | Razorpay, Stripe, PayPal, COD |
| **Seller Service** | [docs/sellers/sellers.md](docs/sellers/sellers.md) | Multi-Vendor Marketplace |
| **Admin Dashboard** | [docs/admin/admin.md](docs/admin/admin.md) | Analytics, Inventory, Reports |
| **Email Service** | [docs/email/email.md](docs/email/email.md) | SMTP, Templates, Preferences |
| **Security** | [docs/security/security.md](docs/security/security.md) | Rate Limiting, CSRF, CORS |

---

## API Overview

### Public Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/items/all` | Browse all products |
| `GET` | `/items/slug/{slug}` | Product by slug |
| `GET` | `/items/category/{slug}` | Products by category |
| `GET` | `/categories/all` | List all categories |
| `POST` | `/api/ai/guest` | Guest AI chat |
| `POST` | `/contact` | Submit contact message |
| `POST` | `/api/newsletter/subscribe` | Subscribe to newsletter |

### Authenticated Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/auth/login` | Login with email/password |
| `GET` | `/auth/google/authorization/google` | Google OAuth2 login |
| `POST` | `/user/cart/create` | Add to cart |
| `POST` | `/user/order/checkout` | Place order |
| `POST` | `/api/ai/chat` | Create AI conversation |

### Admin Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/dashboard/summary` | Dashboard analytics |
| `GET` | `/api/admin/inventory` | Product inventory |
| `GET` | `/api/admin/orders` | All orders |
| `GET` | `/api/admin/reports/sales` | Sales reports |
| `POST` | `/api/admin/promo-codes` | Create promo code |

### Seller Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/seller/register` | Register as seller |
| `GET` | `/seller/dashboard/summary` | Seller dashboard |
| `POST` | `/seller/products/create` | List new product |
| `GET` | `/seller/inventory` | Stock management |

---

## AI Features

### RAG Pipeline

```
User Query → Embedding (384-dim) → Vector Search (Qdrant) → Top-5 Results → LLM Context
     │              │                      │                      │
     │         Local ONNX            Cosine Similarity      maxResults=5
     │         BGE-small-en          minScore=0.75
     │
     └──► LLM Response with real product data
```

### Tool Calling (6 Tools)

| Tool | Description |
|---|---|
| `getProductDetail` | Fetch product info by slug or name |
| `getOrderStatus` | Check user's recent orders |
| `getCartContents` | Get shopping cart contents |
| `getUserReviews` | Retrieve user's product reviews |
| `semanticProductSearch` | Vector-based product search |
| `resolveCurrentConversation` | Close the conversation |

### Available Models

| Model | Label | Use Case |
|---|---|---|
| `nvidia/llama-3.3-nemotron-super-49b-v1.5` | Nemotron 49B | Default, most capable |
| `meta/llama-3.1-8b-instruct` | Llama 3.1 8B | Fast, lightweight |
| `meta/llama-4-maverick-17b-128e-instruct` | Llama 4 Maverick | Balanced |

---

## Security

### Authentication Flow

```
1. User                                    → POST /auth/login { email, password }
2. AuthService.authenticate()              → validates credentials
3. JwtAuthenticationUtils.generateToken()  → HMAC-SHA signed JWT
4. JwtCookieFactory.create()               → HttpOnly cookie (never in body)
5. JwtAuthenticationFilter                 → validates on every request
```

### Security Features

| Feature | Implementation |
|---|---|
| JWT Cookie-Only | HttpOnly, Secure, SameSite=Lax |
| OAuth2 Google | Stateless with HMAC-signed cookies |
| CSRF Protection | SPA mode with X-XSRF-TOKEN |
| Rate Limiting | Bucket4j token-bucket (per-path rules) |
| Account Lockout | 5 failed attempts → 30-min lock |
| Role-Based Access | CUSTOMER, SELLER, ADMIN |

---

## Configuration

### Environment Variables

| Variable | Required | Description |
|---|---|---|
| `DATASOURCE_URL` | Yes | MySQL connection URL |
| `DATASOURCE_USERNAME` | Yes | MySQL username |
| `DATASOURCE_PASSWORD` | Yes | MySQL password |
| `JWT_SECRET` | Yes | JWT signing secret (min 32 chars) |
| `GOOGLE_CLIENT_ID` | Yes | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | Yes | Google OAuth2 client secret |
| `NVIDIA_API_KEY` | For AI | NVIDIA NIM API key |
| `RAZORPAY_KEY_ID` | For payments | Razorpay API key |
| `RAZORPAY_KEY_SECRET` | For payments | Razorpay API secret |
| `SMTP_HOST` | For email | SMTP server host |
| `SMTP_USERNAME` | For email | SMTP username |
| `SMTP_PASSWORD` | For email | SMTP password |

### Application Ports

| Port | Service |
|---|---|
| 8090 | Shoppiq Application |
| 3306 | MySQL Database |
| 6333 | Qdrant REST API |
| 6334 | Qdrant gRPC API |

---

## Contributing

### Development Setup

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Run tests (`./mvnw test`)
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Code Standards

- Follow Java coding conventions
- Use Lombok annotations where appropriate
- Write comprehensive Javadoc for public APIs
- Add unit and integration tests for new features
- Update documentation for any API changes

> ⚠️ Note: this project is **source-available, not open-source** in the OSI sense — commercial use requires the Author's written permission (see [License](#license)). Contributions are welcome under the terms described there.

---

## License

This project is licensed under the **Shoppiq Custom Source-Available License v1.0** — free for personal, educational, and non-commercial use; **commercial use requires prior written permission** from the Author. The license automatically converts to Apache-2.0 on the Change Date (four years from first public release, or December 31, 2030, whichever comes first).

See the [LICENSE.md](LICENSE.md) file for full terms, or contact **Prabhat Kumar Mishra** at [mprabhat774@gmail.com](mailto:mprabhat774@gmail.com) for commercial licensing inquiries.

---

<div align="center">

**Built with passion for modern e-commerce**

[⬆ Back to Top](#-shoppiq)

</div>