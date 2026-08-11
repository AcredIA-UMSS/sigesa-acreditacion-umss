import type { TemplateStatusCode } from '../lib/templateTypes';

const STATUS_LABELS: Record<TemplateStatusCode, string> = {
  DRAFT: 'Borrador',
  PUBLISHED: 'Publicada',
  ARCHIVED: 'Archivada',
};

const STATUS_STYLES: Record<TemplateStatusCode, string> = {
  DRAFT: 'bg-warning/15 text-gray-800',
  PUBLISHED: 'bg-success/15 text-success',
  ARCHIVED: 'bg-gray-100 text-gray-600',
};

interface TemplateStatusBadgeProps {
  status: TemplateStatusCode;
}

export function TemplateStatusBadge({ status }: TemplateStatusBadgeProps) {
  return (
    <span
      className={`inline-flex rounded-full px-3 py-1 text-label-md font-medium ${STATUS_STYLES[status]}`}
    >
      {STATUS_LABELS[status]}
    </span>
  );
}
