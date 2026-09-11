import { FileSearch, Search, X } from 'lucide-react';
import type { FormEvent } from 'react';
import type { PhaseDto } from '../../../api/model';
import { Button } from '../../../components/ui/Button';
import { Select } from '../../../components/ui/Select';
import { useEvidenceSearch } from '../hooks/useEvidenceSearch';

export type ProcessEvidenceSearchPanelProps = {
  processId?: string;
  programId?: string;
  phases?: PhaseDto[];
  onNavigateToSubphase?: (subphaseId: string) => void;
};

export function ProcessEvidenceSearchPanel({
  processId,
  programId,
  phases = [],
  onNavigateToSubphase,
}: ProcessEvidenceSearchPanelProps) {
  const {
    query,
    setQuery,
    phaseId,
    setPhaseId,
    subphaseId,
    setSubphaseId,
    aiEnabled,
    setAiEnabled,
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

  const handleRunScenario = (q: string, enableAi: boolean) => {
    setQuery(q);
    setAiEnabled(enableAi);
    void search({ q, aiEnabled: enableAi });
  };

  return (
    <div className="mb-6 rounded-xl border border-primary-100 bg-primary-50/50 p-4">
      <div className="mb-3 flex items-center justify-between gap-2">
        <div className="flex items-center gap-2 text-label-md font-semibold uppercase text-primary-700">
          <FileSearch size={16} aria-hidden />
          Buscar evidencias en este proceso
        </div>
        {aiEnabled && (
          <span className="rounded-full bg-primary-600 px-2.5 py-0.5 text-xs font-medium text-white">
            Modo IA MCP Activo
          </span>
        )}
      </div>

      <form className="grid gap-3 md:grid-cols-2 xl:grid-cols-4" onSubmit={handleSubmit}>
        <div className="md:col-span-2 xl:col-span-2">
          <label htmlFor="evidence-search-q" className="mb-1 block text-label-md text-gray-700">
            Texto o consulta natural (ej. infraestructuras, laboratorios, informes)
          </label>
          <input
            id="evidence-search-q"
            type="search"
            value={query}
            placeholder="Ej. 'aulas de clase', 'infraestructura', 'informe'…"
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

        <div className="flex items-center gap-2 md:col-span-2 xl:col-span-4">
          <input
            id="evidence-search-ai-toggle"
            type="checkbox"
            checked={aiEnabled}
            onChange={(e) => setAiEnabled(e.target.checked)}
            className="h-4 w-4 rounded border-gray-300 text-primary-600 focus:ring-primary-500"
          />
          <label htmlFor="evidence-search-ai-toggle" className="text-body-sm font-medium text-gray-700">
            Habilitar Asistente MCP IA (Cabecera <code className="bg-gray-100 px-1 font-mono text-xs">X-AI-Enabled</code> / Búsqueda por Sinónimos)
          </label>
        </div>

        <div className="flex flex-wrap items-end gap-2 md:col-span-2 xl:col-span-4">
          <Button type="submit" isLoading={isSearching}>
            <Search size={16} aria-hidden />
            Buscar
          </Button>
          {(hasSearched || query || phaseId || subphaseId || aiEnabled) && (
            <Button type="button" variant="ghost" onClick={reset}>
              <X size={16} aria-hidden />
              Limpiar
            </Button>
          )}
        </div>
      </form>

      <div className="mt-4 border-t border-gray-200/80 pt-3">
        <p className="mb-2 text-label-md font-semibold text-gray-700">Casos de Prueba (Demo)</p>
        <div className="flex flex-wrap gap-2">
          <button
            type="button"
            className="rounded-lg border border-gray-300 bg-body px-3 py-1 text-xs font-medium text-gray-700 hover:bg-gray-50"
            onClick={() => handleRunScenario('infraestructura', false)}
          >
            Escenario 1: "infraestructura" Coincidencia directa (No usa IA)
          </button>
          <button
            type="button"
            className="rounded-lg border border-primary-300 bg-primary-50 px-3 py-1 text-xs font-medium text-primary-800 hover:bg-primary-100"
            onClick={() => handleRunScenario('aulas de clase', true)}
          >
            Escenario 2: Búsqueda Multi-Token Mapea con MCP a subconjuntos
          </button>
          <button
            type="button"
            className="rounded-lg border border-gray-300 bg-body px-3 py-1 text-xs font-medium text-gray-700 hover:bg-gray-50"
            onClick={() => handleRunScenario('¿cómo hacer una pizza?', true)}
          >
            Escenario 3: "¿cómo hacer una pizza?" Fuera de alcance
          </button>
        </div>
      </div>

      {error && (
        <p className="mt-3 text-body-md text-danger" role="alert">
          {error}
        </p>
      )}

      {hasSearched && !error && (
        <div className="mt-4">
          {(results ?? []).length === 0 ? (
            <p className="rounded-md border border-gray-200 bg-body px-3 py-2 text-body-md text-gray-700">
              No se encontraron resultados. Pruebe ampliar filtros o usar otro término de búsqueda.
            </p>
          ) : (
            <>
              <p className="mb-2 text-label-md text-gray-600">
                {total} resultado{total === 1 ? '' : 's'}
                {aiEnabled ? ' (ampliados vía Asistente MCP IA / Sinónimos)' : ''}
              </p>
              <ul className="space-y-2">
                {(results ?? []).map((item) => (
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
