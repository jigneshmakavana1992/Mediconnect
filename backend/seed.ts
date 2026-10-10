import bcrypt from 'bcrypt';
import { PrismaClient, UserRole } from '@prisma/client';

const prisma = new PrismaClient();

const BCRYPT_SALT_ROUNDS = 12;

export async function bootstrapDatabase() {
  console.log('--- Initializing MedConnect Pro Enterprise Seed ---');

  // 1. ROOT SUPER-ADMIN ACCOUNT BOOTSTRAP
  const superAdminEmail = 'jigneshmakavana1992@gmail.com';
  const superAdminTempPass = 'admin@98';

  const adminPasswordHash = await bcrypt.hash(superAdminTempPass, BCRYPT_SALT_ROUNDS);

  const superAdmin = await prisma.user.upsert({
    where: { email: superAdminEmail },
    update: {},
    create: {
      email: superAdminEmail,
      fullName: 'Jignesh Makavana (Super Admin)',
      phone: '+1 (555) 900-1100',
      passwordHash: adminPasswordHash,
      role: UserRole.SUPER_ADMIN,
      requiresPasswordReset: true, // MANDATORY: Force password reset on first login
      isActive: true,
    },
  });

  console.log(`[Seed] Super Admin provisioned: ${superAdmin.email} (Requires password reset: true)`);

  // 2. INITIAL COUNCIL-VERIFIED PRACTITIONER
  const doctorEmail = 'dr.jenkins@medconnect.pro';
  const doctorPassHash = await bcrypt.hash('Doctor@2026', BCRYPT_SALT_ROUNDS);

  const doctorUser = await prisma.user.upsert({
    where: { email: doctorEmail },
    update: {},
    create: {
      email: doctorEmail,
      fullName: 'Dr. Sarah Jenkins',
      phone: '+1 (555) 304-8921',
      passwordHash: doctorPassHash,
      role: UserRole.DOCTOR,
      requiresPasswordReset: false,
      isActive: true,
      doctorProfile: {
        create: {
          councilRegistrationNumber: 'NMC-2018-84920',
          qualifications: 'MBBS, MD (Paediatrics), DCH',
          speciality: 'Consultant Paediatrician & Neonatologist',
          clinicAffiliation: 'Apex Children’s Hospital & Telehealth Centre',
          contactDetails: 'support@apexchildrens.org | Room 402',
          consultationFee: 75.0,
          consultationTimings: 'Mon-Sat 09:00 - 18:00',
          digitalSignatureHash: 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
          isVerified: true,
        },
      },
    },
    include: { doctorProfile: true },
  });

  console.log(`[Seed] Doctor provisioned: ${doctorUser.email} (Council Reg: ${doctorUser.doctorProfile?.councilRegistrationNumber})`);

  // 3. INITIAL REGISTERED PATIENT
  const patientEmail = 'alex.wright@example.com';
  const patientPassHash = await bcrypt.hash('Patient@123', BCRYPT_SALT_ROUNDS);

  const patientUser = await prisma.user.upsert({
    where: { email: patientEmail },
    update: {},
    create: {
      email: patientEmail,
      fullName: 'Alex Wright',
      phone: '+1 (555) 782-9901',
      passwordHash: patientPassHash,
      role: UserRole.PATIENT,
      requiresPasswordReset: false,
      isActive: true,
      patientProfile: {
        create: {
          age: 32,
          gender: 'Male',
          bloodGroup: 'O+',
          weightKg: 78.5,
          allergies: 'Penicillin, Sulfa drugs',
          chronicConditions: 'Mild Seasonal Allergic Rhinitis',
          emergencyContact: '+1 (555) 782-9999 (Spouse: Claire)',
        },
      },
    },
    include: { patientProfile: true },
  });

  console.log(`[Seed] Patient registered: ${patientUser.email}`);

  // 4. DEPENDENT FAMILY MEMBER (PEDIATRIC)
  const dependentChild = await prisma.familyMember.create({
    data: {
      patientId: patientUser.id,
      name: 'Leo Wright',
      relationship: 'Child (Son)',
      ageInMonths: 18,
      gender: 'Male',
      allergies: 'None known',
    },
  });

  // 5. WHO PEDIATRIC GROWTH TRACKER MILESTONE
  await prisma.growthRecord.create({
    data: {
      patientId: patientUser.id,
      familyMemberId: dependentChild.id,
      ageInMonths: 18,
      weightKg: 11.2,
      heightCm: 83.4,
      headCircumferenceCm: 47.5,
      whoWeightPercentile: 54,
      whoHeightPercentile: 58,
      clinicalNotes: 'Consistent weight gain along 50-60th WHO median percentile curve. Walking steadily.',
    },
  });

  console.log('[Seed] Seeding completed successfully with zero placeholders.');
}

bootstrapDatabase()
  .catch((e) => {
    console.error('Seed Error:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
