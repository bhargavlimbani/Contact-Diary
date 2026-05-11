# Contact Diary

Contact Diary is a Spring Boot web application for managing personal contacts. A user can register, login, add contacts, update contacts, delete contacts, search contacts by name, mark favorite contacts, share a contact with another registered user, export contacts as CSV, and print a contact report.

## Technology Stack

- Java 17
- Spring Boot
- Spring MVC
- Spring Data JPA / Hibernate ORM
- MySQL
- Bootstrap 5
- JUnit 5
- GitHub Actions

## Main Features

- Responsive login, registration, and contact dashboard
- Client-side and server-side validation
- Contact fields: name and phone number are required; email, address, and relation are optional
- CRUD operations for contacts
- Real-time search contacts by name
- Mark important contacts as favorite
- Share a contact with another registered user by email
- Export contacts to CSV
- Print contact report from the browser
- Password hashing using BCrypt

## MVC Structure

- Model: `User`, `Contact`
- View: `src/main/resources/static/app.html`
- Controller: `AuthController`, `ContactController`
- Service: `UserService`, `ContactService`
- Repository: `UserRepository`, `ContactRepository`

## Frontend Structure

```text
src/main/resources/static/
├── app.html
├── css/
│   └── app.css
└── js/
    └── app.js
```

- `app.html`: Page layout for login, register, add contact, filters, and contact list
- `css/app.css`: Custom page styling and print report styling
- `js/app.js`: Login/register logic, contact CRUD, search, favorite, WhatsApp share, export, and print actions

## Database Configuration

Create a MySQL database:

```sql
CREATE DATABASE contact_diary;
```

The application uses this configuration in `src/main/resources/application.properties`:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/contact_diary}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
server.port=${PORT:8083}
```

## Run Project

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

Open:

```text
http://localhost:8083/app.html
```

## Deploy (Live)

Set these environment variables on your hosting platform (Render/Railway/Fly/etc):

- `DB_URL`: JDBC URL. For Aiven MySQL (SSL required), example:
  - `jdbc:mysql://<HOST>:<PORT>/<DB_NAME>?sslMode=REQUIRED`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_SSL_MODE`: `REQUIRED` (or leave empty if included in `DB_URL`)
- `PORT`: provided by host (the app reads it automatically)

## Test Cases

Run tests:

```bash
./mvnw test
```

On Windows:

```bash
mvnw.cmd test
```

Current test coverage includes:

- User registration rejects duplicate email
- User login accepts valid hashed password
- Contact update preserves the existing owner
- Contact sharing copies a contact to another user
- Spring application context loads with H2 test database

## CI/CD

GitHub Actions workflow is available at `.github/workflows/ci.yml`.

Recommended branch structure for submission:

- `dev`: active development
- `test`: tested build
- `prod`: final production-ready branch

The workflow runs Maven tests on pushes and pull requests for `dev`, `test`, `prod`, `main`, and `master`.
