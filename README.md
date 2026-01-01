# System Zarządzania Samorządem Uczniowskim (Student Council Management System)

[English version below](#student-council-management-system)

## O projekcie

Aplikacja wspierająca działalność Samorządu Uczniowskiego poprzez cyfryzację kluczowych procesów, takich jak:
- Zarządzanie budżetem i finansami
- Organizacja wydarzeń
- Obsługa sugestii uczniowskich
- Zarządzanie strukturą i członkami samorządu

Projekt składa się z backendu opartego na **Spring Boot** (Java 21) oraz frontendu w **Next.js** (React).

## Wymagania

- **Docker** oraz **Docker Compose** (zainstalowane i skonfigurowane w systemie)

## Uruchomienie aplikacji (Środowisko Deweloperskie)

1. **Pobranie kodu źródłowego**
   Sklonuj repozytorium lub pobierz i rozpakuj archiwum ZIP.

2. **Uruchomienie kontenerów**
   W terminalu, będąc w głównym katalogu projektu, wykonaj polecenie:
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```
   Polecenie to zbuduje i uruchomi kontenery dla bazy danych, backendu oraz frontendu.

3. **Dostęp do aplikacji**
   - Frontend: [http://localhost:3000](http://localhost:3000)
   - Backend API: [http://localhost:8080](http://localhost:8080)
   - Dokumentacja API (Swagger): [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

4. **Zatrzymanie aplikacji**
   ```bash
   docker-compose -f docker-compose.dev.yml down
   ```

## Testowanie

Aby uruchomić testy integracyjne i jednostkowe w odizolowanym środowisku Docker, użyj polecenia:

```bash
docker-compose -f docker-compose-test.yml up --abort-on-container-exit --exit-code-from test_runner
```

---

# Student Council Management System

## About the Project

An application designed to support Student Council activities by digitizing key processes such as:
- Budget and finance management
- Event organization
- Student suggestions handling
- Council structure and member management

The project consists of a **Spring Boot** (Java 21) backend and a **Next.js** (React) frontend.

## Prerequisites

- **Docker** and **Docker Compose** (installed and configured)

## Running the Application (Development Environment)

1. **Get the Source Code**
   Clone the repository or download and extract the ZIP archive.

2. **Start Containers**
   In the terminal, navigate to the project root directory and run:
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```
   This command will build and start containers for the database, backend, and frontend.

3. **Access the Application**
   - Frontend: [http://localhost:3000](http://localhost:3000)
   - Backend API: [http://localhost:8080](http://localhost:8080)
   - API Documentation (Swagger): [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

4. **Stop the Application**
   ```bash
   docker-compose -f docker-compose.dev.yml down
   ```

## Testing

To run integration and unit tests in an isolated Docker environment, use the following command:

```bash
docker-compose -f docker-compose-test.yml up --abort-on-container-exit --exit-code-from test_runner
```
