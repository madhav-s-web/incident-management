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

## Tech Stack

- Java 25
- Spring Boot
- Spring Data JPA and Hibernate
- Spring Security
- MySQL 8.4
- Maven
- JUnit and MockMvc
- Docker and Docker Compose

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

For running tests directly on your Mac, Java 25 is required.

## Setup

Clone the repository:

```bash
git clone https://github.com/madhav-s-web/incident-management.git
cd incident-management
```

Create your local environment file:

```bash
cp .env.example .env
```

Edit `.env` and configure strong, unique values for `DB_PASSWORD` and `APP_DB_PASSWORD`.

Start the application:

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

The API is available at `http://localhost:8080/api/v1`.

**Important:** MySQL initialization variables create accounts when the database volume is initialized for the first time. Changing `.env` later does not automatically change passwords in an existing database.

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

The project includes controller tests for API behavior, validation, access control, pagination, incident assignment, and duplicate OPEN-incident prevention.

Run the test suite after configuring the required test database and environment variables:

```bash
./mvnw clean test
```

## Environment Variables

| Variable | Purpose |
|---|---|
| `DB_PASSWORD` | MySQL root password |
| `APP_DB_PASSWORD` | Application database user's password |
| `DB_URL` | JDBC connection URL |
| `DB_USERNAME` | Database username |

Never commit `.env` or real credentials to GitHub.

## Future Improvements

- Automated testing with GitHub Actions.
- API documentation with OpenAPI/Swagger.
- Deployment to a cloud environment.
- Monitoring and operational metrics.
