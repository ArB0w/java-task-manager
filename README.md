# Task Manager

Webová aplikace pro správu osobních úkolů vytvořená pomocí Java a Spring Boot. Aplikace umožňuje registrovaným uživatelům spravovat vlastní úkoly a sledovat jejich změny v reálném čase prostřednictvím WebSocket komunikace.

## Použité technologie

**Backend**

- Java 21
- Spring Boot 4.0.9
- Spring Web MVC
- Spring Data JPA
- Spring Security
- Hibernate
- Bean Validation
- WebSocket a STOMP
- Maven

**Databáze**

- PostgreSQL 16
- SQL

**Frontend**

- HTML5
- CSS3
- JavaScript
- STOMP.js

**Testování**

- JUnit 5
- Mockito
- Spring Boot Test
- MockMvc

## Funkcionalita

### Správa úkolů

- Vytváření, načítání, úprava a mazání úkolů (CRUD).
- Změna stavu úkolu pomocí PATCH.
- Evidence názvu, popisu, termínu dokončení a data vytvoření.
- Filtrování úkolů podle názvu a stavu dokončení.
- Vyhledávání na straně serveru.

### Uživatelé a autentizace

- Registrace nových uživatelů.
- Přihlašování pomocí e-mailu a hesla.
- Hashování hesel pomocí BCrypt.
- Autentizace a autorizace prostřednictvím Spring Security.
- Oddělení úkolů jednotlivých uživatelů.
- Kontrola vlastnictví úkolů při jejich načítání a úpravách.

### Real-time komunikace

- WebSocket komunikace pomocí protokolu STOMP.
- Ověřování uživatelů při navázání STOMP spojení.
- Odesílání událostí při vytvoření, úpravě a odstranění úkolu.
- Směrování zpráv konkrétním přihlášeným uživatelům.
- Automatická aktualizace seznamu úkolů v dalších otevřených klientech.

### Validace a zpracování chyb

- Validace vstupních dat pomocí Bean Validation.
- Kontrola povinných polí a maximální délky textu.
- Kontrola termínu dokončení.
- Globální zpracování výjimek.
- Používání odpovídajících HTTP status kódů.

## Architektura projektu

Projekt využívá vrstvenou architekturu:

- **Controller** – zpracování HTTP požadavků a komunikace s klientem.
- **Service** – aplikační logika a práce s aktuálním uživatelem.
- **Repository** – přístup k databázi prostřednictvím Spring Data JPA.
- **Model** – databázové entity uživatelů a úkolů.
- **DTO** – objekty pro přenos dat.
- **Config** – konfigurace Spring Security a WebSocket komunikace.
- **Exception** – globální zpracování výjimek.

## REST API

Základní prefix API:

`/api`

| Metoda | Endpoint                    | Popis                          |
| ------ | --------------------------- | ------------------------------ |
| POST   | `/api/auth/register`        | Registrace uživatele           |
| GET    | `/api/tasks`                | Načtení vlastních úkolů        |
| GET    | `/api/tasks/{id}`           | Načtení úkolu podle ID         |
| POST   | `/api/tasks`                | Vytvoření úkolu                |
| PUT    | `/api/tasks/{id}`           | Úprava úkolu                   |
| PATCH  | `/api/tasks/{id}/completed` | Změna stavu dokončení          |
| DELETE | `/api/tasks/{id}`           | Odstranění úkolu               |
| GET    | `/api/tasks/search`         | Vyhledávání a filtrování úkolů |

Vyhledávání podporuje volitelné parametry:

- `title` – vyhledávání podle názvu.
- `completed` – filtrování podle stavu dokončení.

Příklad:

`GET /api/tasks/search?title=projekt&completed=false`

## WebSocket komunikace

WebSocket endpoint:

`/ws`

Komunikace využívá protokol STOMP a uživatelské destinace.

Klienti odebírají zprávy na:

`/user/queue/tasks`

Server odesílá události konkrétnímu uživateli prostřednictvím `SimpMessagingTemplate` a metody `convertAndSendToUser()`.

Události informují o vytvoření, úpravě nebo odstranění úkolu. Klient následně obnoví seznam úkolů prostřednictvím REST API.

## Spuštění projektu

### Požadavky

- Java 21
- Maven (nebo přiložený Maven Wrapper)
- PostgreSQL 16

### Databáze

V PostgreSQL je potřeba vytvořit databázi `task_manager` a uživatele s odpovídajícími oprávněními.

Připojení k databázi je nutné nakonfigurovat v souboru `application.properties` podle lokálního prostředí.

### Spouštění a správa aplikace

**PostgreSQL**

Zobrazení stavu služby:

```bash
sudo service postgresql status
```

Spuštění služby:

```bash
sudo service postgresql start
```

Zastavení služby:

```bash
sudo service postgresql stop
```

**Maven / Spring Boot**

Spuštění aplikace:

```bash
./mvnw spring-boot:run
```

Spuštění testů:

```bash
./mvnw test
```

Smazání předchozích sestavených souborů:

```bash
./mvnw clean
```

Kompletní vyčištění a spuštění testů:

```bash
./mvnw clean test
```

Aplikace bude standardně dostupná na:

`http://localhost:8080`

## Plánovaná rozšíření

- Integrační testy WebSocket komunikace.
- Verzování databázového schématu pomocí Flyway – migrace.
- Kontejnerizace aplikace a databáze pomocí Dockeru a Docker Compose.
- Dokumentace REST API pomocí OpenAPI / Swagger.
- Další bezpečnostní úpravy a příprava produkčního nasazení.
