# Square Games (SG) — Board Game Engine Microservice

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Port](https://img.shields.io/badge/Port-8080-blue.svg)](#)
[![Database](https://img.shields.io/badge/Database-PostgreSQL%20%2F%20H2-blue.svg)](#)
[![OpenAPI](https://img.shields.io/badge/Documentation-Swagger%20UI-green.svg)](#-interactive-swagger-ui-documentation)

Spring Boot microservice responsible for arbitrating, managing the lifecycle, and persisting board game sessions (**Tic-Tac-Toe**, **Connect Four**, **15-Puzzle**).

This service integrates into a distributed architecture and collaborates with the [**Square Users (SU)**](https://github.com/ImRobot777/spring_square_users) microservice for player authentication and validation.

---

## 🏗️ Architecture & Software Design

The project adheres to layered software architecture principles and strict decoupling:
- **Stateless Resource Server & Security (`config`, `service`)**: Spring Security 6 integration running in full `STATELESS` mode. The `JwtAuthenticationFilter` intercepts the standard `Authorization: Bearer <token>` header, verifies the asymmetric cryptographic signature using Square Users' RSA public key (`public.pem`), extracts the `userId` claim (`UUID`), and injects the authenticated identity directly into controllers via `@AuthenticationPrincipal` (zero network calls to SU to validate the game creator).
- **REST Presentation Layer (`controller`)**: Exposes HTTP routes conforming to REST standards, i18n content negotiation (`Accept-Language`), and OpenAPI 3 documentation concealing internal framework parameters (`@Parameter(hidden = true)`).
- **Business & Orchestration Layer (`service`)**: Enforces multiplayer rules, turn management, and game data integrity.
- **Plugin Layer (`plugin`)**: Implements the **Plugin** pattern (`GamePlugin`) to ensure open-ended extensibility without modifying existing codebase (*Open/Closed Principle*). Plugins load default parameters from `application.properties` (`@Value`) and localize game names via `MessageSource`.
- **Data Access Layer (`dao`, `entity`)**: The **DAO** pattern completely decouples business logic from persistence technologies. Dynamically supports in-memory persistence (`InMemoryGameDao`), explicit relational persistence (`JdbcGameDao`), and ORM (`JpaGameDao` with Spring Data JPA).
- **Inter-Service Communication (`client`)**: Declarative synchronous HTTP client leveraging **`RestClient`** (Spring Boot 3.2+) to verify invited opponents with Square Users.

```text
[ HTTP Client (Player with Bearer JWT) ]
                 │
                 ▼
[ JwtAuthenticationFilter ] ──(Local RSA verification via public.pem)──> [ SecurityContextHolder ]
                 │
                 ▼
[ GameController & GameCatalogController ] (@AuthenticationPrincipal UUID userId)
                 │
                 ▼
[ GameServiceImpl ] (@Service) ──(RestClient: opponent validation)──> [ Square Users (Port 8081) ]
         │               │
         ▼               ▼
[ Plugins Layer ]    [ DAO Layer (JpaGameDao) ]
 (TicTacToe, ...)                │
         │                       ▼
         ▼              [ PostgreSQL / H2 Database ]
[ engine.jar Engine ]
```

---

## 📋 Prerequisites

1. **Java Development Kit (JDK) 21** or higher:
   ```bash
   java -version
   ```
2. **Docker** (to run PostgreSQL locally):
   ```bash
   docker --version
   ```
3. **Access to the private GitHub Packages repository (Game Engine)**:
   This project depends on the proprietary engine `fr.le-campus-numerique.square-games:engine:1.0-SNAPSHOT`.
   To allow Maven to download this artifact, configure your local `~/.m2/settings.xml` file:

   ```xml
   <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">

       <servers>
           <server>
               <id>github</id>
               <username>YOUR_GITHUB_USERNAME</username>
               <password>YOUR_GITHUB_PERSONAL_ACCESS_TOKEN</password>
           </server>
       </servers>

       <profiles>
           <profile>
               <id>github</id>
               <repositories>
                   <repository>
                       <id>github</id>
                       <url>https://maven.pkg.github.com/le-campus-numerique/*</url>
                       <snapshots>
                           <enabled>true</enabled>
                       </snapshots>
                   </repository>
               </repositories>
           </profile>
       </profiles>

       <activeProfiles>
           <activeProfile>github</activeProfile>
       </activeProfiles>
   </settings>
   ```

---

## 🗄️ Database Infrastructure (Docker)

The service uses a containerized PostgreSQL 16 database listening on port `5432` by default:

```bash
# 1. Create Docker volume for data persistence
docker volume create sg-postgres-data

# 2. Start PostgreSQL container
docker run -d \
  --name sg-postgres \
  -p 5432:5432 \
  -e POSTGRES_DB=square_games \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -v sg-postgres-data:/var/lib/postgresql/data \
  postgres:16
```

To restart an existing container in subsequent sessions:
```bash
docker start sg-postgres
```

---

## ⚙️ Configuration & Environment Variables

The microservice can be configured through system environment variables (*12-Factor App* methodology). Each variable has a default fallback for local development:

| Environment Variable | Description | Local Default |
|---|---|---|
| `SG_SERVER_PORT` | HTTP server listening port | `8080` |
| `SG_DB_URL` | PostgreSQL JDBC connection URL | `jdbc:postgresql://localhost:5432/square_games` |
| `SG_DB_USER` | Database username | `postgres` |
| `SG_DB_PASSWORD` | Database password | `postgres` |
| `SU_SERVICE_URL` | Root URL of Square Users microservice for player validation | `http://localhost:8081` |
| `SG_JWT_PUBLIC_KEY_PATH` | Path to Square Users RSA public key | `classpath:certs/public_key.pem` |

---

## 🚀 Running the Application

### Option A: PostgreSQL Profile (Standard / Production)
Ensure the `sg-postgres` container is running, then execute:

```bash
./mvnw spring-boot:run
```
The application starts at `http://localhost:8080` and connects to `jdbc:postgresql://localhost:5432/square_games`.

### Option B: H2 Profile (Lightweight / Docker-free Mode)
To run instantly in memory without Docker dependencies:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```
*H2 web console available at: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:square_games`, User: `sa`, empty password).*

---

## 📖 Interactive Swagger UI Documentation

Once the application is running, the interactive Swagger UI provides full endpoint exploration, documentation, and live testing:

👉 **Swagger UI Interface**: [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html)  
👉 **OpenAPI 3 Specification (JSON)**: [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs)

> 💡 **Bearer Authentication**: Protected endpoints require a valid JWT token issued by Square Users (`SU`), sent via the standard `Authorization: Bearer <TOKEN>` header. The connected player's `userId` is automatically resolved in memory by Spring Security (`@AuthenticationPrincipal`).

---

## 🌐 Endpoint Guide & `curl` Examples

### 1. Get Technical IDs of Available Games (Public)
```bash
curl -X GET http://localhost:8080/gamesIds
```
*Response:* `["tictactoe", "15 puzzle", "connect4"]`

### 2. View Localized Catalog (i18n - Public)
```bash
# French names
curl -X GET http://localhost:8080/catalog -H "Accept-Language: fr"
# -> ["Puissance 4", "Morpion", "Taquin"]

# English names
curl -X GET http://localhost:8080/catalog -H "Accept-Language: en-US"
# -> ["Connect Four", "Tic-Tac-Toe", "15 Puzzle"]
```

### 3. Create a New Game (JWT Secured)
> ⚠️ **Important**: The `Authorization: Bearer <TOKEN>` header is required. The creator identity is guaranteed by the token's asymmetric signature.
```bash
curl -X POST http://localhost:8080/games \
  -H "Authorization: Bearer $TOKEN_ALICE" \
  -H "Content-Type: application/json" \
  -d '{
    "gameFactoryId": "tictactoe",
    "boardSize": 3,
    "opponentIds": ["c9a16f43-5678-4b90-c123-9876543210ef"]
  }'
```

### 4. List Connected Player's Games (JWT Secured)
```bash
curl -X GET http://localhost:8080/games \
  -H "Authorization: Bearer $TOKEN_ALICE"
```

### 5. Get Game State by ID
```bash
curl -X GET http://localhost:8080/games/<GAME_UUID> \
  -H "Authorization: Bearer $TOKEN_ALICE"
```

### 6. Get Available Playable Moves
```bash
curl -X GET http://localhost:8080/games/<GAME_UUID>/moves \
  -H "Authorization: Bearer $TOKEN_ALICE"
```

> 💡 **Architectural Note — Open Query vs Strict Mutation ("Conforme par conception / By Design")**:  
> Read-only endpoints (`GET /games/{id}` and `GET /games/{id}/moves`) are intentionally open to any authenticated user to enable **spectator mode** and client-side **board highlighting** (rendering immediately playable cells in real time).  
> The true security boundary lies in state mutation (`POST /games/{id}/moves`), which is strictly enforced in `GameServiceImpl` (validating the active player `game.getCurrentPlayerId().equals(userId)`, returning `403 FORBIDDEN` otherwise). In software audits, this behavior is formally classified as **"Conforme par conception" (By Design)**, as it stems from a deliberate design choice rather than a security oversight.

### 7. Play a Move (JWT Secured)
```bash
curl -X POST http://localhost:8080/games/<GAME_UUID>/moves \
  -H "Authorization: Bearer $TOKEN_ALICE" \
  -H "Content-Type: application/json" \
  -d '{
    "target": { "x": 1, "y": 1 }
  }'
```
*If the token is missing or invalid, the API returns `401 UNAUTHORIZED`. If it is not your turn or if you are not a registered player in the game, the API returns `403 FORBIDDEN`.*

---

## 🧪 Automated Test Suite Execution

The project includes an extensive suite of unit and integration tests validating REST controllers (with `AuthenticationPrincipalArgumentResolver`), business services, and inter-service clients using **JUnit 5**, **Mockito**, and **MockMvc**:

```bash
# Run all tests (20 tests, 0 failures)
./mvnw clean test -Dspring.profiles.active=h2
```

---

## 🔄 Full Microservices Integration Scenario (SG + SU)

To test the entire secured flow between both microservices:

1. **Start Square Users** on port `8081`:
   ```bash
   cd ../spring_square_users && ./mvnw spring-boot:run
   ```
2. **Create two players** in Square Users:
   ```bash
   # Create Alice
   curl -X POST http://localhost:8081/users \
     -H "Content-Type: application/json" \
     -d '{"pseudo": "Alice", "email": "alice@test.com", "password": "passwordAlice123!"}'
   # -> Note Alice's UUID (e.g. uuid_alice)

   # Create Bob (opponent)
   curl -X POST http://localhost:8081/users \
     -H "Content-Type: application/json" \
     -d '{"pseudo": "Bob", "email": "bob@test.com", "password": "passwordBob123!"}'
   # -> Note Bob's UUID (e.g. uuid_bob)
   ```
3. **Authenticate Alice and retrieve her JWT** from Square Users:
   ```bash
   TOKEN_ALICE=$(curl -s -X POST http://localhost:8081/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "Alice", "password": "passwordAlice123!"}' | jq -r '.token')
   ```
4. **Start Square Games** on port `8080`:
   ```bash
   cd ../spring_square_games && ./mvnw spring-boot:run
   ```
5. **Create a game on Square Games using Alice's token**:
   ```bash
   curl -X POST http://localhost:8080/games \
     -H "Authorization: Bearer $TOKEN_ALICE" \
     -H "Content-Type: application/json" \
     -d '{"gameFactoryId": "tictactoe", "boardSize": 3, "opponentIds": ["<uuid_bob>"]}'
   ```
6. **Verify security & arbitration**:
   - If the request is sent without a token or with a tampered token: `401 UNAUTHORIZED`.
   - If a player attempts to play out of turn: `403 FORBIDDEN`.
   - The creator identity is verified locally in RAM (zero network latency), while opponent Bob's existence is checked via `RestClient` against `SU`.
