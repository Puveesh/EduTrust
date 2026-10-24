# Project EduTrust (Sentinel-Ledger) - Backend

Spring Boot 3.3.5 / Java 21 REST API server for APAAR ID-based student data management and verification for AICTE-approved institutions.

## Prerequisites
- Java 21 or Java 25 LTS
- Apache Maven 3.9+
- MySQL 8.0 Server running on port 3306

## Quick Start (PowerShell)
```powershell
# Create MySQL Database
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -proot -e "CREATE DATABASE IF NOT EXISTS sentinel_ledger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Compile and run unit tests
mvn clean test

# Run Spring Boot backend server
mvn spring-boot:run
```

API Server runs at `http://localhost:8080`.
Swagger UI documentation available at `http://localhost:8080/swagger-ui.html`.
OpenAPI JSON available at `http://localhost:8080/v3/api-docs`.
