# Task manager Java

Demonstrační projet.
Pracuje se na měm...

### PostgreSQL

sudo service postgresql status
sudo service postgresql start
sudo service postgresql stop

### Maven

./mvnw spring-boot:run
./mvnw test
./mvnw clean

### DONE

**Správa úkolů**

REST API pro vytváření, načítání, úpravu a mazání úkolů (CRUD). Částečné aktualizace pomocí PATCH.

**Databáze**

PostgreSQL, Spring Data JPA a Hibernate. Ukládání a načítání úkolů, vyhledávání a filtrování na straně serveru.

**Validace a zpracování chyb**

Bean Validation, kontrola vstupních dat a globální zpracování výjimek. Odpovídající HTTP status kódy.

**WebSocket**

Real-time aktualizace úkolů pomocí WebSocket a STOMP. Automatické obnovení dat ve frontendových klientech.

**Frontend**

Statické webové rozhraní pomocí HTML, CSS a JavaScriptu. Propojení s REST API a WebSocket komunikací.

**Testování**

JUnit 5, Mockito a Spring Boot Test. Jednotkové testy servisní vrstvy, testy REST controllerů pomocí MockMvc a integrační testy PostgreSQL.

### TODO

**Přihlašování a uživatelé**

Spring Security, registrace, BCrypt, autentizace, autorizace a později případně JWT. Shere úkolu

**Databáze a migrace**

Flyway nebo Liquibase, aby se změny databázového schématu daly verzovat.

**Docker**

Kontejnerizace Spring Boot aplikace a PostgreSQL pomocí Docker Compose.

**Dokumentace API**

OpenAPI a Swagger UI pro přehledné testování a dokumentaci endpointů.
