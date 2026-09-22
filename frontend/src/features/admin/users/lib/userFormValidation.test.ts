import { describe, expect, it } from 'vitest';
import {
  validateConfirmPassword,
  validateEmail,
  validatePassword,
  validatePersonName,
  validatePhone,
  validateUserForm,
} from './userFormValidation';

describe('userFormValidation', () => {
  it('shouldAcceptAValidUmssCoordinatorForm', () => {
    const errors = validateUserForm({
      firstName: 'Ana',
      lastName: 'López',
      email: 'ana@umss.edu.bo',
      phoneNumber: '71234567',
      role: 'CC',
      programId: 'prog-1',
      password: 'Segura2026!',
      confirmPassword: 'Segura2026!',
      requiresProgram: true,
    });
    expect(errors).toEqual({});
  });

  it('shouldRejectEmptyNameNumbersAndInvalidEmailDomain', () => {
    expect(validatePersonName('', 'Nombre(s)')).toContain('obligatorio');
    expect(validatePersonName('1234', 'Nombre(s)')).toContain('letra');
    expect(validateEmail('user@gmail.com')).toContain('@umss.edu.bo');
  });

  it('shouldRejectWeakPasswordAndMismatchedConfirmation', () => {
    expect(validatePassword('short')).toContain('8 caracteres');
    expect(validateConfirmPassword('Segura2026!', 'otra')).toContain('no coinciden');
  });

  it('shouldRequireProgramWhenRoleNeedsAssignment', () => {
    const errors = validateUserForm({
      firstName: 'Ana',
      lastName: 'López',
      email: 'ana@umss.edu.bo',
      phoneNumber: '71234567',
      role: 'CC',
      programId: '',
      password: 'Segura2026!',
      confirmPassword: 'Segura2026!',
      requiresProgram: true,
    });
    expect(errors.programId).toBe('Seleccione la carrera asignada.');
  });

  it('shouldRejectInvalidPhoneNumbers', () => {
    expect(validatePhone('123')).toContain('8 dígitos');
  });
});
