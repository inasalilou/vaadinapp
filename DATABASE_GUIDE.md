# Database Management Guide

## 📊 Database Overview

This project uses **H2 Database** - an in-memory, lightweight Java database that's perfect for development and testing.

### Database Configuration

**Location:** `src/main/resources/application.properties`

```properties
# H2 Database (In-Memory)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# H2 Console (Web Interface)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### Key Features

1. **In-Memory Database**: Data is stored in RAM, not on disk
   - ⚠️ **Important**: All data is lost when the application stops
   - ✅ **Benefit**: Perfect for testing - clean state on every restart

2. **Auto-Schema Creation**: Hibernate automatically creates tables based on your entities
   - `ddl-auto=update` means tables are created/updated automatically

3. **H2 Console**: Web-based database browser for viewing data

## 🔐 Test Users Created Automatically

When the application starts, the `DataInit` class creates test users:

### Admin User (for testing admin pages)
- **Email:** `admin@test.com`
- **Password:** `admin123`
- **Role:** `ADMIN`

### Organizer User
- **Email:** `organizer@test.com`
- **Password:** `password123`
- **Role:** `ORGANIZER`

### Client User
- **Email:** `client@test.com`
- **Password:** `password123`
- **Role:** `CLIENT`

## 🚀 How to Test Admin Pages

### Step 1: Start the Application
```bash
mvn spring-boot:run
```
or run from your IDE

### Step 2: Login as Admin
1. Navigate to: `http://localhost:8080/login`
2. Enter credentials:
   - Email: `admin@test.com`
   - Password: `admin123`
3. Click "Se connecter"

### Step 3: Access Admin Pages
After login, you can access:
- **Admin Dashboard:** `http://localhost:8080/admin/dashboard`
- **User Management:** `http://localhost:8080/admin/users`
- **Events Management:** `http://localhost:8080/admin/events`
- **Reservations Management:** `http://localhost:8080/admin/reservations`

## 🗄️ Accessing H2 Console (Database Browser)

### Step 1: Start the Application
Make sure the app is running on port 8080

### Step 2: Open H2 Console
Navigate to: `http://localhost:8080/h2-console`

### Step 3: Connect to Database
Use these connection settings:
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **User Name:** `sa`
- **Password:** (leave empty)
- Click **Connect**

### Step 4: View Data
You can now run SQL queries:
```sql
-- View all users
SELECT * FROM users;

-- View all events
SELECT * FROM event;

-- View all reservations
SELECT * FROM reservation;

-- View users by role
SELECT * FROM users WHERE role = 'ADMIN';
```

## 🔄 How Database Initialization Works

### DataInit Class
**Location:** `src/main/java/com/inas/vaadinapp/config/DataInit.java`

This class:
1. Runs automatically when the application starts (`@PostConstruct`)
2. Checks if data already exists (prevents duplicates)
3. Creates test users with **hashed passwords** (BCrypt)
4. Creates sample events
5. Only runs if the database is empty

### Password Hashing
- Passwords are hashed using **BCryptPasswordEncoder**
- Plain text passwords are never stored
- The `UserService` handles password encoding/verification

## 🧪 Resetting the Database

Since H2 is in-memory, simply **restart the application**:
1. Stop the application
2. Start it again
3. All data will be recreated from `DataInit`

## 📝 Database Schema

### Main Tables

1. **users**
   - id, nom, prenom, email, password, role, date_inscription, actif, telephone

2. **event**
   - id, titre, description, categorie, ville, lieu, prix_unitaire, capacite_max, date_debut, date_fin, status, organisateur_id, date_creation

3. **reservation**
   - id, code_reservation, nb_places, montant_total, status, date_reservation, commentaire, client_id, event_id

### Relationships
- Event → User (Many-to-One: organizer)
- Reservation → User (Many-to-One: client)
- Reservation → Event (Many-to-One: event)

## 🔧 Changing Database Configuration

### Switch to File-Based H2 (Persistent)
To keep data between restarts, change in `application.properties`:
```properties
# From in-memory to file-based
spring.datasource.url=jdbc:h2:file:./data/eventmanager
```

### Switch to MySQL/PostgreSQL
1. Add dependency in `pom.xml`
2. Update `application.properties` with new connection details
3. Change `ddl-auto` to `validate` or `none` in production

## ⚠️ Important Notes

1. **In-Memory = No Persistence**: Data is lost on restart
2. **Development Only**: H2 in-memory is not suitable for production
3. **Password Security**: All passwords are hashed with BCrypt
4. **Auto-Initialization**: DataInit only runs if database is empty

## 🐛 Troubleshooting

### Can't login with test credentials?
- Make sure the application has restarted (DataInit runs on startup)
- Check that passwords are hashed (they should start with `$2a$` in database)
- Verify user exists: `SELECT * FROM users WHERE email = 'admin@test.com';`

### H2 Console not accessible?
- Ensure `spring.h2.console.enabled=true` in application.properties
- Check that app is running on port 8080
- Try: `http://localhost:8080/h2-console`

### Data not appearing?
- Check application logs for errors
- Verify DataInit ran (check logs for "PostConstruct")
- Query database directly via H2 console

