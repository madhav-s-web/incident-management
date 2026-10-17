# Production Incident Management Platform

A backend application built with Java and Spring Boot to manage production incidents, track service health, control access by user roles, and maintain an audit history of incident changes.

## Features

- Create and retrieve incidents.
- Filter incidents by status, severity, service, and assignee.
- Paginate incident results.
- Update incident status with transition validation.
- Assign incidents to users.
- Maintain an audit history of incident changes.
- Monitor service health through scheduled checks.
- Enforce authentication and role-based authorization.
- Validate requests and handle errors centrally.
- Prevent multiple open incidents for the same service.

## Tech Stack

- Java
- Spring Boot
- Spring Data JPA
- Spring Security
- MySQL
- Maven
- JUnit and MockMvc

## Prerequisites

- A compatible JDK
- MySQL
- Git

## Local Setup

1. Clone this repository.
2. Create a MySQL database named `incident_management`.
3. Configure the database connection using environment variables.
4. Start the Spring Boot application.

Required environment variables:

- `DB_URL` — JDBC URL for the database
- `DB_USERNAME` — database username
- `DB_PASSWORD` — database password

## Run Tests

```bash
./mvnw clean test
```

## API

The API base path is `/api/v1`. Available endpoints cover authentication, incidents, and services. Refer to the controllers for the current routes and request formats.

## Future Improvements

- Docker-based deployment
- API documentation with OpenAPI/Swagger
- Continuous integration using GitHub Actions
