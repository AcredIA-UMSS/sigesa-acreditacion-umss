export const BACKEND_ROLES = ['JD', 'CC', 'TD'] as const;

export type BackendRoleCode = (typeof BACKEND_ROLES)[number];

export const ROLE_LABELS: Record<BackendRoleCode, string> = {
  JD: 'Jefe',
  CC: 'Coordinador',
  TD: 'Técnico',
};

/** Roles assignable in user registration (excludes public/unauthenticated access). */
export const ASSIGNABLE_ROLES: BackendRoleCode[] = ['JD', 'CC', 'TD'];

export const ROLE_REQUIRES_PROGRAM: Record<BackendRoleCode, boolean> = {
  JD: false,
  CC: true,
  TD: false,
};

export function isBackendRoleCode(value: string): value is BackendRoleCode {
  return (BACKEND_ROLES as readonly string[]).includes(value);
}

export function getRoleLabel(code: string): string {
  if (isBackendRoleCode(code)) {
    return ROLE_LABELS[code];
  }
  return code;
}
