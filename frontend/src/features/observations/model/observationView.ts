import type { EvidenceOption, ProcessObservation } from '../hooks/useProcessObservations';

export const KIND_OPTIONS = [
  { value: 'FORMA', label: 'Forma' },
  { value: 'CONTENIDO', label: 'Contenido' },
  { value: 'CUMPLIMIENTO', label: 'Cumplimiento' },
] as const;

export const PRIORITY_OPTIONS = [
  { value: 'BAJA', label: 'Baja' },
  { value: 'MEDIA', label: 'Media' },
  { value: 'ALTA', label: 'Alta' },
] as const;

export const COMPLIANCE_OPTIONS = [
  { value: 'CUMPLE', label: 'Cumple' },
  { value: 'CUMPLE_PARCIAL', label: 'Cumple parcialmente' },
  { value: 'NO_CUMPLE', label: 'No cumple' },
] as const;

export const STATUS_OPTIONS = [
  { value: '', label: 'Todos los estados' },
  { value: 'BORRADOR', label: 'Borrador' },
  { value: 'PENDIENTES', label: 'Pendientes de atención' },
  { value: 'ENVIADA', label: 'Enviada' },
  { value: 'EN_REVISION', label: 'En revisión' },
  { value: 'RESPONDIDA', label: 'Respondida' },
  { value: 'ATENDIDA', label: 'Atendida' },
  { value: 'CERRADA', label: 'Cerrada' },
] as const;

const PENDING_STATUSES = new Set(['ENVIADA', 'EN_REVISION', 'RESPONDIDA']);

const STATUS_CLASS: Record<string, string> = {
  BORRADOR: 'border-gray-300 bg-gray-100 text-gray-700',
  ENVIADA: 'border-primary-300 bg-primary-50 text-primary-700',
  EN_REVISION: 'border-info/30 bg-info/10 text-info',
  RESPONDIDA: 'border-warning/40 bg-warning/20 text-gray-800',
  ATENDIDA: 'border-success/30 bg-success/15 text-success',
  CERRADA: 'border-gray-400 bg-gray-200 text-gray-800',
};

const PRIORITY_CLASS: Record<string, string> = {
  ALTA: 'border-danger/30 bg-danger/10 text-danger',
  MEDIA: 'border-warning/40 bg-warning/20 text-gray-800',
  BAJA: 'border-gray-300 bg-gray-50 text-gray-700',
};

const COMPLIANCE_CLASS: Record<string, string> = {
  CUMPLE: 'border-success/30 bg-success/15 text-success',
  CUMPLE_PARCIAL: 'border-warning/40 bg-warning/20 text-gray-800',
  NO_CUMPLE: 'border-danger/30 bg-danger/10 text-danger',
};

export interface ObservationCounts {
  drafts: number;
  pending: number;
  attended: number;
  closed: number;
}

export interface ObservationFilters {
  status: string;
  priority: string;
  evidenceId: string;
  authorId: string;
}

export function labelOf(options: readonly { value: string; label: string }[], value: string): string {
  return options.find((option) => option.value === value)?.label ?? value;
}

export function statusClass(status: string): string {
  return STATUS_CLASS[status] ?? 'border-gray-300 bg-gray-100 text-gray-700';
}

export function priorityClass(priority: string): string {
  return PRIORITY_CLASS[priority] ?? 'border-gray-300 bg-gray-100 text-gray-700';
}

export function complianceClass(compliance: string): string {
  return COMPLIANCE_CLASS[compliance] ?? 'border-gray-300 bg-gray-100 text-gray-700';
}

export function roleLabel(role: string): string {
  if (role === 'TD') return 'Técnico';
  if (role === 'CC') return 'Coordinador';
  return role;
}

export function evidenceLabel(option: EvidenceOption | undefined): string {
  if (!option) return 'Evidencia sin indicador';
  const code = option.code ?? 'Indicador';
  const title = option.title ?? 'Sin título';
  return `${code} — ${title}`;
}

export function technicianLabel(authorId: string): string {
  return `Técnico · ${authorId.slice(0, 8)}`;
}

export function countObservations(items: ProcessObservation[]): ObservationCounts {
  return items.reduce<ObservationCounts>(
    (counts, item) => {
      if (item.status === 'BORRADOR') counts.drafts += 1;
      if (PENDING_STATUSES.has(item.status)) counts.pending += 1;
      if (item.status === 'ATENDIDA') counts.attended += 1;
      if (item.status === 'CERRADA') counts.closed += 1;
      return counts;
    },
    { drafts: 0, pending: 0, attended: 0, closed: 0 },
  );
}

export function matchesFilters(item: ProcessObservation, filters: ObservationFilters): boolean {
  if (filters.priority && item.priority !== filters.priority) return false;
  if (filters.evidenceId && item.evidenceId !== filters.evidenceId) return false;
  if (filters.authorId && item.authorId !== filters.authorId) return false;
  if (!filters.status) return true;
  if (filters.status === 'PENDIENTES') return PENDING_STATUSES.has(item.status);
  return item.status === filters.status;
}

export function formatWhen(iso: string): string {
  try {
    return new Intl.DateTimeFormat('es-BO', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(iso));
  } catch {
    return iso;
  }
}
