export type UserFormField =
  | 'firstName'
  | 'lastName'
  | 'email'
  | 'phoneNumber'
  | 'role'
  | 'programId'
  | 'password'
  | 'confirmPassword';

export type UserFormErrors = Partial<Record<UserFormField, string>>;

const NAME_PATTERN = /^(?=.*[\p{L}])[\p{L}\s'.-]+$/u;
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const UMSS_EMAIL_PATTERN = /^[^\s@]+@umss\.edu\.bo$/i;
const PHONE_PATTERN = /^[67]\d{7}$/;
const PASSWORD_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).{8,}$/;

export function validatePersonName(value: string, label: string): string | undefined {
  const trimmed = value.trim();
  if (!trimmed) {
    return `${label} es obligatorio.`;
  }
  if (!NAME_PATTERN.test(trimmed)) {
    return `${label} debe contener al menos una letra y no puede ser solo números o símbolos.`;
  }
  return undefined;
}

export function validateEmail(value: string): string | undefined {
  const trimmed = value.trim();
  if (!trimmed) {
    return 'El correo electrónico es obligatorio.';
  }
  if (!EMAIL_PATTERN.test(trimmed)) {
    return 'Ingrese un correo electrónico válido.';
  }
  if (!UMSS_EMAIL_PATTERN.test(trimmed)) {
    return 'Solo se permiten correos institucionales @umss.edu.bo.';
  }
  return undefined;
}

export function validatePhone(value: string): string | undefined {
  const digits = value.replace(/\D/g, '');
  if (!digits) {
    return 'El celular es obligatorio.';
  }
  if (!PHONE_PATTERN.test(digits)) {
    return 'El celular debe tener 8 dígitos entre 60000000 y 79999999.';
  }
  return undefined;
}

export function validatePassword(value: string): string | undefined {
  if (!value) {
    return 'La contraseña es obligatoria.';
  }
  if (!PASSWORD_PATTERN.test(value)) {
    return 'Mínimo 8 caracteres, con letras y números.';
  }
  return undefined;
}

export function validateConfirmPassword(password: string, confirmPassword: string): string | undefined {
  if (!confirmPassword) {
    return 'Debe repetir la contraseña.';
  }
  if (password !== confirmPassword) {
    return 'Las contraseñas no coinciden.';
  }
  return undefined;
}

export interface ValidateUserFormInput {
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
  role: string;
  programId: string;
  password: string;
  confirmPassword: string;
  requiresProgram: boolean;
}

export function validateUserForm(input: ValidateUserFormInput): UserFormErrors {
  const errors: UserFormErrors = {};

  const firstNameError = validatePersonName(input.firstName, 'Nombre(s)');
  if (firstNameError) errors.firstName = firstNameError;

  const lastNameError = validatePersonName(input.lastName, 'Apellido(s)');
  if (lastNameError) errors.lastName = lastNameError;

  const emailError = validateEmail(input.email);
  if (emailError) errors.email = emailError;

  const phoneError = validatePhone(input.phoneNumber);
  if (phoneError) errors.phoneNumber = phoneError;

  const passwordError = validatePassword(input.password);
  if (passwordError) errors.password = passwordError;

  const confirmError = validateConfirmPassword(input.password, input.confirmPassword);
  if (confirmError) errors.confirmPassword = confirmError;

  if (!input.role) {
    errors.role = 'Seleccione un rol.';
  }

  if (input.requiresProgram && !input.programId) {
    errors.programId = 'Seleccione la carrera asignada.';
  }

  return errors;
}

export function normalizePhoneDigits(value: string): string {
  return value.replace(/\D/g, '').slice(0, 8);
}
