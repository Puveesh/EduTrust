# Project EduTrust (Sentinel-Ledger)

> **Smart India Hackathon (SIH) Prototype**  
> **Problem Statement:** "Development of an APAAR ID-Based Student Data Management and Verification Platform for AICTE-Approved Institutions."

Project EduTrust is a complete full-stack institutional data management and verification platform designed for AICTE-approved colleges and universities. It allows institutions to manage student records, validate submitted enrollment datasets, detect possible cross-institutional duplicate records, monitor approved intake quotas, review flagged cases in a dedicated Review Center, maintain immutable audit event trails, and issue/verify digitally signed academic credentials with minimal privacy disclosure.

---

## 1. Architecture Overview

- **Frontend:** React (JSX) + Vite + Tailwind CSS + React Router + Lucide Icons + Recharts
- **Backend:** Java 21 / Spring Boot 3.3.5 (Spring Web, Spring Security, Spring Data JPA, Flyway, JJWT, Commons CSV, JCA Cryptography)
- **Database:** MySQL 8.0 (`sentinel_ledger`) with Flyway V1 schema migrations and automated development data seeding
- **Security:** Stateless JWT authentication, role-based access control (`AICTE_ADMIN`, `COLLEGE_REGISTRAR`, `VERIFIER`), institution-scoping with IDOR protection
- **Cryptography:** RSA-2048 keypair (`SHA256withRSA`) digital signature generation and verification for tamper-evident credentials
- **API Documentation:** Springdoc OpenAPI 3.0 / Swagger UI live at `/swagger-ui.html`

---

## 2. Directory Structure

```text
EduTrust/
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── assets/
│   │   ├── components/
│   │   │   ├── layout/       (Sidebar, Topbar, Layout)
│   │   │   ├── ui/           (Badge, Button, Card, Modal, Input)
│   │   │   ├── tables/       (DataTable)
│   │   │   ├── feedback/     (Toast, LoadingSpinner, EmptyState)
│   │   ├── pages/
│   │   │   ├── Login/
│   │   │   ├── Dashboard/
│   │   │   ├── Students/
│   │   │   ├── Imports/
│   │   │   ├── Reviews/
│   │   │   ├── Institutions/
│   │   │   ├── Compliance/
│   │   │   ├── Verification/
│   │   │   ├── AuditLogs/
│   │   │   └── Settings/
│   │   ├── services/         (api, auth, student, import, review, etc.)
│   │   ├── context/          (AuthContext, ToastContext)
│   │   ├── routes/           (AppRoutes, ProtectedRoute)
│   │   ├── App.jsx
│   │   ├── main.jsx
│   │   └── index.css
│   ├── .env.example
│   ├── package.json
│   ├── vite.config.js
│   └── README.md
│
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/sentinel/ledger/
│   │   │   │   ├── SentinelLedgerApplication.java
│   │   │   │   ├── config/       (OpenApiConfig)
│   │   │   │   ├── security/     (SecurityConfig, JwtTokenProvider, InstitutionSecurityEvaluator)
│   │   │   │   ├── controller/   (Auth, Student, Import, Review, Compliance, Credential, Audit)
│   │   │   │   ├── service/      (Student, Import, DuplicateDetection, Review, Crypto, Seed)
│   │   │   │   ├── repository/   (Student, User, Institution, ImportBatch, ReviewCase, AuditLog)
│   │   │   │   ├── entity/       (Student, User, Institution, ImportBatch, ReviewCase, Credential)
│   │   │   │   ├── dto/          (Requests, Responses, Summaries)
│   │   │   │   ├── exception/    (GlobalExceptionHandler, ErrorResponse, CustomExceptions)
│   │   │   │   ├── enums/        (Role, AdmissionCategory, CaseStatus, Severity, etc.)
│   │   │   │   └── audit/        (AuditService)
│   │   │   └── resources/
│   │   │       ├── application.yml
│   │   │       └── db/migration/V1__init_schema.sql
│   │   └── test/
│   ├── uploads/
│   │   └── .gitkeep
│   ├── .env.example
│   ├── pom.xml
│   └── README.md
│
├── .gitignore
└── README.md
```

---

## 3. Implemented Features

1. **Enterprise Dashboard:**
   - Real-time DB aggregated counts (Students, Institutions, Open Duplicates, Intake Violations)
   - Visual distribution charts (Students by Institution, Program Intake breakdown, Verification Status)
   - Recent Review Center activity & recent immutable audit events
2. **Student Management Module:**
   - Full CRUD operations with institutional boundaries
   - Search & filtering by course, academic year, category, and status
   - Revision history tracking (`StudentHistory`) storing before & after JSON diffs with user attribution
3. **Data Import Module (CSV Staging Wizard):**
   - Download sample CSV template
   - Safe Commons CSV parsing with error checks (untrusted data protection)
   - Interactive preview table showing valid rows, invalid rows, and cross-institution duplicates
   - Editable cells to fix invalid data directly in the staging area
   - Row exclusion toggles
   - Atomic commit with revalidation & duplicate rechecks; prevents duplicate commits
4. **Demonstrable Duplicate Candidate Detection:**
   - Normalizes synthetic student references
   - Flags cross-institution duplicate candidates across institutions
   - Automatically generates high-severity `ReviewCase` without blindly deleting or penalizing records
5. **Review Center:**
   - Filter cases by duplicate candidates, intake violations, or invalid data
   - Inspect authorized evidence and cross-institution match data
   - Mandatory resolution reason recorded in audit log with user signature
6. **Intake Compliance Monitoring:**
   - Compares admitted student totals against configured AICTE approved limits per program
   - Highlights excess intake warnings as reviewable alerts rather than automatic penalties
7. **Tamper-Resistant Credential Verification:**
   - Generates minimal canonical claim sets signed with RSA-2048 (`SHA256withRSA`)
   - Verifies digital signatures and registry validity status
   - Enforces minimal disclosure (masks personal names, reveals only qualification, year, and issuer)
   - Built-in "Simulate Tamper" mode to prove that changing a single character causes cryptographic verification failure
8. **Institutional Registry Management:**
   - AICTE Admin view of institutions, intake quotas, and campus locations
9. **Audit Event Trail:**
   - Tracks authentication, record creations, updates, import commits, and reviews with IP address and correlation ID

---

## 4. Demo Credentials & Roles

| Role | Username | Password | Assigned Scope | Description |
| :--- | :--- | :--- | :--- | :--- |
| **AICTE Admin** | `admin` | `Admin@123` | National AICTE Grid | Full cross-institutional authority, global reviews, intake management |
| **College Registrar** | `registrar_delhi` | `Registrar@123` | Delhi Institute of Adv Tech | Institutional student records, CSV imports, local review cases |
| **College Registrar** | `registrar_maha` | `Registrar@123` | Maharashtra Inst of Tech | Institutional student records, CSV imports, local review cases |
| **Verifier** | `verifier_corp` | `Verifier@123` | Verifier Suite | Authorized credential verification with minimal privacy disclosure |

*Note: Demo buttons on the login screen allow one-click credential autofill.*

---

## 5. Prerequisites & Local Setup

### Prerequisites
- Java 21 LTS or Java 25 LTS
- Apache Maven 3.9+
- Node.js v18+ and npm
- MySQL 8.0 Server running on port 3306

### Database Setup (PowerShell)
```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -proot -e "CREATE DATABASE IF NOT EXISTS sentinel_ledger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### Backend Startup (PowerShell)
```powershell
cd backend
mvn clean test
mvn spring-boot:run
```
*Backend runs on `http://localhost:8080`.*  
*Swagger UI docs available at `http://localhost:8080/swagger-ui.html`.*

### Frontend Startup (PowerShell)
```powershell
cd frontend
npm install
npm run dev
```
*Frontend runs on `http://localhost:5173`.*  
*Production build: `npm run build`.*

---

## 6. Testing & Acceptance Scenarios

All 20 required acceptance criteria were executed and validated:

| Test ID | Test Scenario | Execution Result |
| :--- | :--- | :--- |
| **TEST 1** | Login as registrar | **PASS** (Token received, InstitutionId scoped) |
| **TEST 2** | Create a student record | **PASS** (Created ID #88: Kavita Krishnan) |
| **TEST 3** | Verify student record in MySQL | **PASS** (Queried directly from `sentinel_ledger.students`) |
| **TEST 4** | Upload CSV with valid records | **PASS** (Parsed and staged) |
| **TEST 5** | Upload CSV with missing fields | **PASS** (Flagged as INVALID with JSON error messages) |
| **TEST 6** | Detect duplicate candidate across institutes | **PASS** (Matched `APAAR-SYN-2024-9999` across Delhi & Maharashtra) |
| **TEST 7** | Correct invalid row in staging | **PASS** (Row #2 updated & revalidated to VALID) |
| **TEST 8** | Commit import batch | **PASS** (Transactionally committed into permanent records) |
| **TEST 9** | Repeat commit prevention | **PASS** (Rejected with HTTP 400 "Batch already committed") |
| **TEST 10** | Review & resolve duplicate case with reason | **PASS** (Status transitioned to CONFIRMED_DUPLICATE with mandatory reason) |
| **TEST 11** | Verify dashboard statistics update from DB | **PASS** (Total students: 91, Total batches: 1) |
| **TEST 12** | IDOR protection test | **PASS** (Registrar accessing other institute record blocked with HTTP 403) |
| **TEST 13** | Verify role restrictions | **PASS** (Verifier attempting student creation blocked with HTTP 403) |
| **TEST 14** | Inspect audit history | **PASS** (Action `REVIEW_CASE_RESOLVED` verified with correlation ID) |
| **TEST 15** | Check intake limit violations | **PASS** (12 program allocations evaluated against approved capacity) |
| **TEST 16** | Verify genuine signed credential | **PASS** (RSA signature verified: `True`, Valid: `True`) |
| **TEST 17** | Tamper detection test | **PASS** (Tampered qualification detected; SignatureValid: `False`) |
| **TEST 18** | Minimal disclosure verification | **PASS** (Returned masked name `N*** I***` and synthetic reference only) |
| **TEST 19** | Backend unit tests | **PASS** (`Tests run: 4, Failures: 0, Errors: 0`) |
| **TEST 20** | Frontend production build | **PASS** (`vite build` succeeded with zero errors) |

---

## 7. Known Limitations & Roadmap

1. **Private Set Intersection (PSI):** Initial MVP uses deterministic synthetic reference comparison; production roadmap includes zero-knowledge PSI protocol integration.
2. **Key Storage:** Development prototype stores RSA keypair in memory/application config; production deployments should integrate a Hardware Security Module (HSM) or cloud KMS.
3. **W3C Verifiable Credentials:** Digital signature uses standard SHA256withRSA JSON canonical envelopes; future phases will map claims to full W3C DID / VC JSON-LD schemas.
