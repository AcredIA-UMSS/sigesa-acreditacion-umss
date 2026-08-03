export const BACKEND_ROLES = ['JD', 'CC', 'TD', 'EE'] as const;

export type BackendRoleCode = (typeof BACKEND_ROLES)[number];

export const ROLE_LABELS: Record<BackendRoleCode, string> = {
  JD: 'Jefatura DUEA [JD]',
  CC: 'Coordinador de Carrera [CC]',
  TD: 'Técnico DUEA [TD]',
  EE: 'Evaluador externo [EE]',
};

/** Roles assignable in user registration (excludes public/unauthenticated access). */
export const ASSIGNABLE_ROLES: BackendRoleCode[] = ['JD', 'CC', 'TD', 'EE'];

export const ROLE_REQUIRES_PROGRAM: Record<BackendRoleCode, boolean> = {
  JD: false,
  CC: true,
  TD: false,
  EE: true,
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
