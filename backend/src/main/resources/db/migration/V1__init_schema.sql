-- Flyway V1: Initial Schema for Sentinel-Ledger (Project EduTrust)

CREATE TABLE IF NOT EXISTS institutions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    state VARCHAR(100) NOT NULL,
    city VARCHAR(100) NOT NULL,
    approval_status VARCHAR(50) NOT NULL DEFAULT 'APPROVED',
    configured_intake INT NOT NULL DEFAULT 120,
    contact_email VARCHAR(150),
    contact_phone VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    institution_id BIGINT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_institution FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS program_intakes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    institution_id BIGINT NOT NULL,
    program_name VARCHAR(150) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    approved_intake INT NOT NULL,
    category_quota_json TEXT,
    policy_version VARCHAR(50) DEFAULT 'AICTE_REG_2024_V1',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_program_institution FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE CASCADE,
    CONSTRAINT uk_inst_prog_year UNIQUE (institution_id, program_name, academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    institution_id BIGINT NOT NULL,
    synthetic_apaar_id VARCHAR(50) NOT NULL,
    enrollment_number VARCHAR(100) NOT NULL,
    student_name VARCHAR(200) NOT NULL,
    program VARCHAR(150) NOT NULL,
    academic_year VARCHAR(20) NOT NULL,
    admission_year INT NOT NULL,
    admission_category VARCHAR(50) NOT NULL,
    admission_status VARCHAR(50) NOT NULL DEFAULT 'ADMITTED',
    verification_status VARCHAR(50) NOT NULL DEFAULT 'VERIFIED',
    gender VARCHAR(20) DEFAULT 'UNSPECIFIED',
    dob DATE NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_students_institution FOREIGN KEY (institution_id) REFERENCES institutions(id),
    INDEX idx_student_apaar (synthetic_apaar_id),
    INDEX idx_student_institution (institution_id),
    INDEX idx_student_program_year (program, academic_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS student_histories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    modified_by VARCHAR(100) NOT NULL,
    change_type VARCHAR(50) NOT NULL,
    old_value_json TEXT,
    new_value_json TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    INDEX idx_history_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS import_batches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    institution_id BIGINT NOT NULL,
    uploaded_by VARCHAR(100) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    total_rows INT NOT NULL DEFAULT 0,
    valid_rows INT NOT NULL DEFAULT 0,
    invalid_rows INT NOT NULL DEFAULT 0,
    duplicate_candidates INT NOT NULL DEFAULT 0,
    excluded_rows INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'UPLOADED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    committed_at TIMESTAMP NULL,
    CONSTRAINT fk_batches_institution FOREIGN KEY (institution_id) REFERENCES institutions(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS import_rows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    batch_id BIGINT NOT NULL,
    row_num INT NOT NULL,
    raw_payload_json TEXT,
    synthetic_apaar_id VARCHAR(50),
    student_name VARCHAR(200),
    program VARCHAR(150),
    academic_year VARCHAR(20),
    admission_year INT,
    admission_category VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'VALID',
    validation_errors_json TEXT,
    duplicate_explanation TEXT,
    is_edited BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_rows_batch FOREIGN KEY (batch_id) REFERENCES import_batches(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS review_cases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_number VARCHAR(50) NOT NULL UNIQUE,
    case_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'OPEN',
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    institution_id BIGINT NULL,
    target_student_id BIGINT NULL,
    evidence_json TEXT,
    resolution_reason TEXT,
    resolved_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    CONSTRAINT fk_cases_institution FOREIGN KEY (institution_id) REFERENCES institutions(id) ON DELETE SET NULL,
    CONSTRAINT fk_cases_student FOREIGN KEY (target_student_id) REFERENCES students(id) ON DELETE SET NULL,
    INDEX idx_case_type_status (case_type, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    username VARCHAR(100) NOT NULL,
    institution_id BIGINT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    outcome VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    reason TEXT,
    ip_address VARCHAR(50),
    correlation_id VARCHAR(100),
    INDEX idx_audit_user (username),
    INDEX idx_audit_action (action),
    INDEX idx_audit_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    credential_id VARCHAR(100) NOT NULL UNIQUE,
    student_id BIGINT NULL,
    synthetic_subject_ref VARCHAR(50) NOT NULL,
    institution_id BIGINT NOT NULL,
    qualification VARCHAR(150) NOT NULL,
    completion_year INT NOT NULL,
    cgpa_or_grade VARCHAR(20),
    issuer VARCHAR(200) NOT NULL,
    issue_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'VALID',
    key_id VARCHAR(100) NOT NULL,
    algorithm VARCHAR(50) NOT NULL DEFAULT 'SHA256withRSA',
    signature TEXT NOT NULL,
    public_key_pem TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_cred_institution FOREIGN KEY (institution_id) REFERENCES institutions(id),
    CONSTRAINT fk_cred_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL,
    INDEX idx_cred_id (credential_id),
    INDEX idx_cred_subject (synthetic_subject_ref)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
