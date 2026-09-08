import { FileSearch, Search, X } from 'lucide-react';
import type { FormEvent } from 'react';
import type { NormativeLevel1NodeDto } from '../../../api/model';
import { Button } from '../../../components/ui/Button';
import { Select } from '../../../components/ui/Select';
import { useEvidenceSearch } from '../hooks/useEvidenceSearch';

export type ProcessEvidenceSearchPanelProps = {
  processId: string;
  programId?: string;
  level1Nodes: NormativeLevel1NodeDto[];
  onNavigateToIndicator?: (indicatorId: string) => void;
};

type IndicatorOption = {
  value: string;
  label: string;
};

function buildIndicatorOptions(
  level1Nodes: NormativeLevel1NodeDto[],
  level1Id: string,
): IndicatorOption[] {
  const sortedLevel1 = [...level1Nodes].sort((a, b) => (a.order ?? 0) - (b.order ?? 0));
  const scopedLevel1 = level1Id
    ? sortedLevel1.filter((node) => node.id === level1Id)
    : sortedLevel1;

  const options: IndicatorOption[] = [];
  for (const level1 of scopedLevel1) {
    for (const level2 of [...(level1.level2Nodes ?? [])].sort(
      (a, b) => (a.order ?? 0) - (b.order ?? 0),
    )) {
      for (const level3 of [...(level2.level3Nodes ?? [])].sort(
        (a, b) => (a.order ?? 0) - (b.order ?? 0),
      )) {
        for (const indicator of [...(level3.indicators ?? [])].sort(
          (a, b) => (a.order ?? 0) - (b.order ?? 0),
        )) {
          if (!indicator.id) continue;
          options.push({
            value: indicator.id,
            label: `${indicator.code ?? 'IND'} · ${indicator.description ?? 'Indicador'}`,
          });
        }
      }
    }
  }
  return options;
}

export function ProcessEvidenceSearchPanel({
  processId,
  programId,
  level1Nodes,
  onNavigateToIndicator,
}: ProcessEvidenceSearchPanelProps) {
  const {
    query,
    setQuery,
    level1Id,
    setLevel1Id,
    indicatorId,
    setIndicatorId,
    results,
    total,
    isSearching,
    hasSearched,
    error,
    search,
    reset,
  } = useEvidenceSearch({ processId, programId });

  const sortedLevel1 = [...level1Nodes].sort((a, b) => (a.order ?? 0) - (b.order ?? 0));
  const level1Options = [
    { value: '', label: 'Todos los nodos N1' },
    ...sortedLevel1.map((node) => ({
      value: node.id ?? '',
      label: node.name ?? 'N1',
    })),
  ];

  const indicatorOptions = [
    { value: '', label: 'Todos los indicadores' },
    ...buildIndicatorOptions(level1Nodes, level1Id).map((item) => ({
      value: item.value,
      label: item.label,
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
            label="Nivel 1"
            id="evidence-search-level1"
            value={level1Id}
            options={level1Options}
            onChange={(event) => setLevel1Id(event.target.value)}
          />
        </div>

        <div>
          <Select
            label="Indicador"
            id="evidence-search-indicator"
            value={indicatorId}
            options={indicatorOptions}
            onChange={(event) => setIndicatorId(event.target.value)}
          />
        </div>

        <div className="flex flex-wrap items-end gap-2 md:col-span-2 xl:col-span-4">
          <Button type="submit" isLoading={isSearching}>
            <Search size={16} aria-hidden />
            Buscar
          </Button>
          {(hasSearched || query || level1Id || indicatorId) && (
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
                          {item.level1Name ?? '—'} · {item.level3Name ?? '—'}
                          {item.indicatorCode ? ` · ${item.indicatorCode}` : ''}
                          {' · '}v{item.version}
                          {!item.blobAvailable ? ' · solo metadatos' : ''}
                        </p>
                      </div>
                      {item.indicatorId && onNavigateToIndicator && (
                        <button
                          type="button"
                          className="shrink-0 text-body-md font-medium text-primary-600 underline decoration-primary-400 underline-offset-2 hover:text-primary-800"
                          onClick={() => onNavigateToIndicator(item.indicatorId!)}
                        >
                          Ir al indicador
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
