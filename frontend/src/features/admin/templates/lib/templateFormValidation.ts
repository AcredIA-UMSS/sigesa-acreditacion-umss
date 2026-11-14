import type { TemplateFormViewModel, TemplateTypeCode } from './templateTypes';

export interface TemplateFormErrors {
  name?: string;
  type?: string;
}

const TEMPLATE_TYPES: TemplateTypeCode[] = ['CEUB', 'ARCU-SUR'];

export function validateTemplateForm(form: TemplateFormViewModel): TemplateFormErrors {
  const errors: TemplateFormErrors = {};

  if (!form.name.trim()) {
    errors.name = 'El nombre de la plantilla es obligatorio.';
  }

  if (!TEMPLATE_TYPES.includes(form.type)) {
    errors.type = 'Seleccione CEUB o ARCU-SUR.';
  }

  return errors;
}

export function hasTemplateFormErrors(errors: TemplateFormErrors): boolean {
  return Boolean(errors.name || errors.type);
}
