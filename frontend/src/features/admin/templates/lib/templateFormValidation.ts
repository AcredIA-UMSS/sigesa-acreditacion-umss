import type {
  TemplateFormViewModel,
  TemplatePhaseFormItem,
  TemplateSubphaseFormItem,
  TemplateTypeCode,
} from './templateTypes';

export interface TemplateFormErrors {
  name?: string;
  type?: string;
  phases?: string;
  phaseErrors?: Record<string, PhaseFormErrors>;
}

export interface PhaseFormErrors {
  name?: string;
  order?: string;
  subphases?: string;
  subphaseErrors?: Record<string, SubphaseFormErrors>;
}

export interface SubphaseFormErrors {
  name?: string;
  order?: string;
  referenceUrl?: string;
}

const HTTPS_URL_PATTERN = /^https:\/\/.+/i;
const TEMPLATE_TYPES: TemplateTypeCode[] = ['CEUB', 'ARCU-SUR'];

function validateUniqueOrders(values: number[]): string | undefined {
  const seen = new Set<number>();
  for (const value of values) {
    if (seen.has(value)) {
      return 'Los valores de orden deben ser únicos en el mismo nivel.';
    }
    seen.add(value);
  }
  return undefined;
}

function validateSubphase(subphase: TemplateSubphaseFormItem): SubphaseFormErrors {
  const errors: SubphaseFormErrors = {};

  if (!subphase.name.trim()) {
    errors.name = 'El nombre de la subfase es obligatorio.';
  }

  if (!Number.isInteger(subphase.order) || subphase.order < 1) {
    errors.order = 'El orden debe ser un entero mayor o igual a 1.';
  }

  if (!subphase.referenceUrl.trim()) {
    errors.referenceUrl = 'El enlace HTTPS es obligatorio.';
  } else if (!HTTPS_URL_PATTERN.test(subphase.referenceUrl.trim())) {
    errors.referenceUrl = 'Use una URL que comience con https://';
  }

  return errors;
}

function validatePhase(phase: TemplatePhaseFormItem): PhaseFormErrors {
  const errors: PhaseFormErrors = {};

  if (!phase.name.trim()) {
    errors.name = 'El nombre de la fase es obligatorio.';
  }

  if (!Number.isInteger(phase.order) || phase.order < 1) {
    errors.order = 'El orden debe ser un entero mayor o igual a 1.';
  }

  if (phase.subphases.length === 0) {
    errors.subphases = 'Agregue al menos una subfase.';
  }

  const subphaseErrors: Record<string, SubphaseFormErrors> = {};
  for (const subphase of phase.subphases) {
    const subErrors = validateSubphase(subphase);
    if (Object.keys(subErrors).length > 0) {
      subphaseErrors[subphase.clientId] = subErrors;
    }
  }

  const subphaseOrderError = validateUniqueOrders(phase.subphases.map((item) => item.order));
  if (subphaseOrderError) {
    errors.subphases = subphaseOrderError;
  }

  if (Object.keys(subphaseErrors).length > 0) {
    errors.subphaseErrors = subphaseErrors;
  }

  return errors;
}

export function validateTemplateForm(form: TemplateFormViewModel): TemplateFormErrors {
  const errors: TemplateFormErrors = {};

  if (!form.name.trim()) {
    errors.name = 'El nombre de la plantilla es obligatorio.';
  }

  if (!TEMPLATE_TYPES.includes(form.type)) {
    errors.type = 'Seleccione CEUB o ARCU-SUR.';
  }

  if (form.phases.length === 0) {
    errors.phases = 'Agregue al menos una fase.';
  }

  const phaseOrderError = validateUniqueOrders(form.phases.map((phase) => phase.order));
  if (phaseOrderError) {
    errors.phases = phaseOrderError;
  }

  const phaseErrors: Record<string, PhaseFormErrors> = {};
  for (const phase of form.phases) {
    const phaseError = validatePhase(phase);
    if (Object.keys(phaseError).length > 0) {
      phaseErrors[phase.clientId] = phaseError;
    }
  }

  if (Object.keys(phaseErrors).length > 0) {
    errors.phaseErrors = phaseErrors;
  }

  return errors;
}

export function hasTemplateFormErrors(errors: TemplateFormErrors): boolean {
  if (errors.name || errors.type || errors.phases) {
    return true;
  }

  if (!errors.phaseErrors) {
    return false;
  }

  return Object.values(errors.phaseErrors).some(
    (phaseError) =>
      Boolean(phaseError.name || phaseError.order || phaseError.subphases) ||
      Boolean(phaseError.subphaseErrors && Object.keys(phaseError.subphaseErrors).length > 0),
  );
}
