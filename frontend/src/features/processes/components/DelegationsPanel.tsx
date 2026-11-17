import { useState } from 'react';
import { Button } from '../../../components/ui/Button';
import { Select } from '../../../components/ui/Select';

export type DelegationView = {
  id: string;
  userId?: string;
  userLabel?: string;
  scopeLabel: string;
};

export type SelectOption = {
  value: string;
  label: string;
};

export type DelegationsPanelProps = {
  items: DelegationView[];
  caeOptions: SelectOption[];
  level1Options: SelectOption[];
  level2Options: SelectOption[];
  indicatorOptions: SelectOption[];
  canManage: boolean;
  isLoading: boolean;
  isSaving: boolean;
  errorMessage?: string;
  onCreate: (draft: {
    userId: string;
    level1Id?: string;
    level2Id?: string;
    indicatorId?: string;
  }) => void;
  onDelete: (delegationId: string) => void;
};

export function DelegationsPanel({
  items,
  caeOptions,
  level1Options,
  level2Options,
  indicatorOptions,
  canManage,
  isLoading,
  isSaving,
  errorMessage,
  onCreate,
  onDelete,
}: DelegationsPanelProps) {
  const [userId, setUserId] = useState('');
  const [scope, setScope] = useState<'LEVEL1' | 'LEVEL2' | 'INDICATOR'>('INDICATOR');
  const [level1Id, setLevel1Id] = useState('');
  const [level2Id, setLevel2Id] = useState('');
  const [indicatorId, setIndicatorId] = useState('');

  if (!canManage) {
    return null;
  }

  return (
    <section className="rounded-2xl border border-gray-200 bg-body p-6 shadow-sm">
      <h2 className="text-heading-lg font-semibold text-primary-800">
        Delegaciones CAE
      </h2>
      <p className="mt-1 text-body-md text-gray-600">
        El Coordinador asigna a un miembro CAE un único alcance: dimensión (N1), área (N2)
        o indicador.
      </p>

      {errorMessage && <p className="mt-3 text-body-md text-danger">{errorMessage}</p>}

      {isLoading ? (
        <p className="mt-3 text-body-md text-gray-600">Cargando delegaciones…</p>
      ) : items.length === 0 ? (
        <p className="mt-3 text-body-md text-gray-500">No hay delegaciones registradas.</p>
      ) : (
        <ul className="mt-4 space-y-2">
          {items.map((item) => (
            <li
              key={item.id}
              className="flex flex-col gap-2 rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 sm:flex-row sm:items-center sm:justify-between"
            >
              <div>
                <p className="text-body-md font-medium text-gray-800">
                  {item.userLabel ?? item.userId}
                </p>
                <p className="text-label-md text-gray-500">{item.scopeLabel}</p>
              </div>
              <Button variant="ghost" onClick={() => onDelete(item.id)} isLoading={isSaving}>
                Revocar
              </Button>
            </li>
          ))}
        </ul>
      )}

      <form
        className="mt-6 grid gap-3 rounded-xl border border-primary-100 bg-primary-50 p-4 md:grid-cols-2"
        onSubmit={(event) => {
          event.preventDefault();
          if (!userId) {
            return;
          }
          onCreate({
            userId,
            level1Id: scope === 'LEVEL1' ? level1Id || undefined : undefined,
            level2Id: scope === 'LEVEL2' ? level2Id || undefined : undefined,
            indicatorId: scope === 'INDICATOR' ? indicatorId || undefined : undefined,
          });
        }}
      >
        <Select
          label="Miembro CAE"
          requiredMark
          value={userId}
          onChange={(event) => setUserId(event.target.value)}
          options={[{ value: '', label: 'Seleccione…' }, ...caeOptions]}
        />
        <Select
          label="Alcance"
          value={scope}
          onChange={(event) =>
            setScope(event.target.value as 'LEVEL1' | 'LEVEL2' | 'INDICATOR')}
          options={[
            { value: 'INDICATOR', label: 'Indicador' },
            { value: 'LEVEL2', label: 'Área (N2)' },
            { value: 'LEVEL1', label: 'Dimensión (N1)' },
          ]}
        />
        {scope === 'LEVEL1' && (
          <Select
            label="Dimensión N1"
            requiredMark
            value={level1Id}
            onChange={(event) => setLevel1Id(event.target.value)}
            options={[{ value: '', label: 'Seleccione…' }, ...level1Options]}
          />
        )}
        {scope === 'LEVEL2' && (
          <Select
            label="Área N2"
            requiredMark
            value={level2Id}
            onChange={(event) => setLevel2Id(event.target.value)}
            options={[{ value: '', label: 'Seleccione…' }, ...level2Options]}
          />
        )}
        {scope === 'INDICATOR' && (
          <Select
            label="Indicador"
            requiredMark
            value={indicatorId}
            onChange={(event) => setIndicatorId(event.target.value)}
            options={[{ value: '', label: 'Seleccione…' }, ...indicatorOptions]}
          />
        )}
        <div className="flex items-end">
          <Button type="submit" variant="primary" isLoading={isSaving}>
            Delegar
          </Button>
        </div>
      </form>
    </section>
  );
}
