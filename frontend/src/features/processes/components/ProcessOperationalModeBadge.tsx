export type OperationalMode =
  | 'ACTIVE'
  | 'AUDIT_READONLY'
  | 'IMPROVEMENT_MONITORING'
  | string;

const MODE_CONFIG: Record<string, { label: string; className: string }> = {
  ACTIVE: {
    label: 'Operativo',
    className: 'bg-success/15 text-success border-success/30',
  },
  AUDIT_READONLY: {
    label: 'Congelado (auditoría)',
    className: 'bg-warning/15 text-warning border-warning/40',
  },
  IMPROVEMENT_MONITORING: {
    label: 'Monitoreo de mejoras',
    className: 'bg-info/15 text-info border-info/40',
  },
};

interface ProcessOperationalModeBadgeProps {
  mode?: string;
}

export function ProcessOperationalModeBadge({ mode }: ProcessOperationalModeBadgeProps) {
  const normalized = mode?.toUpperCase() ?? 'ACTIVE';
  const config = MODE_CONFIG[normalized] ?? {
    label: normalized,
    className: 'bg-gray-100 text-gray-700 border-gray-300',
  };
  return (
    <span
      data-testid="operational-mode-badge"
      className={`inline-flex rounded-full border px-2.5 py-0.5 text-label-md font-medium ${config.className}`}
    >
      {config.label}
    </span>
  );
}
