import { useListTemplates } from '../../../../api/endpoints/plantillas-normativas/plantillas-normativas';
import type { TemplateSummaryResponseDto } from '../../../../api/model/templateSummaryResponseDto';
import type { TemplateListFilters, TemplateRowViewModel, TemplateStatusCode, TemplateTypeCode } from '../lib/templateTypes';

function toStatus(value: string | undefined): TemplateStatusCode {
  if (value === 'PUBLISHED' || value === 'ARCHIVED') {
    return value;
  }
  return 'DRAFT';
}

function toType(value: string | undefined): TemplateTypeCode {
  return value === 'ARCU-SUR' ? 'ARCU-SUR' : 'CEUB';
}

function toRow(template: TemplateSummaryResponseDto): TemplateRowViewModel | null {
  if (!template.id || !template.name) {
    return null;
  }

  return {
    id: template.id,
    name: template.name,
    description: template.description?.trim() || '—',
    type: toType(template.type),
    status: toStatus(template.status),
    phaseCount: template.phaseCount ?? 0,
    subphaseCount: template.subphaseCount ?? 0,
  };
}

export function useTemplatesList(filters: TemplateListFilters) {
  const params = {
    status: filters.status || undefined,
    type: filters.type || undefined,
  };

  const query = useListTemplates(params);

  const templates = (query.data?.data ?? [])
    .map(toRow)
    .filter((row: TemplateRowViewModel | null): row is TemplateRowViewModel => row !== null);

  return {
    templates,
    isLoading: query.isLoading,
    isError: query.isError,
    error: query.error,
    refetch: query.refetch,
  };
}
