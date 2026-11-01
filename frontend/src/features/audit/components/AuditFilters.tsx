import { Button } from '../../../components/ui/Button';
import { Select } from '../../../components/ui/Select';
import { TextInput } from '../../../components/ui/TextInput';
import type { AuditHistoryView } from '../historyRoutes';
import {
  ACCOUNT_ACTION_OPTIONS,
  ACTION_OPTIONS,
  EVIDENCE_ACTION_OPTIONS,
  RESOURCE_OPTIONS,
  RESULT_OPTIONS,
} from '../lib/auditDisplay';
import type { AuditFilterValues } from '../lib/auditFilters';

interface AuditFiltersProps {
  viewId: AuditHistoryView['id'];
  values: AuditFilterValues;
  rangeError: string | null;
  onChange: (values: AuditFilterValues) => void;
  onSearch: () => void;
  onClear: () => void;
}

export function AuditFilters({
  viewId,
  values,
  rangeError,
  onChange,
  onSearch,
  onClear,
}: AuditFiltersProps) {
  const patch = (partial: Partial<AuditFilterValues>) => onChange({ ...values, ...partial });
  const actionOptions = viewId === 'evidence'
    ? EVIDENCE_ACTION_OPTIONS
    : viewId === 'accounts'
      ? ACCOUNT_ACTION_OPTIONS
      : ACTION_OPTIONS;

  return (
    <form
      className="rounded-2xl border border-gray-100 bg-body p-6 shadow-sm"
      data-testid="audit-filters"
      onSubmit={(event) => {
        event.preventDefault();
        onSearch();
      }}
    >
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {viewId !== 'user' && (
          <TextInput
            label="Usuario"
            type="email"
            value={values.actorEmail}
            placeholder="correo@umss.edu.bo"
            onChange={(event) => patch({ actorEmail: event.target.value })}
          />
        )}
        {viewId !== 'access' && (
          <Select
            label="Acción"
            value={values.action}
            options={[{ value: '', label: 'Todas' }, ...actionOptions]}
            onChange={(event) => patch({ action: event.target.value })}
          />
        )}
        {viewId === 'general' && (
          <Select
            label="Módulo"
            value={values.resourceType}
            options={[{ value: '', label: 'Todos' }, ...RESOURCE_OPTIONS]}
            onChange={(event) => patch({ resourceType: event.target.value })}
          />
        )}
        <Select
          label="Resultado"
          value={values.result}
          options={[{ value: '', label: 'Todos' }, ...RESULT_OPTIONS]}
          onChange={(event) => patch({ result: event.target.value })}
        />
        <TextInput
          label="Fecha desde"
          type="date"
          value={values.from}
          onChange={(event) => patch({ from: event.target.value })}
        />
        <TextInput
          label="Fecha hasta"
          type="date"
          value={values.to}
          error={rangeError ?? undefined}
          onChange={(event) => patch({ to: event.target.value })}
        />
      </div>
      <div className="mt-4 flex flex-wrap gap-3">
        <Button type="submit" data-testid="audit-search">Buscar</Button>
        <Button type="button" variant="ghost" onClick={onClear} data-testid="audit-clear">
          Limpiar filtros
        </Button>
      </div>
    </form>
  );
}
