# Vaadin App (Spring Boot)

## Titre et description du projet

Application web construite avec **Vaadin** (UI) et **Spring Boot** (backend). Le projet utilise **Maven** (via Maven Wrapper) et une base **H2** (par défaut) avec **Spring Data JPA / Hibernate**.

## Technologies utilisées

- Java 17
- Spring Boot (starter web, validation)
- Spring Data JPA / Hibernate
- Vaadin (vaadin-spring-boot-starter)
- H2 Database (runtime)
- Spring Security Crypto (BCrypt / hash)
- Maven + Maven Wrapper (`mvnw` / `mvnw.cmd`)

## Prérequis et installation

### Prérequis

- **JDK 17** installé et disponible dans le `PATH`
- (Optionnel) **Maven**  sinon utilisez le **Maven Wrapper** inclus

### Installation

Depuis la racine du projet :

#### Windows (PowerShell)

```powershell
.\mvnw.cmd clean package
```

#### macOS / Linux

```bash
./mvnw clean package
```

## Configuration de la base de données

La configuration se trouve dans `src/main/resources/application.properties`.

### Configuration actuelle (par défaut)

Le projet est configuré sur **H2 en mémoire** :

- URL: `jdbc:h2:mem:testdb`
- Driver: `org.h2.Driver`
- Utilisateur: `sa`
- Mot de passe: (vide)

La console H2 est activée (`spring.h2.console.enabled=true`). Par défaut, elle est généralement accessible via `/h2-console`.

### Changer de base de données

- Pour une base H2 fichier, vous pouvez remplacer lURL par un chemin fichier (ex: `jdbc:h2:file:./data/vaadinapp`).
- Pour une base externe (MySQL/PostgreSQL/etc.), il faudra :
  - changer `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`,
  - ajouter le driver JDBC correspondant dans le `pom.xml`,
  - ajuster éventuellement `spring.jpa.hibernate.ddl-auto`.

Pour plus de détails, voir [DATABASE_GUIDE.md](DATABASE_GUIDE.md).

## Instructions de lancement

### Mode développement

#### Windows (PowerShell)

```powershell
.\mvnw.cmd spring-boot:run
```

#### macOS / Linux

```bash
./mvnw spring-boot:run
```

Puis ouvrir `http://localhost:8080` (ou le port configuré via `server.port`).

### Mode production (optionnel)

```bash
./mvnw -Pproduction clean package
```

## Références

- Guide DB : [DATABASE_GUIDE.md](DATABASE_GUIDE.md)
- Guide structure/admin : [ADMIN_STRUCTURE_GUIDE.md](ADMIN_STRUCTURE_GUIDE.md)
