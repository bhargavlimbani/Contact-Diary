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

Mail settings are also environment-variable based in production:

```properties
spring.mail.host=${MAIL_HOST:smtp.gmail.com}
spring.mail.port=${MAIL_PORT:587}
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
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

This project is ready to deploy on Render with Docker and an external Aiven MySQL database.

### 1. Create MySQL on Aiven

1. Create an **Aiven for MySQL** service.
2. In Aiven Console, open your MySQL service and use **Quick connect**.
3. Choose **Java** and copy the JDBC connection string, or collect:
   - host
   - port
   - database name
   - username
   - password
4. Create a database named `contact_diary` if you do not want to use `defaultdb`.

Example Aiven JDBC URL:

```text
jdbc:mysql://HOST:PORT/contact_diary?sslmode=require
```

### 2. Push This Project to GitHub

Render deploys from a Git repository. Push this project to GitHub, GitLab, or Bitbucket.

### 3. Create a Render Web Service

Render currently deploys Java/Spring Boot apps like this one with the **Docker** runtime.

1. In Render, click **New +** -> **Web Service**.
2. Connect your repository.
3. Render should detect the included `render.yaml`, or you can configure manually:
   - Runtime: `Docker`
   - Instance type: `Free` or higher
4. Deploy the service.

### 4. Set Environment Variables on Render

Set these variables in the Render dashboard:

- `DB_URL`: JDBC URL. For Aiven MySQL (SSL required), example:
  - `jdbc:mysql://<HOST>:<PORT>/<DB_NAME>?sslmode=require`
- `DB_USERNAME`
- `DB_PASSWORD`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_SMTP_AUTH`
- `MAIL_SMTP_STARTTLS_ENABLE`

Notes:

- Render provides `PORT` automatically for web services.
- Aiven documentation shows Java/MySQL connections using SSL in the JDBC URL.
- If you do not want email features yet, leave the mail variables empty, but registration OTP and password reset emails will not send successfully.

### 5. Open the Live App

After the deploy finishes, open:

```text
https://YOUR-RENDER-SERVICE.onrender.com/app.html
```

### Important Security Note

Do not keep real Gmail credentials in source code. This project now reads mail and database secrets from environment variables instead.

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
