type ProcessStatus = 'ACTIVE' | 'COMPLETED' | 'CANCELLED' | string;

interface ProcessStatusBadgeProps {
  status: ProcessStatus;
}

const statusConfig: Record<
  string,
  { label: string; className: string }
> = {
  ACTIVE: {
    label: 'Activo',
    className: 'bg-primary-100 text-primary-700 border-primary-300',
  },
  COMPLETED: {
    label: 'Completado',
    className: 'bg-success/15 text-success border-success/30',
  },
  CANCELLED: {
    label: 'Cancelado',
    className: 'bg-gray-100 text-gray-700 border-gray-300',
  },
};

export function ProcessStatusBadge({ status }: ProcessStatusBadgeProps) {
  const normalized = status?.toUpperCase() ?? 'UNKNOWN';
  const config = statusConfig[normalized] ?? {
    label: status || 'Desconocido',
    className: 'bg-gray-100 text-gray-700 border-gray-300',
  };

  return (
    <span
      className={`inline-flex rounded-full border px-3 py-1 text-label-md font-medium ${config.className}`}
    >
      {config.label}
    </span>
  );
}
