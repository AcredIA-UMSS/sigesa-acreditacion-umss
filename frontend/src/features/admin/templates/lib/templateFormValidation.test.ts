import { describe, expect, it } from 'vitest';
import { hasTemplateFormErrors, validateTemplateForm } from './templateFormValidation';
import type { TemplateFormViewModel } from './templateTypes';

function validForm(): TemplateFormViewModel {
  return {
    name: 'CEUB 2026',
    description: 'Plantilla piloto',
    type: 'CEUB',
  };
}

describe('templateFormValidation', () => {
  it('shouldAcceptACompleteHttpsStructure', () => {
    const errors = validateTemplateForm(validForm());
    expect(hasTemplateFormErrors(errors)).toBe(false);
  });

  it('shouldRejectInvalidTemplateType', () => {
    const errors = validateTemplateForm({
      ...validForm(),
      type: 'OTRO' as TemplateFormViewModel['type'],
    });
    expect(errors.type).toContain('CEUB o ARCU-SUR');
  });

  it('shouldRejectEmptyTemplateName', () => {
    const errors = validateTemplateForm({
      name: '  ',
      description: '',
      type: 'CEUB',
    });
    expect(errors.name).toContain('obligatorio');
  });
});
