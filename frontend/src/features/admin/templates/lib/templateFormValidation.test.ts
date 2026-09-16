import { describe, expect, it } from 'vitest';
import { hasTemplateFormErrors, validateTemplateForm } from './templateFormValidation';
import type { TemplateFormViewModel } from './templateTypes';

function validForm(): TemplateFormViewModel {
  return {
    name: 'CEUB 2026',
    description: 'Plantilla piloto',
    type: 'CEUB',
    phases: [
      {
        clientId: 'p1',
        name: 'Autoevaluación',
        order: 1,
        description: '',
        subphases: [
          {
            clientId: 's1',
            name: 'Diagnóstico',
            order: 1,
            referenceUrl: 'https://duea.umss.edu.bo/guia',
            description: '',
            requirements: 'Adjuntar informe y evidencias.',
          },
        ],
      },
    ],
  };
}

describe('templateFormValidation', () => {
  it('shouldAcceptACompleteHttpsStructure', () => {
    const errors = validateTemplateForm(validForm());
    expect(hasTemplateFormErrors(errors)).toBe(false);
  });

  it('shouldRequireHttpsLinkAndRequirements', () => {
    const form = validForm();
    form.phases[0].subphases[0].referenceUrl = 'http://inseguro.example';
    form.phases[0].subphases[0].requirements = '';
    const errors = validateTemplateForm(form);
    expect(errors.phaseErrors?.p1.subphaseErrors?.s1.referenceUrl).toContain('https://');
    expect(errors.phaseErrors?.p1.subphaseErrors?.s1.requirements).toContain('requisitos');
  });

  it('shouldRejectEmptyTemplateNameAndMissingPhases', () => {
    const errors = validateTemplateForm({
      name: '  ',
      description: '',
      type: 'CEUB',
      phases: [],
    });
    expect(errors.name).toContain('obligatorio');
    expect(errors.phases).toContain('al menos una fase');
  });
});
