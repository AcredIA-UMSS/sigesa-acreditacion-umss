import type { BackendRoleCode } from './roleLabels';

export interface AuthSession {
  accessToken: string;
  expiresIn: number;
  role: BackendRoleCode;
  programScope: string[];
  expiresAt: number;
}

export const AUTH_STORAGE_KEY = 'sigesa_auth_session';

export const UMSS_EMAIL_SUFFIX = '@umss.edu.bo';

export const UMSS_EMAIL_PATTERN = /^[^\s@]+@umss\.edu\.bo$/i;
