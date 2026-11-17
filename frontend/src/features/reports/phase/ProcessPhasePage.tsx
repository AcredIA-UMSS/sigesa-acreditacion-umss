import { useEffect, useMemo, useState } from 'react';
import { Sidebar } from '../../../components/layout/Sidebar';
import { Select } from '../../../components/ui/Select';
import { Alert } from '../../../components/ui/Alert';
import { useListStages } from '../../../api/endpoints/stage-workflow/stage-workflow';
import type { ProcessSummaryResponseDto } from '../../../api/model';
import { isApiError } from '../../../lib/api/apiError';
import { useProcessList } from '../../processes/hooks/useProcessList';
import { ProcessEvidenceTrace } from '../../observations/components/ProcessEvidenceTrace';
import { ProcessPhaseChart } from './ProcessPhaseChart';

function processForCareer(processes: ProcessSummaryResponseDto[], careerId: string): ProcessSummaryResponseDto | undefined {
  const matches = processes.filter((process) => process.careerId === careerId && process.id);
  return matches.find((process) => process.status === 'ACTIVE') ?? matches[0];
}

export function ProcessPhasePage() {
  const { processes, isLoading, isError, errorMessage } = useProcessList();
  const [careerId, setCareerId] = useState('');

  const careers = useMemo(() => {
    const byId = new Map<string, string>();
    for (const process of processes) {
      if (!process.careerId) continue;
      byId.set(process.careerId, process.careerName ?? process.careerCode ?? 'Carrera sin nombre');
    }
    return [...byId.entries()]
      .map(([value, label]) => ({ value, label }))
      .sort((left, right) => left.label.localeCompare(right.label, 'es'));
  }, [processes]);

  useEffect(() => {
    if (!careerId && careers[0]) {
      setCareerId(careers[0].value);
    }
  }, [careerId, careers]);

  const selected = processForCareer(processes, careerId);
  const stagesQuery = useListStages(selected?.id ?? '00000000-0000-0000-0000-000000000000', {
    query: { enabled: Boolean(selected?.id) },
  });
  const stages = stagesQuery.data?.status === 200 ? stagesQuery.data.data : [];
  const stagesError = stagesQuery.isError
    ? (isApiError(stagesQuery.error) ? stagesQuery.error.message : 'No se pudieron cargar las fases.')
    : undefined;

  return (
    <div className="flex h-screen bg-gray-50" data-testid="process-phase-page">
      <Sidebar activeNav="reports" />
      <main className="flex-1 overflow-y-auto px-4 py-6 md:px-8">
        <header className="mb-6">
          <h1 className="text-heading-xl font-semibold text-primary-800">Seguimiento de procesos</h1>
          <p className="mt-1 text-body-md text-gray-600">
            Elija una carrera para ver la fase registrada y la secuencia de autoevaluación ARCU-SUR.
          </p>
        </header>

        {isError && errorMessage && (
          <div className="mb-4"><Alert variant="error">{errorMessage}</Alert></div>
        )}

        <form className="mb-8 max-w-xl rounded-2xl border border-gray-200 bg-body p-4">
          <Select
            id="phase-career"
            label="Carrera"
            value={careerId}
            disabled={isLoading || careers.length === 0}
            options={careers.length === 0
              ? [{ value: '', label: isLoading ? 'Cargando carreras…' : 'No hay carreras con proceso' }]
              : careers}
            onChange={(event) => setCareerId(event.target.value)}
          />
        </form>

        {selected?.id && (
          <section className="mb-8 rounded-2xl border border-gray-200 bg-body p-6">
            <h2 className="text-heading-md font-semibold text-primary-800">Evidencias del coordinador</h2>
            <p className="mt-1 mb-4 text-body-md text-gray-600">
              Archivos que el coordinador envió con Subir evidencia. Visualícelos y registre la revisión.
            </p>
            <ProcessEvidenceTrace processId={selected.id} mode="technician" />
          </section>
        )}

        {selected && (
          <div className="rounded-2xl border border-gray-200 bg-body p-6">
            <ProcessPhaseChart
              processId={selected.id ?? ''}
              careerName={selected.careerName ?? 'La carrera'}
              stages={stages}
              isLoading={stagesQuery.isLoading}
              errorMessage={stagesError}
            />
          </div>
        )}

        {!isLoading && careers.length === 0 && (
          <p className="text-body-md text-gray-600">No hay procesos de acreditación en su alcance.</p>
        )}
      </main>
    </div>
  );
}
