import type { TemplateDetailResponseDto } from '../../../../api/model/templateDetailResponseDto';
import type { UpsertTemplateRequestDto } from '../../../../api/model/upsertTemplateRequestDto';
import type {
  TemplateFormViewModel,
  TemplatePhaseFormItem,
  TemplateSubphaseFormItem,
  TemplateTypeCode,
} from './templateTypes';

function createClientId(): string {
  return crypto.randomUUID();
}

export function createEmptySubphase(order: number): TemplateSubphaseFormItem {
  return {
    clientId: createClientId(),
    name: '',
    order,
    referenceUrl: '',
    description: '',
  };
}

export function createEmptyPhase(order: number): TemplatePhaseFormItem {
  return {
    clientId: createClientId(),
    name: '',
    order,
    description: '',
    subphases: [createEmptySubphase(1)],
  };
}

export function createEmptyTemplateForm(): TemplateFormViewModel {
  return {
    name: '',
    description: '',
    type: 'CEUB',
    phases: [createEmptyPhase(1)],
  };
}

function toTypeCode(value: string | undefined): TemplateTypeCode {
  return value === 'ARCU-SUR' ? 'ARCU-SUR' : 'CEUB';
}

export function mapDetailToForm(detail: TemplateDetailResponseDto): TemplateFormViewModel {
  const phases = (detail.phases ?? []).map((phase, phaseIndex) => ({
    clientId: phase.id ?? createClientId(),
    id: phase.id,
    name: phase.name ?? '',
    order: phase.order ?? phaseIndex + 1,
    description: phase.description ?? '',
    subphases: (phase.subphases ?? []).map((subphase, subIndex) => ({
      clientId: subphase.id ?? createClientId(),
      id: subphase.id,
      name: subphase.name ?? '',
      order: subphase.order ?? subIndex + 1,
      referenceUrl: subphase.referenceUrl ?? '',
      description: subphase.description ?? '',
    })),
  }));

  return {
    name: detail.name ?? '',
    description: detail.description ?? '',
    type: toTypeCode(detail.type),
    phases: phases.length > 0 ? phases : [createEmptyPhase(1)],
  };
}

export function mapFormToUpsertRequest(form: TemplateFormViewModel): UpsertTemplateRequestDto {
  return {
    name: form.name.trim(),
    description: form.description.trim() || undefined,
    type: form.type,
    phases: form.phases.map((phase) => ({
      id: phase.id,
      name: phase.name.trim(),
      order: phase.order,
      description: phase.description.trim() || undefined,
      subphases: phase.subphases.map((subphase) => ({
        id: subphase.id,
        name: subphase.name.trim(),
        order: subphase.order,
        referenceUrl: subphase.referenceUrl.trim(),
        description: subphase.description.trim() || undefined,
      })),
    })),
  };
}
