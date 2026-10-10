-- ====================================================================
-- MedConnect Pro: Enterprise Telemedicine Platform Database Schema
-- Database: PostgreSQL 15+
-- Compliance: HIPAA & Medical Council Regulatory Standards
-- ====================================================================

-- 1. EXTENSIONS
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. ENUM TYPES
DO $$ BEGIN
    CREATE TYPE user_role AS ENUM ('SUPER_ADMIN', 'DOCTOR', 'PATIENT');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

DO $$ BEGIN
    CREATE TYPE appointment_status AS ENUM ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- 3. CORE USERS & AUTHENTICATION TABLE
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    role user_role NOT NULL DEFAULT 'PATIENT',
    requires_password_reset BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- 4. DOCTOR PROFILES (Admin Provisioned Only)
CREATE TABLE IF NOT EXISTS doctor_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    council_registration_number VARCHAR(100) UNIQUE NOT NULL, -- e.g. GMC / NMC ID
    qualifications VARCHAR(255) NOT NULL, -- e.g. MBBS, MD (Paediatrics)
    speciality VARCHAR(150) NOT NULL,
    clinic_affiliation VARCHAR(255) NOT NULL,
    contact_details VARCHAR(255) NOT NULL,
    consultation_fee NUMERIC(10, 2) NOT NULL DEFAULT 50.00,
    consultation_timings VARCHAR(150) NOT NULL DEFAULT 'Mon-Fri 09:00 - 17:00',
    digital_signature_hash VARCHAR(128) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT TRUE,
    provisioned_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_doctor_council_reg ON doctor_profiles(council_registration_number);
CREATE INDEX IF NOT EXISTS idx_doctor_speciality ON doctor_profiles(speciality);

-- 5. PATIENT PROFILES
CREATE TABLE IF NOT EXISTS patient_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    date_of_birth DATE,
    age INT,
    gender VARCHAR(20) NOT NULL,
    blood_group VARCHAR(10),
    weight_kg NUMERIC(5, 2),
    allergies TEXT DEFAULT 'None known',
    chronic_conditions TEXT DEFAULT 'None',
    emergency_contact VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_patient_user ON patient_profiles(patient_id);

-- 6. FAMILY MEMBERS & DEPENDENTS
CREATE TABLE IF NOT EXISTS family_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    relationship VARCHAR(50) NOT NULL, -- Child, Spouse, Parent
    date_of_birth DATE,
    age_in_months INT,
    gender VARCHAR(20) NOT NULL,
    allergies TEXT DEFAULT 'None known',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_family_patient ON family_members(patient_id);

-- 7. WHO PEDIATRIC GROWTH TRACKER RECORDS
CREATE TABLE IF NOT EXISTS growth_records (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    family_member_id UUID REFERENCES family_members(id) ON DELETE CASCADE,
    recorded_date TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    age_in_months INT NOT NULL,
    weight_kg NUMERIC(5, 2) NOT NULL,
    height_cm NUMERIC(5, 2) NOT NULL,
    head_circumference_cm NUMERIC(5, 2),
    who_weight_percentile INT NOT NULL,
    who_height_percentile INT NOT NULL,
    clinical_notes TEXT,
    recorded_by UUID REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_growth_patient ON growth_records(patient_id);

-- 8. APPOINTMENTS & TELECONSULTATIONS
CREATE TABLE IF NOT EXISTS appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL REFERENCES users(id),
    patient_id UUID NOT NULL REFERENCES users(id),
    family_member_id UUID REFERENCES family_members(id),
    appointment_time TIMESTAMPTZ NOT NULL,
    consultation_type VARCHAR(50) NOT NULL DEFAULT 'Video Call',
    chief_complaint TEXT NOT NULL,
    status appointment_status NOT NULL DEFAULT 'SCHEDULED',
    meeting_room_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_apt_doctor ON appointments(doctor_id, appointment_time);
CREATE INDEX IF NOT EXISTS idx_apt_patient ON appointments(patient_id, appointment_time);

-- 9. STANDARDIZED CLINICAL PRESCRIPTIONS
CREATE TABLE IF NOT EXISTS prescriptions (
    id VARCHAR(50) PRIMARY KEY, -- e.g. RX-2026-9041
    appointment_id UUID REFERENCES appointments(id),
    doctor_id UUID NOT NULL REFERENCES users(id),
    patient_id UUID NOT NULL REFERENCES users(id),
    doctor_name VARCHAR(150) NOT NULL,
    doctor_qualifications VARCHAR(255) NOT NULL,
    doctor_speciality VARCHAR(150) NOT NULL,
    council_registration_number VARCHAR(100) NOT NULL,
    clinic_affiliation VARCHAR(255) NOT NULL,
    patient_name VARCHAR(150) NOT NULL,
    patient_age INT NOT NULL,
    patient_gender VARCHAR(20) NOT NULL,
    patient_weight_kg NUMERIC(5, 2),
    patient_allergies TEXT,
    patient_chronic_conditions TEXT,
    diagnosis_icd10 VARCHAR(255) NOT NULL,
    vitals_bp VARCHAR(50),
    vitals_pulse VARCHAR(50),
    vitals_spo2 VARCHAR(50),
    vitals_temp VARCHAR(50),
    medications_json JSONB NOT NULL,
    clinical_advice_json TEXT,
    lab_orders_json TEXT,
    follow_up_text VARCHAR(255),
    follow_up_date TIMESTAMPTZ,
    doctor_signature_hash VARCHAR(128) NOT NULL,
    verification_qr_url TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rx_patient ON prescriptions(patient_id);
CREATE INDEX IF NOT EXISTS idx_rx_doctor ON prescriptions(doctor_id);

-- 10. CLINICAL AUDIT TRAILS (Immutable)
CREATE TABLE IF NOT EXISTS clinical_audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    action VARCHAR(100) NOT NULL,
    performed_by_email VARCHAR(255) NOT NULL,
    user_role VARCHAR(50) NOT NULL,
    target_resource VARCHAR(100) NOT NULL,
    details TEXT NOT NULL,
    ip_address VARCHAR(45),
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON clinical_audit_logs(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_actor ON clinical_audit_logs(performed_by_email);
