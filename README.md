# fit-ease-pwr — Web App (Spring Boot + Thymeleaf + Supabase PostgreSQL)

## What is this?
- **Spring Boot + Thymeleaf** = Java server-rendered web app → http://localhost:8080  
- **Supabase PostgreSQL** = cloud database (no Docker needed!)  
- **Maven Wrapper (`mvnw`)** = run without installing Maven

---

## Requirements (install once)
- Git
- Java 17+ (`java -version`)

---

## Quickstart

### 1. Clone
```bash
git clone https://github.com/EfranFenris/fit-ease-pwr.git
cd fit-ease-pwr
```

### 2. Configure Database
The database connection is already configured in [`application.properties`](src/main/resources/application.properties):

```properties
spring.datasource.url=jdbc:postgresql://aws-1-eu-west-1.pooler.supabase.com:5432/postgres?user=postgres.wphyygzuylpqigxixqqs&password=fit-ease-pwr
spring.jpa.hibernate.ddl-auto=update
```

✅ **Database schema is automatically managed by Supabase**

### 3. Run Backend

```bash
# Mac/Linux
chmod +x mvnw
./mvnw spring-boot:run

# Windows (PowerShell)
.\mvnw.cmd spring-boot:run
```

### 4. Check
Open: http://localhost:8080 → shows a public landing page if you are not logged in.

To access the app, log in at http://localhost:8080/login and then visit
http://localhost:8080/facilities.

**Default test accounts (created by `DataInitializer`):**
- Admin: `admin@test.com` / password hash in code
- User: `user@test.com` / password hash in code
- Maintenance: `manteinance@test.com` / password hash in code
- User (Marta): `marta@test.com` / password hash in code

You can also create a new account at http://localhost:8080/signup.

---

## Project Structure
```
src/
├── main/
│   ├── java/start/spring/io/backend/
│   │   ├── controller/     # REST endpoints
│   │   ├── model/          # JPA entities (User, Facility, etc.)
│   │   ├── repository/     # Database access
│   │   ├── service/        # Business logic
│   │   └── security/       # Authentication config
│   └── resources/
│       ├── static/         # CSS, JS, images
│       ├── templates/      # Thymeleaf HTML views
│       └── application.properties  # Database config
```

---

## Key Features
- ✅ User authentication (login/signup)
- ✅ Facility management
- ✅ Reservation system
- ✅ Maintenance request tracking with dashboard
- ✅ Role-based access (admin/user/maintenance)

---

## Database Tables (Managed by Supabase)
- `users` - User accounts with roles
- `facility` - Sports facilities (tennis, padel, etc.)
- `reservation` - Booking records
- `maintenance_request` - Issue tracking
- `penalty` - User penalties

All tables are automatically created/updated via JPA (`spring.jpa.hibernate.ddl-auto=update`)

---

## Common Issues

### Port 8080 already in use
```bash
# Find process using port 8080
# Mac/Linux
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### Database connection error
- Check your internet connection (Supabase is cloud-based)
- Verify credentials in [`application.properties`](src/main/resources/application.properties)

---

## Development Notes
- **No Docker needed** - Database is hosted on Supabase
- **Auto-schema updates** - JPA handles table creation/updates
- **Sample data** - Added automatically by [`DataInitializer`](src/main/java/start/spring/io/backend/config/DataInitializer.java)

---

## Deployment
When deploying to production:
1. Change `spring.jpa.hibernate.ddl-auto` to `validate`
2. Use environment variables for database credentials
3. Enable HTTPS for Supabase connection
