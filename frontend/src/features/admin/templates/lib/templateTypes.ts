export type TemplateTypeCode = 'CEUB' | 'ARCU-SUR';

export type TemplateStatusCode = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface TemplateSubphaseFormItem {
  clientId: string;
  id?: string;
  name: string;
  order: number;
  referenceUrl: string;
  description: string;
  requirements: string;
}

export interface TemplatePhaseFormItem {
  clientId: string;
  id?: string;
  name: string;
  order: number;
  description: string;
  subphases: TemplateSubphaseFormItem[];
}

export interface TemplateFormViewModel {
  name: string;
  description: string;
  type: TemplateTypeCode;
  phases: TemplatePhaseFormItem[];
}

export interface TemplateListFilters {
  status: '' | TemplateStatusCode;
  type: '' | TemplateTypeCode;
}

export interface TemplateRowViewModel {
  id: string;
  name: string;
  description: string;
  type: TemplateTypeCode;
  status: TemplateStatusCode;
  phaseCount: number;
  subphaseCount: number;
}
