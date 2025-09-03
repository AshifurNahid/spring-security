# JWT Authentication API

A production-grade Spring Boot 3.5.3 REST API with JWT authentication, built with Java 21 and Spring Security 6.x.

## Features

- ✅ **JWT Authentication** with access & refresh tokens
- ✅ **Spring Security 6.x** with stateless configuration
- ✅ **Token Rotation** - refresh tokens are rotated on each refresh
- ✅ **BCrypt Password Encoding** (strength 12)
- ✅ **Input Validation** with Bean Validation
- ✅ **RFC 7807 Error Responses** with proper HTTP status codes
- ✅ **CORS Support** with configurable origins
- ✅ **Method-level Security** with `@PreAuthorize`
- ✅ **PostgreSQL Database** with JPA/Hibernate
- ✅ **MapStruct** for DTO mapping
- ✅ **OpenAPI/Swagger Documentation**
- ✅ **Scheduled Cleanup** of expired tokens
- ✅ **Production-ready Configuration**

## Tech Stack

- **Java 21**
- **Spring Boot 3.5.3**
- **Spring Security 6.x**
- **Spring Data JPA**
- **PostgreSQL**
- **JWT (jjwt 0.12.6)**
- **Lombok**
- **MapStruct**
- **Maven**

## Quick Start

### Prerequisites

- JDK 21+
- PostgreSQL 12+
- Maven 3.6+

### Database Setup

```sql
CREATE DATABASE jwt_auth_db;
CREATE USER jwt_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE jwt_auth_db TO jwt_user;
```

### Environment Variables

```bash
# Database
DB_USERNAME=jwt_user
DB_PASSWORD=your_password

# JWT (Generate a secure 256-bit key)
JWT_SECRET=your_jwt_secret_key_here

# CORS
CORS_ORIGINS=http://localhost:3000,http://localhost:4200
```

### Run the Application

```bash
./mvnw clean install
./mvnw spring-boot:run
```

## API Endpoints

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | No |
| POST | `/api/auth/login` | Login user | No |
| POST | `/api/auth/refresh` | Refresh access token | No |

### User Management

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users/me` | Get current user profile | Yes |

## Request/Response Examples

### Register User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "securePassword123"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "tokenType": "Bearer",
  "expiresIn": 900000
}
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "securePassword123"
  }'
```

### Get Current User

```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**Response:**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "role": "USER",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

### Refresh Token

```bash
curl -X POST http://localhost:8080/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

## Security Configuration

### JWT Token Settings

- **Access Token:** 15 minutes (900,000 ms)
- **Refresh Token:** 7 days (604,800,000 ms)
- **Clock Skew:** 5 minutes (300,000 ms)
- **Algorithm:** HS256
- **Token Rotation:** Enabled

### Password Security

- **Encoding:** BCrypt with strength 12
- **Minimum Length:** 8 characters
- **Maximum Length:** 128 characters

### CORS Configuration

Configure allowed origins in `application.properties`:

```properties
app.cors.allowed-origins=http://localhost:3000,http://localhost:4200
```

## Error Handling

The API returns RFC 7807 compliant error responses:

```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/auth/register",
  "timestamp": "2024-01-01T10:00:00.000Z",
  "errors": [
    {
      "field": "email",
      "message": "Email should be valid",
      "rejectedValue": "invalid-email"
    }
  ]
}
```

## Documentation

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI Docs:** http://localhost:8080/api-docs

## Configuration

Key configuration properties in `application.properties`:

```properties
# JWT Configuration
app.jwt.secret=${JWT_SECRET}
app.jwt.access-token-expiration=900000
app.jwt.refresh-token-expiration=604800000

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/jwt_auth_db
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:password}

# CORS
app.cors.allowed-origins=${CORS_ORIGINS:http://localhost:3000}
```

## Production Deployment

### Docker Compose Example

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_USERNAME=jwt_user
      - DB_PASSWORD=secure_password
      - JWT_SECRET=your_production_jwt_secret
    depends_on:
      - db
      
  db:
    image: postgres:15
    environment:
      - POSTGRES_DB=jwt_auth_db
      - POSTGRES_USER=jwt_user
      - POSTGRES_PASSWORD=secure_password
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### Security Recommendations

1. **Use strong JWT secrets** (256+ bit)
2. **Enable HTTPS** in production
3. **Configure proper CORS** origins
4. **Use connection pooling** for database
5. **Enable rate limiting**
6. **Monitor and log** security events
7. **Regularly rotate** JWT secrets
8. **Use environment variables** for secrets

## Testing

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## License

This project is licensed under the MIT License.
