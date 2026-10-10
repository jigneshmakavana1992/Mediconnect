import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { PrismaClient, UserRole } from '@prisma/client';

const prisma = new PrismaClient();
const JWT_SECRET = process.env.JWT_SECRET || 'medconnect_enterprise_jwt_secret_key_2026';

export interface AuthenticatedUser {
  id: string;
  email: string;
  role: UserRole;
  fullName: string;
  requiresPasswordReset: boolean;
}

export interface AuthenticatedRequest extends Request {
  user?: AuthenticatedUser;
}

/**
 * Validates JWT Bearer token and attaches user payload
 */
export async function authenticateToken(
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    res.status(401).json({
      error: 'UNAUTHORIZED',
      message: 'Access denied. Missing bearer token.',
    });
    return;
  }

  try {
    const decoded = jwt.verify(token, JWT_SECRET) as AuthenticatedUser;

    const userInDb = await prisma.user.findUnique({
      where: { id: decoded.id },
      select: {
        id: true,
        email: true,
        role: true,
        fullName: true,
        requiresPasswordReset: true,
        isActive: true,
      },
    });

    if (!userInDb || !userInDb.isActive) {
      res.status(401).json({
        error: 'ACCOUNT_DISABLED',
        message: 'User account is deactivated or not found.',
      });
      return;
    }

    // ENFORCE MANDATORY PASSWORD RESET ON FIRST LOGIN
    if (userInDb.requiresPasswordReset && req.path !== '/api/auth/reset-password') {
      res.status(403).json({
        error: 'PASSWORD_RESET_REQUIRED',
        message: 'Mandatory password reset required before accessing clinical features.',
        requiresPasswordReset: true,
      });
      return;
    }

    req.user = userInDb;
    next();
  } catch (err) {
    res.status(403).json({
      error: 'INVALID_TOKEN',
      message: 'Cryptographic token validation failed.',
    });
  }
}

/**
 * Strict RBAC Role Guard Middleware
 */
export function requireRole(...allowedRoles: UserRole[]) {
  return async (req: AuthenticatedRequest, res: Response, next: NextFunction): Promise<void> => {
    if (!req.user) {
      res.status(401).json({ error: 'UNAUTHENTICATED' });
      return;
    }

    if (!allowedRoles.includes(req.user.role)) {
      // Log access violation into immutable audit trail
      await prisma.clinicalAuditLog.create({
        data: {
          action: 'ACCESS_VIOLATION_BLOCKED',
          performedByEmail: req.user.email,
          userRole: req.user.role,
          targetResource: req.originalUrl,
          details: `Role ${req.user.role} attempted unauthorized access to restricted resource requiring [${allowedRoles.join(', ')}]`,
          ipAddress: req.ip,
        },
      });

      res.status(403).json({
        error: 'FORBIDDEN',
        message: `HTTP 403 Forbidden: Insufficient permissions. Required: [${allowedRoles.join(', ')}].`,
      });
      return;
    }

    next();
  };
}

/**
 * Clinical Authority Verification Middleware:
 * Verifies that the clinician possesses an active Medical Council Registration Number
 * before issuing digital prescriptions.
 */
export async function requireVerifiedClinicalPractitioner(
  req: AuthenticatedRequest,
  res: Response,
  next: NextFunction
): Promise<void> {
  if (!req.user || req.user.role !== UserRole.DOCTOR) {
    res.status(403).json({
      error: 'NON_PRACTITIONER',
      message: 'Only registered medical doctors can write and issue prescriptions.',
    });
    return;
  }

  const doctorProfile = await prisma.doctorProfile.findUnique({
    where: { doctorId: req.user.id },
  });

  if (!doctorProfile || !doctorProfile.isVerified || !doctorProfile.councilRegistrationNumber) {
    res.status(403).json({
      error: 'UNVERIFIED_COUNCIL_REGISTRATION',
      message: 'Practitioner lacks verified Medical Council Registration credentials.',
    });
    return;
  }

  next();
}
