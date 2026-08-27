import { useCallback, useRef } from 'react';
import { Link } from 'react-router-dom';
import { ArrowLeft, Pencil, RefreshCw } from 'lucide-react';
import { Button } from '../../../components/ui/Button';
import { useAuth } from '../../../lib/auth/useAuth';
import { useProcessDetail } from '../hooks/useProcessDetail';
import { ProcessEvidenceSearchPanel } from '../../evidence/components/ProcessEvidenceSearchPanel';
import { ProcessPhaseTree } from './ProcessPhaseTree';
import { PhasesCopilotPanel } from './PhasesCopilotPanel';
import { ProcessResponsibleContainer } from './ProcessResponsibleContainer';
import { ProcessStatusBadge } from './ProcessStatusBadge';

interface ProcessDetailViewProps {
  processId: string;
}

function formatDate(iso?: string): string {
  if (!iso) return '—';
  try {
    return new Intl.DateTimeFormat('es-BO', {
      dateStyle: 'long',
      timeStyle: 'short',
    }).format(new Date(iso));
  } catch {
    return iso;
  }
}

export function ProcessDetailView({ processId }: ProcessDetailViewProps) {
  const { session } = useAuth();
  const { process, isLoading, isError, isNotFound, errorMessage, refetch } =
    useProcessDetail(processId);
  const canEditStructure =
    (session?.role === 'JD' || session?.role === 'TD') && process?.status === 'ACTIVE';
  const canUseCopilot =
    session?.role === 'JD' || session?.role === 'TD' || session?.role === 'CC';
  const copilotReadOnly = session?.role === 'CC';
  const canUploadEvidence = session?.role === 'CC';
  const canObserveEvidence = session?.role === 'JD' || session?.role === 'TD';
  const canReviewEvidence = session?.role === 'TD';
  const subphaseAnchorRef = useRef<HTMLDivElement>(null);

  const navigateToSubphase = useCallback((subphaseId: string) => {
    const element = document.getElementById(`subphase-${subphaseId}`);
    element?.scrollIntoView({ behavior: 'smooth', block: 'center' });
    element?.classList.add('ring-2', 'ring-primary-400');
    window.setTimeout(() => {
      element?.classList.remove('ring-2', 'ring-primary-400');
    }, 2000);
  }, []);

  return (
    <div className="space-y-6" ref={subphaseAnchorRef}>
      <div className="flex flex-wrap items-center justify-between gap-4">
        <Link
          to="/procesos"
          className="inline-flex items-center gap-2 text-body-md font-medium text-primary-600 hover:text-primary-800"
        >
          <ArrowLeft size={18} />
          Volver al listado
        </Link>
        <Button variant="ghost" onClick={refetch} isLoading={isLoading}>
          <RefreshCw size={16} />
          Actualizar
        </Button>
      </div>

      {isLoading && (
        <div className="rounded-2xl border border-gray-200 bg-body p-12 text-center">
          <p className="text-body-md text-gray-600">Cargando detalle del proceso…</p>
        </div>
      )}

      {isError && !isLoading && (
        <div
          className={`rounded-2xl border p-6 ${
            isNotFound
              ? 'border-gray-300 bg-gray-50'
              : 'border-danger/30 bg-danger/10'
          }`}
        >
          <p className={`text-body-md ${isNotFound ? 'text-gray-700' : 'text-danger'}`}>
            {errorMessage}
          </p>
          {!isNotFound && (
            <Button className="mt-4" variant="secondary" onClick={refetch}>
              Reintentar
            </Button>
          )}
        </div>
      )}

      {!isLoading && !isError && process && (
        <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_340px] xl:items-start">
          <div className="space-y-6">
          <section className="rounded-2xl border border-primary-200/40 bg-gradient-to-r from-primary-600 to-primary-500 p-6 text-body shadow-md">
            <div className="flex flex-col justify-between gap-4 md:flex-row md:items-start">
              <div>
                <p className="text-label-md uppercase tracking-wide text-primary-100">
                  {process.templateType ?? 'Plantilla'}
                </p>
                <h1 className="mt-1 text-heading-xl font-bold">{process.careerName ?? 'Carrera'}</h1>
                <p className="mt-1 text-body-md text-primary-100">
                  {process.careerCode ?? '—'} · {process.templateName ?? '—'}
                </p>
              </div>
              <ProcessStatusBadge status={process.status ?? 'UNKNOWN'} />
            </div>
            <p className="mt-4 text-body-md text-primary-100">
              Inicio: {formatDate(process.startDate)}
            </p>
          </section>

          <ProcessResponsibleContainer
            processId={processId}
            process={process}
            onUpdated={refetch}
          />

          <section className="rounded-2xl border border-gray-200 bg-body p-6 shadow-sm">
            <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h2 className="text-heading-lg font-semibold text-primary-800">
                  Estructura del proceso
                </h2>
                <p className="mt-1 text-body-md text-gray-600">
                  Fases y subfases con requisitos de completitud. En cada subfase puede
                  cargar una o más evidencias y el equipo técnico puede registrar observaciones.
                </p>
              </div>
              {canEditStructure && (
                <Link to={`/procesos/${processId}/estructura`}>
                  <Button variant="secondary">
                    <Pencil size={16} />
                    Editar estructura
                  </Button>
                </Link>
              )}
            </div>
            <ProcessEvidenceSearchPanel
              processId={processId}
              programId={process.careerId}
              phases={process.phases ?? []}
              onNavigateToSubphase={navigateToSubphase}
            />
            <ProcessPhaseTree
              phases={process.phases ?? []}
              processId={processId}
              canUploadEvidence={canUploadEvidence}
              canObserveEvidence={canObserveEvidence}
              canReviewEvidence={canReviewEvidence}
              canSubsanateEvidence={canUploadEvidence}
              canClosePhase={canReviewEvidence}
              onStructureUpdated={refetch}
              onNavigateToSubphase={navigateToSubphase}
            />
          </section>
          </div>

          {canUseCopilot && (
            <PhasesCopilotPanel
              readOnly={copilotReadOnly}
              process={{
                processId,
                careerName: process.careerName ?? 'Carrera',
                careerCode: process.careerCode ?? '—',
                templateType: process.templateType ?? 'CEUB',
              }}
            />
          )}
        </div>
      )}
    </div>
  );
}
