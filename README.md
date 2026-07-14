# Employee Data Service

A Spring Boot REST API for managing employee records. The service focuses on secure handling of sensitive personal data (SSN), clean layering, and well-reasoned, test-covered implementation.

## Tech Stack
- Java 21
- Spring Boot 4.1 (Spring Web MVC, Spring Data JPA, Bean Validation)
- Docker / Docker Compose
- Lombok
- PostgreSQL 16
- Hibernate
- JUnit 5 + Mockito (unit tests), MockMvc (controller tests)

## Running the project with Docker Compose

### Prerequisites

- Docker Desktop installed and running
- A long random value for `SSN_HASH_SECRET`

You can generate a secure secret with:
```bash
openssl rand -base64 32
```
### Steps

1. Clone the repository:
```
  git clone https://github.com/awaszeciak/employee-data-service.git
  cd employee-data-service
```
2. Copy the example environment file:
```
  cp .env.example .env
```
3. Generate a secret key for SSN hashing and paste it into `.env` as `SSN_HASH_SECRET`:
```
  openssl rand -base64 32
```
The app deliberately has **no default value** for this secret - it will fail to start without it.

4. Build and start everything (database + application):
```
  docker-compose up --build
```

5. The API is available at `http://localhost:8081`.

### Example requests

**Create an employee** - `POST http://localhost:8081/employees`

Request body:
```json
{
  "firstName": "Jan",
  "lastName": "Kowalski",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "socialSecurityNumber": "123456789"
}
```

Response (`201 Created`):
```json
{
  "id": 1,
  "firstName": "Jan",
  "lastName": "Kowalski",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE"
}
```

**Get an employee by ID** - `GET http://localhost:8081/employees/1`

Response (`200 OK`):
```json
{
  "id": 1,
  "firstName": "Jan",
  "lastName": "Kowalski",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE"
}
```

**List all employees** - `GET http://localhost:8081/employees`

Response (`200 OK`):
```json
[
  {
    "id": 1,
    "firstName": "Jan",
    "lastName": "Kowalski",
    "dateOfBirth": "1990-05-15",
    "gender": "MALE"
  }
]
```

## API Endpoints

| Method | Path              | Description                     |
|--------|-------------------|----------------------------------|
| POST   | `/employees`      | Create a new employee record    |
| GET    | `/employees/{id}` | Retrieve a single employee      |
| GET    | `/employees`      | List all employees              |

## Technology choices
- **Spring Boot** was chosen because it provides built-in support for REST APIs, validation, dependency injection, exception handling, and integration with Spring Data JPA.
- **PostgreSQL** was chosen because employee records are structured relational data. It also provides reliable database constraints and uniqueness enforcement.
- **Docker Compose** allows the application and PostgreSQL database to be started in a consistent local environment.
- **HMAC-SHA256** was chosen because the application does not need to recover the original SSN, but it still needs to detect duplicate values.

## SSN security
The Social Security Number is never stored in plaintext and is never returned by the API.

Before an employee is persisted, the SSN is processed using HMAC-SHA256 with a secret provided through the `SSN_HASH_SECRET` environment variable.

Hashing was chosen instead of encryption because the application does not need to recover or display the original SSN. The deterministic HMAC output still allows duplicate SSNs to be detected.

HMAC-SHA256 was chosen instead of plain SHA-256 because SSNs have a limited and predictable input space. Using a secret key makes precomputed lookup attacks significantly more difficult.

The application has no default value for `SSN_HASH_SECRET` and will fail to start if the secret is missing.

## Validation and error handling

The API validates:
- first name and last name are required,
- date of birth must be in the past,
- gender is required,
- SSN must contain exactly nine digits.

The API returns appropriate HTTP status codes:

| Status | Description |
|--------|-------------|
| `201 Created` | Employee was successfully created |
| `200 OK` | Employee data was successfully retrieved |
| `400 Bad Request` | Request validation failed |
| `404 Not Found` | Employee with the requested ID does not exist |
| `409 Conflict` | An employee with the same SSN already exists |
| `500 Internal Server Error` | An unexpected application error occurred |


Example validation error:

```json
{
  "status": 400,
  "message": "Validation failed",
  "details": [
    "socialSecurityNumber: Social security number must contain 9 digits"
  ]
}
```

## Testing

The project includes:
- unit tests for the service layer,
- tests for HMAC generation,
- tests verifying that the SSN is not stored in plaintext,
- controller tests using MockMvc,
- request validation tests,
- tests for `404 Not Found` responses,
- tests for `409 Conflict` responses,
- tests verifying that neither the SSN nor its hash is returned by the API.

Run the tests on Windows:
```powershell
.\mvnw.cmd test
```

Run the tests on macOS or Linux:
```bash
./mvnw test
```

## AI-assisted development

I used ChatGPT and Claude as an AI-assisted development tool during the implementation.

I used them to:

- discuss the tradeoffs between encryption, plain hashing, and HMAC for storing the SSN,
- get a second opinion on the layered project structure (controller/service/repository/mapper),
- troubleshoot Docker platform issues (`exec format error` on Windows).

One example of a suggestion I rejected was using BCrypt for SSN storage.

BCrypt is appropriate for passwords because it uses a random salt and is intentionally slow. However, different BCrypt hashes are produced for the same input, which would make efficient database-level duplicate detection impossible.

Since the service needs to detect duplicate SSNs without recovering the original value, I chose deterministic HMAC-SHA256 with an external secret instead.

## What I'd do differently with more time

- Add pagination to `GET /employees`.
- Implement a key-rotation strategy for the SSN hash secret - currently it must stay constant for the database's lifetime, since changing it makes old hashes unmatchable.
- Add OpenAPI/Swagger documentation.

