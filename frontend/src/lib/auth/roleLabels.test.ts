import { describe, expect, it } from 'vitest';
import {
  ASSIGNABLE_ROLES,
  getRoleLabel,
  isBackendRoleCode,
  ROLE_REQUIRES_PROGRAM,
} from './roleLabels';

describe('roleLabels — CAE_MEMBER', () => {
  it('reconoce CAE_MEMBER como rol de backend asignable con carrera', () => {
    expect(isBackendRoleCode('CAE_MEMBER')).toBe(true);
    expect(ASSIGNABLE_ROLES).toContain('CAE_MEMBER');
    expect(ROLE_REQUIRES_PROGRAM.CAE_MEMBER).toBe(true);
    expect(getRoleLabel('CAE_MEMBER')).toContain('CAE');
  });
});
