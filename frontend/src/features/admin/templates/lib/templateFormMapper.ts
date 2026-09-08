import type { TemplateDetailResponseDto } from '../../../../api/model/templateDetailResponseDto';
import type { UpsertTemplateRequestDto } from '../../../../api/model/upsertTemplateRequestDto';
import type { TemplateFormViewModel, TemplateTypeCode } from './templateTypes';

export function createEmptyTemplateForm(): TemplateFormViewModel {
  return {
    name: '',
    description: '',
    type: 'CEUB',
  };
}

function toTypeCode(value: string | undefined): TemplateTypeCode {
  return value === 'ARCU-SUR' ? 'ARCU-SUR' : 'CEUB';
}

export function mapDetailToForm(detail: TemplateDetailResponseDto): TemplateFormViewModel {
  return {
    name: detail.name ?? '',
    description: detail.description ?? '',
    type: toTypeCode(detail.type),
  };
}

export function mapFormToUpsertRequest(form: TemplateFormViewModel): UpsertTemplateRequestDto {
  return {
    name: form.name.trim(),
    description: form.description.trim() || undefined,
    type: form.type,
  };
}
