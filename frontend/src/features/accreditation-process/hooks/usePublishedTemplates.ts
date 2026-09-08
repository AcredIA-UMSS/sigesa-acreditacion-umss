import { useListTemplates } from '../../../api/endpoints/plantillas-normativas/plantillas-normativas';
import type { TemplateSummaryResponseDto } from '../../../api/model/templateSummaryResponseDto';
import type { TemplateOption } from '../components/CreateProcessForm';

function toTemplateOption(template: TemplateSummaryResponseDto): TemplateOption | null {
  if (!template.id || !template.name) {
    return null;
  }

  const type = template.type?.trim() || 'CEUB';
  const level1Count = template.level1Count ?? 0;
  const indicatorCount = template.indicatorCount ?? 0;
  const structureHint =
    level1Count > 0 || indicatorCount > 0
      ? ` · ${level1Count} N1 · ${indicatorCount} ind.`
      : '';

  return {
    id: template.id,
    name: `${template.name} (${type})${structureHint}`,
    type,
  };
}

export function usePublishedTemplates() {
  const query = useListTemplates({ status: 'PUBLISHED' });

  const templates = (query.data?.data ?? [])
    .map(toTemplateOption)
    .filter((item): item is TemplateOption => item !== null)
    .sort((a, b) => a.name.localeCompare(b.name, 'es'));

  return {
    templates,
    isLoading: query.isLoading,
    isError: query.isError,
    error: query.error,
    refetch: query.refetch,
  };
}
