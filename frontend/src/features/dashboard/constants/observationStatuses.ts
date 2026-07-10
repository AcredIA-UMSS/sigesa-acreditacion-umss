/** Valores de estado usados por el backend en observaciones (JpaDashboardQueryAdapter / seed). */
export const OBSERVATION_STATUS_OPTIONS = [
  { value: 'PENDING_REMEDIATION', label: 'Pendiente subsanación' },
  { value: 'PENDING_SUBSANACION', label: 'Pendiente subsanación (ES)' },
  { value: 'EN_REVISION_TECNICA', label: 'En revisión técnica' },
  { value: 'APROBADO', label: 'Aprobado' },
  { value: 'RECHAZADO', label: 'Rechazado' },
] as const;

export function getObservationStatusLabel(status: string): string {
  const match = OBSERVATION_STATUS_OPTIONS.find((option) => option.value === status);
  return match?.label ?? status.replace(/_/g, ' ');
}

export function getObservationStatusTone(
  status: string
): 'pending' | 'review' | 'approved' | 'rejected' | 'neutral' {
  if (status === 'PENDING_REMEDIATION' || status === 'PENDING_SUBSANACION') {
    return 'pending';
  }
  if (status === 'EN_REVISION_TECNICA' || status === 'EN_REVISION') {
    return 'review';
  }
  if (status === 'APROBADO') {
    return 'approved';
  }
  if (status === 'RECHAZADO') {
    return 'rejected';
  }
  return 'neutral';
}

export const OBSERVATION_STATUS_BADGE: Record<
  ReturnType<typeof getObservationStatusTone>,
  string
> = {
  pending: 'bg-secondary-50 text-secondary-600 border-secondary-200',
  review: 'bg-warning/10 text-amber-800 border-warning/20',
  approved: 'bg-success/10 text-success border-success/20',
  rejected: 'bg-secondary-50 text-secondary-700 border-secondary-200',
  neutral: 'bg-gray-100 text-gray-700 border-gray-200',
};
