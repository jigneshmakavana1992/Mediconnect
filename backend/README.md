# MedConnect Pro – Enterprise Telemedicine Architecture

## 1. Core Architectural Constraints & Access Governance

### Strict Role Separation (Single Client Architecture)
The application serves two distinct user types within the same client build: **Patient** and **Doctor/Admin**. 
* **Zero Self-Registration for Doctors:** The public registration interface NEVER exposes a doctor registration flow. Only Patients can sign up via the client app.
* **Admin-Only Doctor Provisioning:** Doctor accounts can exclusively be provisioned and onboarded via the Admin panel. Once created by an admin, the system dispatches secure credentials to the doctor with a mandatory password reset on first login.
* **Dynamic Navigation & Route Guards:** Strict JWT-based RBAC routes dynamically upon authentication:
  * Super Admin -> Administrative Control Console (`/admin`)
  * Doctor -> Telemedicine Clinical Workspace (`/doctor`)
  * Patient -> Patient EMR Portal & Telehealth Queue (`/patient`)
  Any route tampering or privilege escalation attempt is blocked with an HTTP 403 Forbidden and logged to the immutable audit trail.

---

## 2. Initial Super-Admin Bootstrap Credentials
* **Email:** `jigneshmakavana1992@gmail.com`
* **Temporary Password:** `admin@98`
* **Password Policy:** Enforced reset on initial login (`requires_password_reset = true`), Bcrypt cost factor = 12.

---

## 3. Entity Relationship Diagram (ERD)

```
[Users]
  │ 1:1
  ├────────► [DoctorProfiles] (Council Reg, Qualifications, Affiliation, Fee)
  │ 1:1
  ├────────► [PatientProfiles] (Age, Gender, Blood Group, Allergies, Chronic)
  │ 1:N
  ├────────► [FamilyMembers] (Dependents, DOB, Relationship)
  │            │ 1:N
  │            └────────► [GrowthRecords] (WHO Percentiles, Weight, Height)
  │ 1:N
  ├────────► [Appointments] (DoctorId, PatientId, RoomId, Status)
  │            │ 1:N
  │            └────────► [Prescriptions] (ICD-10, Vitals, Rx JSON, SHA-256 Sign, QR)
  │
  └────────► [ClinicalAuditLogs] (Immutable HIPAA Log: Actor, Action, Resource, IP)
```

---

## 4. Key Endpoints
* `POST /api/auth/login`
* `POST /api/auth/patient/register`
* `POST /api/auth/reset-password`
* `POST /api/admin/doctors/provision` (Admin only)
* `GET  /api/admin/audit-logs` (Admin only)
* `POST /api/prescriptions` (Doctor only with Council Reg check)
* `GET  /api/prescriptions/:id` (Cryptographically verified EMR view)
* `POST /api/patients/:id/growth` (WHO percentile tracker)
