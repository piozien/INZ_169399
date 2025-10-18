# Testing Guide

This guide explains how to run tests for the Student Union Management System.

## 🚀 Quick Start

### Run All Tests
```bash
# Start test environment
docker-compose -f docker-compose-test.yml up -d

# Run tests
docker-compose -f docker-compose-test.yml exec backend-test ./mvnw test

# Stop test environment
docker-compose -f docker-compose-test.yml down
```

### Run Tests Locally
```bash
# Start MailHog for email testing
docker-compose -f docker-compose-test.yml up mailhog -d

# Run tests with test profile
cd su_backend
./mvnw test -Dspring.profiles.active=test

# Stop MailHog
docker-compose -f docker-compose-test.yml down mailhog
```

## 🧪 Test Types

### Unit Tests
```bash
# Run unit tests only
./mvnw test -Dtest="*ServiceTest"

# Run specific test class
./mvnw test -Dtest=UserServiceTest

# Run tests with coverage
./mvnw test jacoco:report
```

### Integration Tests
```bash
# Run integration tests
./mvnw test -Dtest="*IntegrationTest"

# Run all tests
./mvnw test
```

### Email Tests
```bash
# Start MailHog
docker-compose -f docker-compose-test.yml up mailhog -d

# Run email tests
./mvnw test -Dtest="*EmailTest"

# Check emails in MailHog UI
# http://localhost:8025
```

## 🔧 Test Configuration

### Test Profile (`application-test.yml`)
- **Database:** H2 in-memory
- **Email:** MailHog (localhost:1025)
- **Logging:** WARN level
- **Scheduling:** Disabled
- **Flyway:** Disabled

### Test Environment Variables
```bash
export SPRING_PROFILES_ACTIVE=test
export SPRING_DATASOURCE_URL=jdbc:h2:mem:testdb
export SPRING_MAIL_HOST=localhost
export SPRING_MAIL_PORT=1025
```

## 📊 Test Services

### MailHog (Email Testing)
- **URL:** http://localhost:8025
- **SMTP:** localhost:1025
- **Purpose:** Test email functionality

### H2 Database
- **Console:** http://localhost:8082
- **JDBC URL:** `jdbc:h2:mem:testdb`
- **Purpose:** In-memory database for tests

## 🏗️ Project Structure

```
su_backend/
├── src/
│   ├── main/java/pl/su/su_backend/
│   │   ├── controller/          # REST Controllers
│   │   │   ├── classCon/       # Class management
│   │   │   ├── user/           # User management
│   │   │   └── event/          # Event management
│   │   ├── service/            # Business Logic
│   │   │   ├── budget/         # Budget services
│   │   │   ├── auth/           # Authentication
│   │   │   └── user/           # User services
│   │   ├── model/              # JPA Entities
│   │   │   ├── budget/         # Budget models
│   │   │   ├── users/          # User models
│   │   │   └── enums/          # Enumerations
│   │   ├── dto/                # Data Transfer Objects
│   │   │   ├── budget/         # Budget DTOs
│   │   │   └── user/           # User DTOs
│   │   ├── repositories/       # JPA Repositories
│   │   └── exception/          # Custom exceptions
│   └── test/java/pl/su/su_backend/
│       ├── controller/         # Controller tests
│       ├── service/            # Service tests
│       └── integration/        # Integration tests
└── src/main/resources/
    ├── application.yml         # Production config
    ├── application-test.yml    # Test config
    └── db/migration/           # Database migrations
```

## 📦 Key Packages

### Controllers (`pl.su.su_backend.controller`)
- **ClassController** - Class management (CRUD)
- **UserController** - User management
- **EventController** - Event management
- **ClassBudgetController** - Budget management

### Services (`pl.su.su_backend.service`)
- **UserService** - User business logic
- **ClassBudgetService** - Budget calculations
- **PermissionService** - Access control
- **EmailService** - Email notifications

### Models (`pl.su.su_backend.model`)
- **Users** - User entity with roles
- **ClassBudget** - Budget with transactions
- **Classes** - Class information
- **Events** - Event management

### DTOs (`pl.su.su_backend.dto`)
- **RequestDto** - Input validation
- **ResponseDto** - Output formatting
- **Mappers** - Entity ↔ DTO conversion



## 🔍 Debugging Tests

### Run with Debug Mode
```bash
# Start with debug port
docker-compose -f docker-compose-test.yml up -d
docker-compose -f docker-compose-test.yml exec backend-test ./mvnw test -Dmaven.surefire.debug

# Connect debugger to localhost:5005
```

### View Test Logs
```bash
# View test logs
docker-compose -f docker-compose-test.yml logs -f backend-test

# View MailHog logs
docker-compose -f docker-compose-test.yml logs -f mailhog
```

## 📈 Test Coverage

### Generate Coverage Report
```bash
./mvnw test jacoco:report

# View report
open su_backend/target/site/jacoco/index.html
```
