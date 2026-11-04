# Production Incident Management Platform

A backend application built with Java and Spring Boot to manage production incidents, track service health, enforce role-based access control, and maintain an audit history of incident changes.

## Features

- Create and retrieve incidents.
- Filter incidents by status, severity, service, and assignee.
- Paginate incident results.
- Validate incident status transitions.
- Assign incidents to users.
- Maintain an audit trail of incident changes.
- Check service health and support scheduled monitoring.
- Secure endpoints using Spring Security and role-based authorization.
- Validate API requests and handle exceptions centrally.
- Prevent multiple OPEN incidents for the same service.
- Run the application and MySQL using Docker Compose.
- Test REST APIs using JUnit and MockMvc.
- Explore and test endpoints through Swagger UI.
- Automatically run tests through GitHub Actions.

## Tech Stack

- Java 25
- Spring Boot
- Spring Data JPA and Hibernate
- Spring Security and HTTP Basic Authentication
- MySQL 8.4
- Maven
- JUnit and MockMvc
- Docker and Docker Compose
- OpenAPI and Swagger UI
- GitHub Actions

## Architecture

The application follows a layered backend architecture:

- **Controller layer:** Handles HTTP requests and responses.
- **Service layer:** Implements incident-management business rules.
- **Repository layer:** Uses Spring Data JPA to access MySQL.
- **Security layer:** Handles authentication and role-based authorization.
- **Audit logging:** Records important incident operations.
- **Database:** Stores incidents, services, users, and audit history.

## Prerequisites

- Docker Desktop with Docker Compose
- Git

For running tests directly on your Mac, use a compatible Java version. This project is configured for Java 25.

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/madhav-s-web/incident-management.git
cd incident-management
```

### 2. Configure environment variables

Create your local environment file:

```bash
cp .env.example .env
```

Edit `.env` and replace the example values with strong, unique passwords for `DB_PASSWORD` and `APP_DB_PASSWORD`.

Never commit `.env` or real credentials to GitHub.

### 3. Start the application

```bash
docker compose up --build -d
```

Check the containers:

```bash
docker compose ps
```

Check application health:

```bash
curl http://localhost:8080/actuator/health
```

The API base path is:

`http://localhost:8080/api/v1`

**Important:** MySQL initialization variables create accounts when the database volume is initialized for the first time. Changing `.env` later does not automatically change passwords in an existing database. Do not delete the database volume to troubleshoot credentials.

## API Documentation

The project uses OpenAPI and Swagger UI to provide interactive API documentation.

After starting the application, open:

**http://localhost:8080/swagger-ui/index.html**

Swagger UI lets you inspect API endpoints, view request and response formats, and execute API requests from your browser.

Protected endpoints require authentication. Use the **Authorize** button with a registered application account. Authorization rules still apply, so ADMIN-only operations require an ADMIN user.

## API Overview

Base path: `/api/v1`

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/auth/register` | Register a user |
| POST | `/incidents` | Create an incident |
| GET | `/incidents` | Retrieve incidents |
| GET | `/incidents/page?page=0&size=10` | Paginated incidents |
| GET | `/incidents/status/OPEN` | Filter incidents by status |
| PATCH | `/incidents/{id}/status` | Update incident status |
| PATCH | `/incidents/{incidentId}/assign/{userId}` | Assign an incident |
| GET | `/incidents/{id}/audit` | View incident audit history |
| POST | `/services` | Create a service |
| GET | `/services/{id}` | Retrieve a service |
| POST | `/services/{id}/check` | Check service health |

Most endpoints require authentication. Authorization depends on the user's role.

## Testing

The project includes controller tests for API behavior, validation, access control, pagination, incident assignment, and prevention of duplicate OPEN incidents.

Run the test suite locally after configuring the required test database and environment variables:

```bash
./mvnw clean test
```

## Continuous Integration

GitHub Actions automatically runs the Maven test suite on pushes to `main` and pull requests targeting `main`.

View workflow runs and test results here:

https://github.com/madhav-s-web/incident-management/actions

## Environment Variables

| Variable | Purpose |
|---|---|
| `DB_PASSWORD` | MySQL root password |
| `APP_DB_PASSWORD` | Application database user's password |
| `DB_URL` | JDBC connection URL |
| `DB_USERNAME` | Database username |

Keep `.env` and other files containing real credentials out of version control. `.env.example` should contain placeholders only.

## Future Improvements

- Deploy the application to a cloud environment.
- Add production monitoring, metrics, and alerting.
- Add integration tests and stronger automated security checks.
- Configure HTTPS and production-grade credential management.
