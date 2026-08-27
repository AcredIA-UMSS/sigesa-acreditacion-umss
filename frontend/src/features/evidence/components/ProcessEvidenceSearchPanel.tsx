import { FileSearch, Search, X } from 'lucide-react';
import type { FormEvent } from 'react';
import type { PhaseDto } from '../../../api/model';
import { Button } from '../../../components/ui/Button';
import { Select } from '../../../components/ui/Select';
import { useEvidenceSearch } from '../hooks/useEvidenceSearch';

export type ProcessEvidenceSearchPanelProps = {
  processId: string;
  programId?: string;
  phases: PhaseDto[];
  onNavigateToSubphase?: (subphaseId: string) => void;
};

export function ProcessEvidenceSearchPanel({
  processId,
  programId,
  phases,
  onNavigateToSubphase,
}: ProcessEvidenceSearchPanelProps) {
  const {
    query,
    setQuery,
    phaseId,
    setPhaseId,
    subphaseId,
    setSubphaseId,
    results,
    total,
    isSearching,
    hasSearched,
    error,
    search,
    reset,
  } = useEvidenceSearch({ processId, programId });

  const sortedPhases = [...phases].sort((a, b) => (a.order ?? 0) - (b.order ?? 0));
  const selectedPhase = sortedPhases.find((phase) => phase.id === phaseId);
  const subphases = [...(selectedPhase?.subphases ?? [])].sort(
    (a, b) => (a.order ?? 0) - (b.order ?? 0),
  );

  const phaseOptions = [
    { value: '', label: 'Todas las fases' },
    ...sortedPhases.map((phase) => ({
      value: phase.id ?? '',
      label: phase.name ?? 'Fase',
    })),
  ];

  const subphaseOptions = [
    { value: '', label: 'Todas las subfases' },
    ...subphases.map((sub) => ({
      value: sub.id ?? '',
      label: sub.name ?? 'Subfase',
    })),
  ];

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault();
    void search();
  };

  return (
    <div className="mb-6 rounded-xl border border-primary-100 bg-primary-50/50 p-4">
      <div className="mb-3 flex items-center gap-2 text-label-md font-semibold uppercase text-primary-700">
        <FileSearch size={16} aria-hidden />
        Buscar evidencias en este proceso
      </div>

      <form className="grid gap-3 md:grid-cols-2 xl:grid-cols-4" onSubmit={handleSubmit}>
        <div className="md:col-span-2 xl:col-span-2">
          <label htmlFor="evidence-search-q" className="mb-1 block text-label-md text-gray-700">
            Texto (nombre archivo, descripción, indicador…)
          </label>
          <input
            id="evidence-search-q"
            type="search"
            value={query}
            placeholder="Ej. informe, plan de estudios, PDF…"
            className="w-full rounded-lg border border-gray-300 bg-body px-3 py-2 text-body-md text-gray-900 outline-none focus:border-primary-500"
            onChange={(event) => setQuery(event.target.value)}
          />
        </div>

        <div>
          <Select
            label="Fase"
            id="evidence-search-phase"
            value={phaseId}
            options={phaseOptions}
            onChange={(event) => setPhaseId(event.target.value)}
          />
        </div>

        <div>
          <Select
            label="Subfase"
            id="evidence-search-subphase"
            value={subphaseId}
            disabled={!phaseId}
            options={subphaseOptions}
            onChange={(event) => setSubphaseId(event.target.value)}
          />
        </div>

        <div className="flex flex-wrap items-end gap-2 md:col-span-2 xl:col-span-4">
          <Button type="submit" isLoading={isSearching}>
            <Search size={16} aria-hidden />
            Buscar
          </Button>
          {(hasSearched || query || phaseId || subphaseId) && (
            <Button type="button" variant="ghost" onClick={reset}>
              <X size={16} aria-hidden />
              Limpiar
            </Button>
          )}
        </div>
      </form>

      {error && (
        <p className="mt-3 text-body-md text-danger" role="alert">
          {error}
        </p>
      )}

      {hasSearched && !error && (
        <div className="mt-4">
          {results.length === 0 ? (
            <p className="rounded-md border border-gray-200 bg-body px-3 py-2 text-body-md text-gray-700">
              No se encontraron resultados. Pruebe ampliar filtros o usar otro término de búsqueda.
            </p>
          ) : (
            <>
              <p className="mb-2 text-label-md text-gray-600">
                {total} resultado{total === 1 ? '' : 's'}
              </p>
              <ul className="space-y-2">
                {results.map((item) => (
                  <li
                    key={item.evidenceId}
                    className="rounded-md border border-gray-200 bg-body px-3 py-2"
                  >
                    <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
                      <div>
                        <p className="font-medium text-gray-900">
                          {item.originalFilename ?? 'Evidencia sin nombre'}
                        </p>
                        <p className="text-body-md text-gray-600">{item.description}</p>
                        <p className="mt-1 text-label-md text-gray-500">
                          {item.phaseName ?? '—'} · {item.subphaseName ?? '—'}
                          {item.indicatorCode ? ` · ${item.indicatorCode}` : ''}
                          {' · '}v{item.version}
                          {!item.blobAvailable ? ' · solo metadatos' : ''}
                        </p>
                      </div>
                      {item.subphaseId && onNavigateToSubphase && (
                        <button
                          type="button"
                          className="shrink-0 text-body-md font-medium text-primary-600 underline decoration-primary-400 underline-offset-2 hover:text-primary-800"
                          onClick={() => onNavigateToSubphase(item.subphaseId!)}
                        >
                          Ir a subfase
                        </button>
                      )}
                    </div>
                  </li>
                ))}
              </ul>
            </>
          )}
        </div>
      )}
    </div>
  );
}
