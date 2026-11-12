import { Select } from '../../../components/ui/Select';
import { PRIORITY_OPTIONS, STATUS_OPTIONS } from '../model/observationView';

interface ObservationFiltersProps {
  status: string;
  priority: string;
  evidenceId: string;
  authorId: string;
  evidences: { value: string; label: string }[];
  authors: { value: string; label: string }[];
  onStatus: (value: string) => void;
  onPriority: (value: string) => void;
  onEvidence: (value: string) => void;
  onAuthor: (value: string) => void;
}

export function ObservationFilters({
  status,
  priority,
  evidenceId,
  authorId,
  evidences,
  authors,
  onStatus,
  onPriority,
  onEvidence,
  onAuthor,
}: ObservationFiltersProps) {
  return (
    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      <Select
        id="observation-filter-status"
        label="Estado"
        options={[...STATUS_OPTIONS]}
        value={status}
        onChange={(event) => onStatus(event.target.value)}
      />
      <Select
        id="observation-filter-priority"
        label="Prioridad"
        options={[{ value: '', label: 'Todas las prioridades' }, ...PRIORITY_OPTIONS]}
        value={priority}
        onChange={(event) => onPriority(event.target.value)}
      />
      <Select
        id="observation-filter-evidence"
        label="Evidencia"
        options={[{ value: '', label: 'Todas las evidencias' }, ...evidences]}
        value={evidenceId}
        onChange={(event) => onEvidence(event.target.value)}
      />
      <Select
        id="observation-filter-author"
        label="Técnico"
        options={[{ value: '', label: 'Todos los técnicos' }, ...authors]}
        value={authorId}
        onChange={(event) => onAuthor(event.target.value)}
      />
    </div>
  );
}
