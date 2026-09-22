export type TemplateTypeCode = 'CEUB' | 'ARCU-SUR';

export type TemplateStatusCode = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';

export interface TemplateFormViewModel {
  name: string;
  description: string;
  type: TemplateTypeCode;
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
  level1Count: number;
  indicatorCount: number;
  evaluatorModel?: string;
}
