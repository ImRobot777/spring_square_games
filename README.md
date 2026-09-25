# Square Games (SG) — Microservice de Moteur de Jeux de Plateau

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Port](https://img.shields.io/badge/Port-8080-blue.svg)](#)
[![Database](https://img.shields.io/badge/Database-PostgreSQL%20%2F%20H2-blue.svg)](#)
[![OpenAPI](https://img.shields.io/badge/Documentation-Swagger%20UI-green.svg)](#-documentation-interactive-swagger-ui)

Microservice Spring Boot responsable de l'arbitrage, de la gestion du cycle de vie et de la persistance des parties de jeux de plateau (**Morpion / Tic-Tac-Toe**, **Puissance 4 / Connect Four**, **Taquin / 15-Puzzle**).

Ce service s'intègre dans une architecture distribuée et collabore avec le microservice [**Square Users (SU)**](https://github.com/ImRobot777/spring_square_users) pour l'authentification et la validation des joueurs.

---

## 🏗️ Architecture & Conception Logicielle

Le projet applique les principes de l'architecture logicielle en couches et du découplage strict :
- **Sécurité & Resource Server Stateless (`config`, `service`)** : Intégration de Spring Security 6 en mode 100% sans état (`STATELESS`). Le filtre `JwtAuthenticationFilter` intercepte l'en-tête standard `Authorization: Bearer <token>`, valide la signature cryptographique asymétrique à l'aide de la clé publique RSA de Square Users (`public.pem`), extrait le claim `userId` (`UUID`) et injecte l'identité authentifiée directement dans le contrôleur via `@AuthenticationPrincipal` (zéro requête réseau vers SU pour valider le créateur).
- **Couche Présentation REST (`controller`)** : Exposition des routes HTTP conformes aux standards REST, négociation de contenu i18n (`Accept-Language`), documentation OpenAPI 3 masquant les paramètres internes (`@Parameter(hidden = true)`).
- **Couche Métier & Orchestration (`service`)** : Application des règles multi-joueurs, contrôle du tour de jeu et intégrité des données.
- **Couche Plugins (`plugin`)** : Implémentation du patron **Plugin** (`GamePlugin`) pour rendre le système ouvert à l'extension sans modifier le code existant (*Open/Closed Principle*). Les plugins chargent leurs paramètres par défaut depuis `application.properties` (`@Value`) et traduisent les noms via `MessageSource`.
- **Couche d'Accès aux Données (`dao`, `entity`)** : Patron **DAO** isolant totalement le métier de la technologie de persistance. Support dynamique de persistance en mémoire vive (`InMemoryGameDao`), relationnelle explicite (`JdbcGameDao`) et ORM (`JpaGameDao` avec Spring Data JPA).
- **Communication Inter-Services (`client`)** : Client HTTP synchrone déclaratif s'appuyant sur **`RestClient`** (Spring Boot 3.2+) pour vérifier l'existence des adversaires invités auprès de Square Users.

```text
[ Client HTTP (Joueur avec Bearer JWT) ]
                 │
                 ▼
[ JwtAuthenticationFilter ] ──(Validation RSA locale public.pem)──> [ SecurityContextHolder ]
                 │
                 ▼
[ GameController & GameCatalogController ] (@AuthenticationPrincipal UUID userId)
                 │
                 ▼
[ GameServiceImpl ] (@Service) ──(RestClient: validation adversaires)──> [ Square Users (Port 8081) ]
         │               │
         ▼               ▼
[ Couche Plugins ]   [ Couche DAO (JpaGameDao) ]
 (TicTacToe, ...)                │
         │                       ▼
         ▼              [ Base PostgreSQL / H2 ]
[ Moteur engine.jar ]
```

---

## 📋 Prérequis

1. **Java Development Kit (JDK) 21** ou supérieur :
   ```bash
   java -version
   ```
2. **Docker** (pour exécuter PostgreSQL localement) :
   ```bash
   docker --version
   ```
3. **Accès au repository GitHub Packages privé (Moteur de jeu)** :
   Le projet dépend du moteur propriétaire `fr.le-campus-numerique.square-games:engine:1.0-SNAPSHOT`.
   Pour que Maven puisse télécharger ce composant, configurez votre fichier local `~/.m2/settings.xml` :

   ```xml
   <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">

       <servers>
           <server>
               <id>github</id>
               <username>VOTRE_IDENTIFIANT_GITHUB</username>
               <password>VOTRE_PERSONAL_ACCESS_TOKEN_GITHUB</password>
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

## 🗄️ Infrastructure Base de Données (Docker)

Le service utilise par défaut une base de données PostgreSQL 16 conteneurisée sur le port d'écoute `5432` :

```bash
# 1. Créer le volume Docker pour la rétention des données
docker volume create sg-postgres-data

# 2. Démarrer le conteneur PostgreSQL
docker run -d \
  --name sg-postgres \
  -p 5432:5432 \
  -e POSTGRES_DB=square_games \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -v sg-postgres-data:/var/lib/postgresql/data \
  postgres:16
```

Pour redémarrer un conteneur arrêté lors des sessions suivantes :
```bash
docker start sg-postgres
```

---

## 🚀 Démarrage de l'Application

### Option A : Profil PostgreSQL (Standard / Production)
Assurez-vous que le conteneur `sg-postgres` est actif, puis lancez :

```bash
./mvnw spring-boot:run
```
L'application démarre sur `http://localhost:8080` et se connecte à `jdbc:postgresql://localhost:5432/square_games`.

### Option B : Profil H2 (Mode Léger / Sans Docker)
Pour exécuter l'application instantanément sans dépendance Docker grâce à une base mémoire H2 :

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2
```
*Console web H2 disponible sur : `http://localhost:8080/h2-console` (JDBC URL : `jdbc:h2:mem:square_games`, User : `sa`, mot de passe vide).*

---

## 📖 Documentation Interactive Swagger UI

Dès que l'application est démarrée, l'interface interactive Swagger UI permet d'explorer, de documenter et de tester l'ensemble des endpoints en direct :

👉 **Interface Swagger UI** : [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html)  
👉 **Spécification OpenAPI 3 (JSON)** : [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs)

> 💡 **Authentification Bearer** : Les routes protégées nécessitent un jeton JWT valide émis par Square Users (`SU`), transmis via l'en-tête standard `Authorization: Bearer <TOKEN>`. L'identifiant `userId` du joueur connecté est extrait automatiquement du jeton en mémoire vive par Spring Security (`@AuthenticationPrincipal`).

---

## 🌐 Guide des Endpoints & Exemples `curl`

### 1. Obtenir les identifiants techniques des jeux disponibles (Public)
```bash
curl -X GET http://localhost:8080/gamesIds
```
*Réponse :* `["tictactoe", "15 puzzle", "connect4"]`

### 2. Consulter le catalogue traduit (i18n - Public)
```bash
# Noms en français
curl -X GET http://localhost:8080/catalog -H "Accept-Language: fr"
# -> ["Puissance 4", "Morpion", "Taquin"]

# Noms en anglais
curl -X GET http://localhost:8080/catalog -H "Accept-Language: en-US"
# -> ["Connect Four", "Tic-Tac-Toe", "15 Puzzle"]
```

### 3. Créer une nouvelle partie (Sécurisé par JWT)
> ⚠️ **Important** : L'en-tête `Authorization: Bearer <TOKEN>` est obligatoire. Le créateur est garanti et certifié par la signature cryptographique asymétrique du jeton.
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

### 4. Lister les parties du joueur connecté (Sécurisé par JWT)
```bash
curl -X GET http://localhost:8080/games \
  -H "Authorization: Bearer $TOKEN_ALICE"
```

### 5. Consulter l'état d'une partie par son identifiant
```bash
curl -X GET http://localhost:8080/games/<GAME_UUID> \
  -H "Authorization: Bearer $TOKEN_ALICE"
```

### 6. Obtenir la liste des coups immédiatement jouables
```bash
curl -X GET http://localhost:8080/games/<GAME_UUID>/moves \
  -H "Authorization: Bearer $TOKEN_ALICE"
```

### 7. Jouer un coup (Sécurisé par JWT)
```bash
curl -X POST http://localhost:8080/games/<GAME_UUID>/moves \
  -H "Authorization: Bearer $TOKEN_ALICE" \
  -H "Content-Type: application/json" \
  -d '{
    "target": { "x": 1, "y": 1 }
  }'
```
*Si le jeton est manquant ou invalide, l'API renvoie `401 UNAUTHORIZED`. Si ce n'est pas votre tour ou si vous n'êtes pas un joueur de la partie, l'API renvoie `403 FORBIDDEN`.*

---

## 🧪 Exécution des Tests Automatisés

Le projet inclut une suite de tests unitaires et d'intégration validant les contrôleurs REST (avec `AuthenticationPrincipalArgumentResolver`), les services métier et les clients inter-services avec **JUnit 5**, **Mockito** et **MockMvc** :

```bash
# Exécution de l'intégralité des tests (18 tests, 0 échec)
./mvnw clean test -Dspring.profiles.active=h2
```

---

## 🔄 Scénario d'Intégration Microservices Complet (SG + SU)

Pour tester la chaîne complète sécurisée entre les deux microservices :

1. **Démarrer Square Users** sur le port `8081` :
   ```bash
   cd ../spring_square_users && ./mvnw spring-boot:run
   ```
2. **Créer deux joueurs** dans Square Users :
   ```bash
   # Création d'Alice
   curl -X POST http://localhost:8081/users \
     -H "Content-Type: application/json" \
     -d '{"pseudo": "Alice", "email": "alice@test.com", "password": "passwordAlice123!"}'
   # -> Noter l'UUID Alice (ex: uuid_alice)

   # Création de Bob (adversaire)
   curl -X POST http://localhost:8081/users \
     -H "Content-Type: application/json" \
     -d '{"pseudo": "Bob", "email": "bob@test.com", "password": "passwordBob123!"}'
   # -> Noter l'UUID Bob (ex: uuid_bob)
   ```
3. **Authentifier Alice et récupérer son jeton JWT** auprès de Square Users :
   ```bash
   TOKEN_ALICE=$(curl -s -X POST http://localhost:8081/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "Alice", "password": "passwordAlice123!"}' | jq -r '.token')
   ```
4. **Démarrer Square Games** sur le port `8080` :
   ```bash
   cd ../spring_square_games && ./mvnw spring-boot:run
   ```
5. **Créer la partie sur Square Games avec le jeton d'Alice** :
   ```bash
   curl -X POST http://localhost:8080/games \
     -H "Authorization: Bearer $TOKEN_ALICE" \
     -H "Content-Type: application/json" \
     -d '{"gameFactoryId": "tictactoe", "boardSize": 3, "opponentIds": ["<uuid_bob>"]}'
   ```
6. **Vérifier la sécurité et l'arbitrage** :
   - Si la requête est envoyée sans jeton ou avec un jeton corrompu : `401 UNAUTHORIZED`.
   - Si un joueur tente de jouer hors de son tour : `403 FORBIDDEN`.
   - L'identité du créateur est validée en mémoire vive locale (zéro latence réseau), tandis que l'existence de l'adversaire Bob est vérifiée via `RestClient` auprès de `SU`.
