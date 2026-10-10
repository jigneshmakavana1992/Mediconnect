package com.example.ui.screens.policy

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Supported legal and compliance policy document types.
 */
enum class PolicyType(val displayName: String, val shortTitle: String) {
    PRIVACY("Privacy Policy", "Privacy"),
    TERMS("Terms & Conditions & Indemnity", "Terms & Indemnity"),
    REFUND("Refund & Cancellation Policy", "Refunds"),
    PAYOUT("Doctor Payout & Settlement Policy", "Payouts")
}

/**
 * Data model for a structured policy section.
 */
data class PolicySection(
    val id: String,
    val title: String,
    val category: String,
    val icon: ImageVector,
    val summary: String,
    val details: List<String>,
    val legalCitation: String? = null,
    val highlightBadge: String? = null,
    val badgeColor: Color? = null,
    val badgeTextColor: Color? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalPoliciesScreen(
    initialPolicy: PolicyType = PolicyType.PRIVACY,
    onBack: () -> Unit,
    onNavigateToComplaintsHelpdesk: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedPolicy by remember { mutableStateOf(initialPolicy) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var acknowledgedPolicies by remember { mutableStateOf(setOf<PolicyType>()) }
    var expandedSectionIds by remember { mutableStateOf(setOf<String>()) }

    BackHandler { onBack() }

    // Privacy Policy Content
    val privacySections = remember {
        listOf(
            PolicySection(
                id = "priv_1",
                title = "1. Scope & Healthcare Legislative Compliance",
                category = "Scope",
                icon = Icons.Default.Gavel,
                summary = "Remix MedConnect Pro complies with the Digital Personal Data Protection (DPDP) Act 2023, Telemedicine Practice Guidelines, and DISHA healthcare confidentiality standards.",
                details = listOf(
                    "This Privacy Policy applies to all patients, registered medical practitioners (Doctors), relationship managers, and administrators utilizing the MedConnect telemedicine platform.",
                    "Personal identifiable information (PII) and electronic medical records (EMR) are classified as Protected Health Information (PHI) with institutional-grade safeguards.",
                    "By registering or initiating a teleconsultation, you give explicit affirmative consent to processing strictly as outlined herein."
                ),
                legalCitation = "DPDP Act 2023 & NMC Telemedicine Practice Guidelines",
                highlightBadge = "STATUTORY STANDARD",
                badgeColor = MedTealContainer,
                badgeTextColor = MedTealPrimary
            ),
            PolicySection(
                id = "priv_2",
                title = "2. Clinical Data & Protected Health Information (PHI)",
                category = "Data Collected",
                icon = Icons.Default.MedicalServices,
                summary = "We collect clinical symptoms, consultation history, diagnostic records, WHO pediatric milestones, and digital prescriptions necessary for care delivery.",
                details = listOf(
                    "Patient Demographics: Full name, age, gender, contact number, emergency contacts, and registered family members.",
                    "Clinical History: Chief complaints, allergies (e.g. Penicillin, Sulfa drugs), chronic conditions, and past consultation notes.",
                    "Pediatric Growth Data: WHO growth milestones (weight, height, age) entered via the Growth Chart Tracker.",
                    "Digital Prescriptions: Medication dosages, frequency, duration, instructions, and ICD-10 diagnostic classifications generated by attending physicians."
                ),
                legalCitation = "Medical Council of India Code of Ethics Reg. 1.3",
                highlightBadge = "CLINICAL CONFIDENTIALITY",
                badgeColor = MedSuccessContainer,
                badgeTextColor = MedSuccess
            ),
            PolicySection(
                id = "priv_3",
                title = "3. Biometric & Credential Protection",
                category = "Security",
                icon = Icons.Default.Fingerprint,
                summary = "Hardware-backed biometric authentication uses Android Keystore secure enclave. Raw biometric templates never leave your device.",
                details = listOf(
                    "Biometric Verification: Touch ID / Fingerprint verification is executed on-device using Android BiometricPrompt with cryptographic key attestation.",
                    "No Central Biometric Storage: Fingerprint images and templates are never transmitted or stored on platform servers.",
                    "Password Security: Passwords are salted and hashed using PBKDF2/Argon2. Multi-factor OTP authentication provides one-time verification tokens expiring in 10 minutes.",
                    "Doctor KYC: Aadhaar numbers collected for practitioner registration are tokenized in compliance with UIDAI regulations and masked as XXXX-XXXX-1234."
                ),
                legalCitation = "Aadhaar Act 2016 & ISO/IEC 27001",
                highlightBadge = "ENCRYPTED ENCLAVE",
                badgeColor = MedTealContainer,
                badgeTextColor = MedTealDark
            ),
            PolicySection(
                id = "priv_4",
                title = "4. WebRTC Video & Audio Teleconsultation Privacy",
                category = "Teleconsultation",
                icon = Icons.Default.VideoCameraFront,
                summary = "Teleconsultation streams use peer-to-peer WebRTC encryption. Unauthorized recording is strictly prohibited.",
                details = listOf(
                    "End-to-End Media Stream Encryption: Video and audio packets are encrypted using DTLS-SRTP standards during transmission.",
                    "No Surreptitious Recording: Sessions are never recorded without concurrent, explicit consent from both patient and physician.",
                    "Call Metadata: Call start time, duration, and connection quality metrics are logged solely to fulfill billing, refund, and audit trail requirements."
                ),
                highlightBadge = "E2E ENCRYPTION",
                badgeColor = MedCyanContainer,
                badgeTextColor = MedCyanSecondary
            ),
            PolicySection(
                id = "priv_5",
                title = "5. Role-Based Access Control (RBAC) & EMR Vault",
                category = "Access Control",
                icon = Icons.Default.Shield,
                summary = "Strict RBAC ensures only treating physicians and patients can view specific health records. Administrative access is logged in immutable audit trails.",
                details = listOf(
                    "Doctor Access Boundary: Doctors can access medical records only for patients who have scheduled an active or past appointment with them.",
                    "Patient EMR Vault: Patients hold continuous access to their diagnostic history, family records, and digital prescriptions in the Patient Vault.",
                    "Super Admin & Audit Logging: System administrators cannot view private clinical notes without generating an immutable audit log entry visible in the compliance registry."
                ),
                highlightBadge = "ZERO TRUST RBAC",
                badgeColor = MedWarningContainer,
                badgeTextColor = MedWarning
            ),
            PolicySection(
                id = "priv_6",
                title = "6. Commercial Restraint & Non-Disclosure",
                category = "Sharing & Disclosure",
                icon = Icons.Default.Lock,
                summary = "We guarantee zero sale or commercial disclosure of patient medical data to advertisers, marketers, or data brokers.",
                details = listOf(
                    "No Commercial Data Selling: MedConnect Pro does not sell, rent, or lease patient health records to pharmaceutical companies or marketing agencies.",
                    "Pharmacy & Diagnostic Referrals: Health data is shared with fulfillment partners only when the patient explicitly requests e-prescription dispatch.",
                    "Statutory Disclosures: Disclosures to law enforcement occur strictly pursuant to valid court orders or mandatory public health reporting under Indian law."
                ),
                highlightBadge = "ZERO DATA SALE",
                badgeColor = MedSuccessContainer,
                badgeTextColor = MedSuccess
            ),
            PolicySection(
                id = "priv_7",
                title = "7. Patient Rights & Data Retention Period",
                category = "Patient Rights",
                icon = Icons.Default.FolderShared,
                summary = "You hold rights to review, download, update, and request data erasure, subject to mandatory 3-year medical record retention laws.",
                details = listOf(
                    "Right to Access & Portability: Download your clinical summaries and signed digital prescriptions anytime.",
                    "Right to Correction: Request updates to demographic, contact, and non-clinical record inaccuracies.",
                    "Mandatory Retention: Under National Medical Commission regulations, medical records and prescriptions must be preserved for at least 3 years from the date of consultation.",
                    "Account Deletion: Upon account closure, non-clinical personal identifiers are scrubbed, while required clinical logs are archived in encrypted cold storage."
                ),
                legalCitation = "NMC Reg. 2020 Clause 3.4",
                highlightBadge = "3-YR RETENTION",
                badgeColor = MedTealContainer,
                badgeTextColor = MedTealPrimary
            ),
            PolicySection(
                id = "priv_8",
                title = "8. Data Protection Officer (DPO) & Redressal",
                category = "Grievance",
                icon = Icons.Default.SupportAgent,
                summary = "Contact our designated Grievance & Data Protection Officer with responses guaranteed within 48 business hours.",
                details = listOf(
                    "Data Protection Officer: Dr. A. V. Kulkarni, Chief Compliance Officer",
                    "Email Contact: dpo@medconnect.pro | Phone: +91 1800-202-3001",
                    "Resolution Timeline: Statutory grievance acknowledgment within 48 hours and final disposition within 15 calendar days.",
                    "Direct Ticket Escalation: You may file a data privacy inquiry directly through the in-app Complaints & Helpdesk portal."
                ),
                highlightBadge = "48-HR SLA",
                badgeColor = MedSuccessContainer,
                badgeTextColor = MedSuccess
            )
        )
    }

    // Terms & Conditions & Indemnity Content
    val termsSections = remember {
        listOf(
            PolicySection(
                id = "term_1",
                title = "1. Platform Intermediary Status & Zero Clinical Liability",
                category = "Intermediary & Liability",
                icon = Icons.Default.Gavel,
                summary = "MedConnect Pro operates strictly as an electronic telecommunication intermediary under Section 79 of the IT Act. The platform is not liable for doctor negligence.",
                details = listOf(
                    "Intermediary Safe Harbor: MedConnect Pro is a digital communication conduit and software provider. Under Section 79 of the Information Technology Act 2000 and Telemedicine Practice Guidelines, the platform does not provide medical services directly.",
                    "NO PLATFORM LIABILITY FOR DOCTOR NEGLIGENCE: THE PLATFORM, ITS OPERATING COMPANY, DIRECTORS, AFFILIATES, AND EMPLOYEES SHALL NOT BE LIABLE OR RESPONSIBLE FOR ANY MEDICAL NEGLIGENCE, CLINICAL MALPRACTICE, MISDIAGNOSIS, MEDICATION ERROR, DELAY IN TREATMENT, FAILURE TO ADVISE IN-PERSON EXAMINATION, OR BODILY INJURY/COMPLICATIONS CAUSED BY ANY PRACTITIONER.",
                    "Direct Doctor-Patient Contract: All teleconsultations, clinical opinions, prescriptions, and healthcare advice establish a bilateral professional relationship exclusively between the independent registered medical practitioner and the patient.",
                    "No Warranty of Medical Efficacy: The platform makes no representation or warranty regarding the clinical accuracy, efficacy, or outcome of any diagnosis, treatment regimen, or prescription issued by consulting doctors."
                ),
                legalCitation = "Section 79, Information Technology Act 2000 & NMC Telemedicine Practice Guidelines",
                highlightBadge = "ZERO PLATFORM LIABILITY",
                badgeColor = MedCoralContainer,
                badgeTextColor = MedCoralTertiary
            ),
            PolicySection(
                id = "term_2",
                title = "2. Mandatory Doctor Professional Indemnity Insurance",
                category = "Doctor Indemnity",
                icon = Icons.Default.Security,
                summary = "Every doctor must independently procure and maintain an active Professional Indemnity Medical Malpractice Insurance policy covering telemedicine.",
                details = listOf(
                    "Mandatory Doctor Insurance Procurement: EVERY REGISTERED DOCTOR PRACTICING ON MEDCONNECT PRO MUST INDEPENDENTLY PROCURE, FUND, AND MAINTAIN AN ADEQUATE PROFESSIONAL INDEMNITY MEDICAL MALPRACTICE INSURANCE POLICY FROM AN IRDAI-REGISTERED GENERAL INSURER.",
                    "Telemedicine Coverage Required: The doctor's professional indemnity policy must explicitly cover errors, omissions, negligent acts, remote teleconsultations, electronic diagnostic assessments, and digitally generated prescriptions.",
                    "Continuous Active Policy Requirement: The practitioner must maintain policy validity without lapse. Proof of policy coverage (Certificate of Insurance and Policy Number) must be provided upon request and renewed annually.",
                    "Doctor Defense & Hold Harmless: Each doctor expressly agrees to defend, indemnify, and hold harmless MedConnect Pro, its officers, directors, and agents from any patient claims, demands, damages, arbitration awards, legal defense fees, or statutory liabilities arising out of alleged or actual clinical negligence, malpractice, or breach of professional standards."
                ),
                legalCitation = "IRDAI Professional Indemnity Standard & NMC Medical Ethics Regulations",
                highlightBadge = "MANDATORY DOCTOR INSURANCE",
                badgeColor = MedTealContainer,
                badgeTextColor = MedTealDark
            ),
            PolicySection(
                id = "term_3",
                title = "3. Independent Licensed Medical Practitioner Relationship",
                category = "Independent Status",
                icon = Icons.Default.MedicalServices,
                summary = "Doctors are independent licensed practitioners exercising sole medical discretion, with no employer-employee relationship with MedConnect.",
                details = listOf(
                    "Independent Contractor Status: Consulting doctors are independent healthcare practitioners licensed by their respective State Medical Councils or the National Medical Commission (NMC). Nothing in these terms creates an employment, agency, joint venture, or partnership relationship.",
                    "Independent Clinical Judgment: Practitioners retain sole and uncompromised discretion regarding clinical protocols, whether a teleconsultation is medically appropriate, or if an in-person physical examination is mandated.",
                    "Credential Validity: Doctors warrant that their council registration is valid, current, and unencumbered by any disciplinary proceedings, suspensions, or revocations."
                ),
                legalCitation = "National Medical Commission Act 2019",
                highlightBadge = "INDEPENDENT CONTRACTOR",
                badgeColor = MedCyanContainer,
                badgeTextColor = MedCyanSecondary
            ),
            PolicySection(
                id = "term_4",
                title = "4. Patient Acknowledgment & Telemedicine Limitations",
                category = "Patient Consent",
                icon = Icons.Default.Assignment,
                summary = "Patients acknowledge the inherent physical constraints of remote consultations and agree to provide truthful medical disclosures.",
                details = listOf(
                    "Absence of Physical Palpation: Patients understand that a virtual consultation does not replace physical in-person examination, vital auscultation, or hands-on clinical assessment.",
                    "Duty of Honest Clinical Disclosure: Patients agree to furnish complete, accurate details regarding present symptoms, known drug allergies (e.g. Penicillin, NSAIDs), existing prescriptions, and chronic conditions. Neither the platform nor the doctor is liable for adverse reactions resulting from patient non-disclosure.",
                    "Doctor Right to Redirect: Attending physicians reserve the professional right to decline virtual treatment and instruct the patient to visit a hospital emergency department or clinic immediately."
                ),
                highlightBadge = "INFORMED PATIENT CONSENT",
                badgeColor = MedWarningContainer,
                badgeTextColor = MedWarning
            ),
            PolicySection(
                id = "term_5",
                title = "5. Life-Threatening Emergencies Prohibition",
                category = "Emergency Disclaimer",
                icon = Icons.Default.WarningAmber,
                summary = "Teleconsultations are strictly prohibited for acute medical emergencies. Immediately call 112/108 or proceed to the nearest emergency room.",
                details = listOf(
                    "Do NOT Use for Acute Emergencies: MedConnect Pro is strictly NOT intended for severe chest pain, acute myocardial infarction, stroke signs, severe dyspnea, massive hemorrhage, deep trauma, poisoning, or altered consciousness.",
                    "Emergency Services Action: In acute emergencies, dial national emergency assistance (112 or 108 in India) or seek immediate care at the nearest hospital emergency casualty ward.",
                    "No Emergency Duty of Care: The platform disclaims any and all liability for complications or death resulting from any user attempting to access teleconsultation services for emergency care."
                ),
                legalCitation = "Telemedicine Practice Guidelines, Appendix Clause 5",
                highlightBadge = "NOT FOR EMERGENCIES",
                badgeColor = MedCoralContainer,
                badgeTextColor = MedCoralTertiary
            ),
            PolicySection(
                id = "term_6",
                title = "6. Prescription Guidelines & Controlled Substances Ban",
                category = "Prescriptions",
                icon = Icons.Default.Description,
                summary = "Prescribing Schedule X narcotics, psychotropics, and addictive habit-forming drugs via teleconsultation is strictly prohibited.",
                details = listOf(
                    "Strict Schedule X Ban: Doctors are prohibited by federal law and platform policy from prescribing Schedule X medications (narcotic drugs, psychoactive sedatives) over teleconsultation.",
                    "Permissible Formulations: Only OTC medications, List A (first-line chronic therapies), and List B (add-on medications) may be prescribed pursuant to NMC Telemedicine Practice Guidelines.",
                    "Clinical Discretion: Doctors maintain absolute discretion to decline prescription requests if medical necessity is not substantiated."
                ),
                legalCitation = "Drugs and Cosmetics Rules 1945 & NDPS Act 1985",
                highlightBadge = "SCHEDULE X PROHIBITED",
                badgeColor = MedTealContainer,
                badgeTextColor = MedTealPrimary
            ),
            PolicySection(
                id = "term_7",
                title = "7. Governing Law, Jurisdiction & Dispute Settlement",
                category = "Legal Jurisdiction",
                icon = Icons.Default.Balance,
                summary = "Medical negligence disputes lie solely between patient and doctor/insurer. Platform commercial terms are governed by Indian law.",
                details = listOf(
                    "Medical Negligence Claims: Any allegation of medical negligence or professional malpractice must be raised directly against the attending practitioner and their professional indemnity insurer before the competent consumer or civil forum.",
                    "Platform Arbitration: Non-clinical disputes regarding platform software functionality shall be referred to arbitration under the Arbitration and Conciliation Act 1996 in New Delhi, India.",
                    "Severability: If any provision of these terms is deemed unenforceable, the remaining terms shall continue in full force and effect."
                ),
                legalCitation = "Arbitration and Conciliation Act 1996",
                highlightBadge = "INDIAN JURISDICTION",
                badgeColor = MedTealContainer,
                badgeTextColor = MedTealDark
            )
        )
    }

    // Refund & Cancellation Policy Content
    val refundSections = remember {
        listOf(
            PolicySection(
                id = "ref_1",
                title = "1. Patient Cancellation Window & Fee Schedule",
                category = "Cancellation",
                icon = Icons.Default.EventBusy,
                summary = "Cancellation refunds are determined by the time remaining before the scheduled consultation slot.",
                details = listOf(
                    "More than 2 hours before slot: 100% full refund credited to your original UPI or bank payment method.",
                    "30 minutes to 2 hours before slot: 50% refund (50% retained as doctor reservation and slot-blocking fee).",
                    "Less than 30 minutes before slot: Non-refundable, as the physician has committed their schedule to your consultation.",
                    "Patient No-Show: If you do not join the teleconsultation within 15 minutes of the start time, the appointment is marked complete and fees are forfeited."
                ),
                highlightBadge = "100% REFUND (>2h)",
                badgeColor = MedSuccessContainer,
                badgeTextColor = MedSuccess
            ),
            PolicySection(
                id = "ref_2",
                title = "2. Doctor Unavailability & No-Show Guarantee",
                category = "Doctor No-Show",
                icon = Icons.Default.EventAvailable,
                summary = "If a doctor is unable to attend or misses your appointment, you receive an automatic 100% refund or free priority rescheduling.",
                details = listOf(
                    "Automatic 100% Refund: If the doctor fails to enter the teleconsultation room within 10 minutes of schedule, a full refund is immediately queued.",
                    "Instant Rescheduling Option: You may choose to reschedule to the doctor's next available slot at zero extra charge.",
                    "Doctor Cancellation: If the doctor cancels in advance due to clinical emergency, 100% refund is issued instantly with an SMS notification."
                ),
                highlightBadge = "100% GUARANTEE",
                badgeColor = MedTealContainer,
                badgeTextColor = MedTealPrimary
            ),
            PolicySection(
                id = "ref_3",
                title = "3. Technical Disruptions & Call Drop Policy",
                category = "Technical Failures",
                icon = Icons.Default.SignalCellularConnectedNoInternet0Bar,
                summary = "Network failure or server disconnects within the first 3 minutes qualify for a replacement session or full refund.",
                details = listOf(
                    "Call Drop Within 3 Minutes: If technical network failure prevents meaningful medical interaction within the initial 3 minutes, the system provides a free re-entry token.",
                    "Platform Server Outages: In the event of system downtime preventing call connection, an automatic 100% refund is initiated.",
                    "Teleconsultation Verification: Call session logs recorded by WebRTC signaling servers serve as objective proof for technical claims."
                ),
                highlightBadge = "TECH PROTECTION",
                badgeColor = MedCyanContainer,
                badgeTextColor = MedCyanSecondary
            ),
            PolicySection(
                id = "ref_4",
                title = "4. Non-Refundable Situations",
                category = "Exceptions",
                icon = Icons.Default.Block,
                summary = "Completed consultations with issued medical advice or generated digital prescriptions are strictly non-refundable.",
                details = listOf(
                    "Concluded Consultations: Once the doctor conducts the consultation and answers medical queries, the professional fee is fully earned and non-refundable.",
                    "Prescription Generated: Generation and signing of a digital prescription via the Rx Studio confirms clinical delivery and waives refund claims.",
                    "Clinical Disagreement: Disagreement with a doctor's diagnostic opinion, dosage advice, or ethical refusal to prescribe habit-forming drugs does not constitute grounds for refund."
                ),
                highlightBadge = "NON-REFUNDABLE",
                badgeColor = MedCoralContainer,
                badgeTextColor = MedCoralTertiary
            ),
            PolicySection(
                id = "ref_5",
                title = "5. Refund Disbursement Timelines & Channels",
                category = "Disbursement",
                icon = Icons.Default.AccountBalanceWallet,
                summary = "Approved refunds are disbursed within 24 to 48 hours for UPI and 3 to 5 business days for bank transfers.",
                details = listOf(
                    "UPI (Google Pay, PhonePe, Paytm, BHIM): Credited directly to the originating VPA within 2 to 24 hours.",
                    "Net Banking & Debit/Credit Cards: Processed via RBI-licensed payment aggregators within 3 to 5 banking days.",
                    "Zero Cancellation Penalty: MedConnect does not levy administrative deduction surcharges on valid refunds."
                ),
                highlightBadge = "24-48 HR DISBURSEMENT",
                badgeColor = MedSuccessContainer,
                badgeTextColor = MedSuccess
            ),
            PolicySection(
                id = "ref_6",
                title = "6. Dispute Redressal & Helpdesk Escalation",
                category = "Disputes",
                icon = Icons.Default.ReportProblem,
                summary = "Disputes are investigated by an assigned Relationship Manager within 24 hours via the in-app Helpdesk.",
                details = listOf(
                    "Filing a Dispute: Navigate to the Complaints Helpdesk, select category 'PAYMENT_REFUND', and attach appointment ID.",
                    "Audit Review: Teleconsultation duration, chat logs, and signaling metadata are evaluated by the clinical operations desk.",
                    "Escalation Matrix: If unresolved within 48 hours, disputes are escalated to the Senior Grievance Officer for final binding settlement."
                ),
                highlightBadge = "ESCROW SUPPORT",
                badgeColor = MedWarningContainer,
                badgeTextColor = MedWarning
            )
        )
    }

    // Doctor Payout Policy Content
    val payoutSections = remember {
        listOf(
            PolicySection(
                id = "pay_1",
                title = "1. Doctor Eligibility & Credential Verification",
                category = "Eligibility & KYC",
                icon = Icons.Default.VerifiedUser,
                summary = "Only verified medical practitioners with NMC registration, Aadhaar KYC, and linked PAN/Bank accounts are eligible for disbursements.",
                details = listOf(
                    "NMC / State Medical Council Verification: Active council registration must be validated by the Super Admin prior to enabling consultations.",
                    "Aadhaar Identity KYC: Practitioners must complete Aadhaar-based OTP verification to prevent impersonation and identity fraud.",
                    "Verified Bank Account & UPI VPA: Bank account name must match the doctor's PAN card to comply with RBI and Income Tax regulations.",
                    "Zero Self-Registration Policy: Unverified self-onboarded profiles are barred from receiving patient appointments and financial settlements."
                ),
                legalCitation = "NMC Act 2019 & Prevention of Money Laundering Act (PMLA)",
                highlightBadge = "MANDATORY KYC",
                badgeColor = MedTealContainer,
                badgeTextColor = MedTealDark
            ),
            PolicySection(
                id = "pay_2",
                title = "2. Weekly Monday Settlement Cycle",
                category = "Settlement Cycle",
                icon = Icons.Default.CalendarToday,
                summary = "Disbursements are executed on an automated weekly cycle every Monday at 06:00 AM IST via NEFT, RTGS, or UPI.",
                details = listOf(
                    "Weekly Monday Processing: All matured batches are compiled and processed every Monday morning directly to registered bank accounts.",
                    "Automated UTR Generation: Platform generates unique Bank UTR (Unique Transaction Reference) codes visible in the Doctor Payment Hub.",
                    "Weekend Cut-Off: Consultations whose escrow maturation falls on or before Sunday 23:59 IST are included in the immediate Monday payout batch.",
                    "Holiday Handling: If Monday coincides with an RTGS/NEFT bank holiday, payouts are executed on the immediate next banking day."
                ),
                highlightBadge = "EVERY MONDAY 06:00",
                badgeColor = MedSuccessContainer,
                badgeTextColor = MedSuccess
            ),
            PolicySection(
                id = "pay_3",
                title = "3. Mandatory 30-Day Escrow Holding Period",
                category = "Escrow Rules",
                icon = Icons.Default.HourglassTop,
                summary = "Consultation fees are held in an escrow buffer for exactly 30 calendar days to accommodate follow-ups and patient dispute reviews.",
                details = listOf(
                    "30-Day Escrow Buffer: Following consultation completion, funds are assigned 'HOLDING_ESCROW' status for exactly 30 calendar days.",
                    "Reason for Escrow: Facilitates patient 7-day free follow-up queries, technical dispute resolutions, and medical audit reviews.",
                    "Maturation Transition: On day 31, escrow funds transition to 'MATURED_QUEUED' status and disburse on the subsequent Monday cycle.",
                    "Real-Time Transparency: Doctors can inspect exact release dates and escrow countdowns in the Doctor Payment & Settlement Hub."
                ),
                highlightBadge = "30-DAY ESCROW",
                badgeColor = MedCyanContainer,
                badgeTextColor = MedCyanSecondary
            ),
            PolicySection(
                id = "pay_4",
                title = "4. Platform Commission & Fee Structure",
                category = "Fees & Deductions",
                icon = Icons.Default.Percent,
                summary = "A standard 15% platform commission covers HD WebRTC infrastructure, encrypted EMR storage, and SMS gateway costs.",
                details = listOf(
                    "Doctor Net Share: Doctors receive 85% of their set consultation fee (e.g. ₹500 fee results in ₹425 base doctor earnings).",
                    "Platform Infrastructure: The 15% fee funds secure telemedicine servers, prescription digital signing engines, and 24/7 technical helpdesk.",
                    "Configurable Rates: Super-Admin configurable fee tiers allow preferential rates for institutional or high-volume healthcare departments."
                ),
                highlightBadge = "15% PLATFORM FEE",
                badgeColor = MedTealContainer,
                badgeTextColor = MedTealPrimary
            ),
            PolicySection(
                id = "pay_5",
                title = "5. Tax Deducted at Source (TDS) Compliance",
                category = "Tax & TDS",
                icon = Icons.Default.ReceiptLong,
                summary = "TDS is withheld at 10% under Section 194J of the Income Tax Act with quarterly Form 16A certificates provided.",
                details = listOf(
                    "Section 194J Withholding: 10% TDS is deducted on gross professional fees for practitioners holding a valid PAN card.",
                    "Non-PAN Deductions: If PAN is not linked, statutory higher withholding rate of 20% applies under Section 206AA.",
                    "Quarterly Form 16A: Official TDS certificates are uploaded to your settlement dashboard every quarter for income tax filing.",
                    "Direct PAN Matching: Payout bank accounts must match the verified PAN to avoid regulatory settlement holds."
                ),
                legalCitation = "Section 194J & 206AA, Indian Income Tax Act 1961",
                highlightBadge = "10% SEC 194J TDS",
                badgeColor = MedWarningContainer,
                badgeTextColor = MedWarning
            ),
            PolicySection(
                id = "pay_6",
                title = "6. Minimum Payout Threshold & Rollover",
                category = "Thresholds",
                icon = Icons.Default.PriceCheck,
                summary = "The minimum payout threshold is ₹500. Balances below ₹500 rollover to the subsequent Monday without penalty.",
                details = listOf(
                    "Threshold Limit: Matured balances of ₹500 or more are disbursed automatically on Monday.",
                    "Automatic Rollover: If matured earnings are less than ₹500, they remain safe in your account and combine with subsequent matured batches.",
                    "Account Inactivity: Accumulated earnings never expire or incur dormancy penalties."
                ),
                highlightBadge = "MIN ₹500 THRESHOLD",
                badgeColor = MedSuccessContainer,
                badgeTextColor = MedSuccess
            ),
            PolicySection(
                id = "pay_7",
                title = "7. Clawbacks, Chargebacks & Quality Holds",
                category = "Clawbacks",
                icon = Icons.Default.WarningAmber,
                summary = "Fees for confirmed doctor no-shows or substantiated clinical complaints are reversed from pending escrow balances.",
                details = listOf(
                    "No-Show Reversal: If an appointment is refunded to a patient due to verified doctor absence, the associated escrow credit is cancelled.",
                    "Substantiated Complaint Deductions: Proven cases of gross medical negligence or fraudulent prescription issuance subject funds to administrative clawback.",
                    "Negative Balance Treatment: If refunds exceed current escrow, the difference is offset against future matured consultation earnings.",
                    "Fair Opportunity for Defense: Doctors receive 7 calendar days to respond through the Helpdesk before clawback execution."
                ),
                highlightBadge = "DISPUTE CLAWBACK",
                badgeColor = MedCoralContainer,
                badgeTextColor = MedCoralTertiary
            ),
            PolicySection(
                id = "pay_8",
                title = "8. Settlement Helpdesk & Financial Inquiries",
                category = "Support",
                icon = Icons.Default.ContactPhone,
                summary = "Dedicated doctor finance desk reachable at +91 1800-202-3001 with query resolution within 24 hours.",
                details = listOf(
                    "Doctor Finance Helpline: +91 1800-202-3001 (Mon-Sat, 09:00 - 18:00 IST).",
                    "Direct In-App Category: Submit settlement queries under 'PAYMENT_PAYOUT' in the Complaints & Helpdesk portal.",
                    "Bank Rejection Handling: In case of IFSC/account mismatch, SMS alert is dispatched with instant re-submission facility."
                ),
                highlightBadge = "DIRECT HELPDESK",
                badgeColor = MedSuccessContainer,
                badgeTextColor = MedSuccess
            )
        )
    }

    // Active policy sections based on tab
    val currentSections = when (selectedPolicy) {
        PolicyType.PRIVACY -> privacySections
        PolicyType.TERMS -> termsSections
        PolicyType.REFUND -> refundSections
        PolicyType.PAYOUT -> payoutSections
    }

    // Available categories for current policy
    val categories = remember(currentSections) {
        listOf("ALL") + currentSections.map { it.category }.distinct()
    }

    // Filtered sections based on category and search query
    val filteredSections = remember(currentSections, selectedCategoryFilter, searchQuery) {
        currentSections.filter { sec ->
            val matchesCategory = selectedCategoryFilter == "ALL" || sec.category == selectedCategoryFilter
            val matchesSearch = searchQuery.isBlank() ||
                    sec.title.contains(searchQuery, ignoreCase = true) ||
                    sec.summary.contains(searchQuery, ignoreCase = true) ||
                    sec.details.any { it.contains(searchQuery, ignoreCase = true) } ||
                    (sec.legalCitation?.contains(searchQuery, ignoreCase = true) == true)
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MedBackgroundLight)
    ) {
        // Institutional Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("policy_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MedTealDark
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Institutional Policy Center",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MedTealDark
                    )
                    Text(
                        text = "MedConnect Pro • Legal, Privacy & Compliance Hub",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                // Helpdesk shortcut if available
                if (onNavigateToComplaintsHelpdesk != null) {
                    IconButton(
                        onClick = onNavigateToComplaintsHelpdesk,
                        modifier = Modifier.testTag("policy_helpdesk_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "Contact Grievance Desk",
                            tint = MedTealPrimary
                        )
                    }
                }
            }
        }

        // Policy Type Selector Tabs (Privacy, Refund, Payout)
        TabRow(
            selectedTabIndex = selectedPolicy.ordinal,
            containerColor = Color.White,
            contentColor = MedTealPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            PolicyType.values().forEach { policy ->
                Tab(
                    selected = selectedPolicy == policy,
                    onClick = {
                        selectedPolicy = policy
                        selectedCategoryFilter = "ALL"
                        searchQuery = ""
                    },
                    modifier = Modifier.testTag("policy_tab_${policy.name.lowercase()}"),
                    text = {
                        Text(
                            text = policy.shortTitle,
                            fontSize = 13.sp,
                            fontWeight = if (selectedPolicy == policy) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    icon = {
                        val icon = when (policy) {
                            PolicyType.PRIVACY -> Icons.Default.Shield
                            PolicyType.TERMS -> Icons.Default.Gavel
                            PolicyType.REFUND -> Icons.Default.CreditCard
                            PolicyType.PAYOUT -> Icons.Default.AccountBalance
                        }
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }
        }

        // Search Bar and Category Chips
        Surface(
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search policy terms, clauses, or citations...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MedTealPrimary, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("policy_search_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MedTealPrimary,
                        unfocusedBorderColor = MedOutlineLight
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            modifier = Modifier.testTag("policy_category_chip_${cat.lowercase()}"),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MedTealContainer,
                                selectedLabelColor = MedTealDark
                            )
                        )
                    }
                }
            }
        }

        // Policy Header Overview Banner
        val headerColor = when (selectedPolicy) {
            PolicyType.PRIVACY -> MedTealPrimary
            PolicyType.TERMS -> Color(0xFF1E293B) // Dark Navy
            PolicyType.REFUND -> MedCyanSecondary
            PolicyType.PAYOUT -> Color(0xFF0F172A)
        }

        val headerSubtitle = when (selectedPolicy) {
            PolicyType.PRIVACY -> "DPDP Act 2023 & DISHA Compliant • End-to-End Health Data Protection"
            PolicyType.TERMS -> "IT Act Sec 79 Intermediary • Platform Not Liable for Doctor Negligence • Doctor Indemnity Required"
            PolicyType.REFUND -> "100% Doctor No-Show Protection • 24-48h UPI Processing"
            PolicyType.PAYOUT -> "Automated Monday Settlements • 30-Day Dispute Escrow Holding"
        }

        Surface(
            color = headerColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedPolicy.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = headerSubtitle,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Rev v2.4 (2026)",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }

        // Policy Sections List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("policy_sections_list"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (filteredSections.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp)
                            .testTag("policy_empty_state_card"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No matching policy clauses found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MedTealDark
                            )
                            Text(
                                text = "Try modifying your search query or reset the category filter.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategoryFilter = "ALL"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary)
                            ) {
                                Text("Reset Filters")
                            }
                        }
                    }
                }
            } else {
                items(filteredSections, key = { it.id }) { section ->
                    val isExpanded = expandedSectionIds.contains(section.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedSectionIds = if (isExpanded) {
                                    expandedSectionIds - section.id
                                } else {
                                    expandedSectionIds + section.id
                                }
                            }
                            .testTag("policy_section_card_${section.id}"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isExpanded) MedTealPrimary.copy(alpha = 0.5f) else Color(0xFFE2E8F0)
                        ),
                        elevation = CardDefaults.cardElevation(if (isExpanded) 3.dp else 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Surface(
                                        color = MedTealContainer,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = section.icon,
                                                contentDescription = null,
                                                tint = MedTealPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = section.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MedTealDark
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        if (section.highlightBadge != null) {
                                            Surface(
                                                color = section.badgeColor ?: MedTealContainer,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = section.highlightBadge,
                                                    color = section.badgeTextColor ?: MedTealPrimary,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        expandedSectionIds = if (isExpanded) {
                                            expandedSectionIds - section.id
                                        } else {
                                            expandedSectionIds + section.id
                                        }
                                    },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("policy_expand_button_${section.id}")
                                ) {
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                                        tint = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = section.summary,
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                lineHeight = 17.sp
                            )

                            // Expanded Detailed Breakdown
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Column(modifier = Modifier.padding(top = 10.dp)) {
                                    HorizontalDivider(
                                        color = Color(0xFFF1F5F9),
                                        thickness = 1.dp
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    section.details.forEach { detail ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = MedTealPrimary,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .padding(top = 2.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = detail,
                                                fontSize = 11.sp,
                                                color = Color(0xFF334155),
                                                lineHeight = 16.sp
                                            )
                                        }
                                    }

                                    if (section.legalCitation != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            color = Color(0xFFF8FAFC),
                                            shape = RoundedCornerShape(6.dp),
                                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.Balance,
                                                    contentDescription = null,
                                                    tint = MedTealPrimary,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Statutory Reference: ${section.legalCitation}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MedTealDark
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Grievance & Compliance Desk Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .testTag("policy_grievance_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = MedCyanSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Grievance & Legal Compliance Desk",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "For clarification regarding our Privacy, Refund, or Doctor Payout policies, reach our dedicated compliance office. All official inquiries are recorded with timestamped audit logs.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onNavigateToComplaintsHelpdesk != null) {
                                Button(
                                    onClick = onNavigateToComplaintsHelpdesk,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .testTag("policy_lodge_ticket_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MedTealPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.SupportAgent, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Lodge Ticket", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            val isAcknowledged = acknowledgedPolicies.contains(selectedPolicy)
                            OutlinedButton(
                                onClick = {
                                    acknowledgedPolicies = if (isAcknowledged) {
                                        acknowledgedPolicies - selectedPolicy
                                    } else {
                                        acknowledgedPolicies + selectedPolicy
                                    }
                                    Toast.makeText(
                                        context,
                                        if (!isAcknowledged) "${selectedPolicy.displayName} Acknowledged" else "Acknowledgment Reset",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("policy_acknowledge_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isAcknowledged) MedSuccess else Color.White
                                ),
                                border = BorderStroke(1.dp, if (isAcknowledged) MedSuccess else Color(0xFF334155))
                            ) {
                                Icon(
                                    imageVector = if (isAcknowledged) Icons.Default.Check else Icons.Default.ThumbUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAcknowledged) "Acknowledged ✓" else "I Agree & Acknowledge",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
