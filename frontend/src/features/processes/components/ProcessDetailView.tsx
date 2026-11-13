import { useCallback, useEffect, useRef, useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { ArrowLeft, Download, Lock, Pencil, RefreshCw } from 'lucide-react';
import {
  getGetProcessQueryKey,
  getListProcessesQueryKey,
  useCloseProcess,
} from '../../../api/endpoints/procesos-de-acreditación/procesos-de-acreditación';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog';
import { getApiErrorMessage } from '../../../lib/api/mapApiError';
import { useAuth } from '../../../lib/auth/useAuth';
import { useDossierExport } from '../hooks/useDossierExport';
import { useProcessDetail } from '../hooks/useProcessDetail';
import { ProcessEvidenceSearchPanel } from '../../evidence/components/ProcessEvidenceSearchPanel';
import { ProcessNormativeTree } from './ProcessNormativeTree';
import { ProcessResponsibleContainer } from './ProcessResponsibleContainer';
import { EvaluationMetricsPanelContainer } from './EvaluationMetricsPanelContainer';
import { ProcessStatusBadge } from './ProcessStatusBadge';
import { ProcessOperationalModeBadge } from './ProcessOperationalModeBadge';
import { ObservationsPanel } from '../../observations/components/ObservationsPanel';

interface ProcessDetailViewProps {
  processId: string;
  /** Deep-link target from notifications: /procesos/:id/indicadores/:indicatorId */
  initialIndicatorId?: string;
  initialObservationId?: string;
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

export function ProcessDetailView({ processId, initialIndicatorId, initialObservationId }: ProcessDetailViewProps) {
  const { session } = useAuth();
  const queryClient = useQueryClient();
  const { process, isLoading, isError, isNotFound, errorMessage, refetch } =
    useProcessDetail(processId);
  const [closeDialogOpen, setCloseDialogOpen] = useState(false);
  const [closeError, setCloseError] = useState<string | null>(null);
  const { mutateAsync: closeProcess, isPending: isClosing } = useCloseProcess({
    mutation: {
      onSuccess: async () => {
        await Promise.all([
          queryClient.invalidateQueries({ queryKey: getGetProcessQueryKey(processId) }),
          queryClient.invalidateQueries({ queryKey: getListProcessesQueryKey() }),
        ]);
      },
    },
  });
  const operationalMode = process?.operationalMode ?? 'ACTIVE';
  const isAuditFrozen = operationalMode === 'AUDIT_READONLY';
  const canEditStructure =
    (session?.role === 'JD' || session?.role === 'TD') && process?.status === 'ACTIVE';
  const canUploadEvidence =
    (session?.role === 'CC' || session?.role === 'CAE_MEMBER') && !isAuditFrozen;
  const canEditFoda =
    (session?.role === 'CC' || session?.role === 'CAE_MEMBER') && !isAuditFrozen;
  const canReviewEvidence = session?.role === 'TD';
  const canSeeObservations = session?.role === 'TD' || session?.role === 'CC';
  const canCloseProcess =
    (session?.role === 'JD' || session?.role === 'TD' || session?.role === 'EE') &&
    process?.status === 'ACTIVE';
  const structureAnchorRef = useRef<HTMLDivElement>(null);
  const [expandToIndicatorId, setExpandToIndicatorId] = useState<string | undefined>(
    initialIndicatorId,
  );

  const navigateToIndicator = useCallback((indicatorId: string) => {
    setExpandToIndicatorId(indicatorId);
    window.setTimeout(() => {
      const element = document.getElementById(`indicator-${indicatorId}`);
      element?.scrollIntoView({ behavior: 'smooth', block: 'center' });
      element?.classList.add('ring-2', 'ring-primary-400');
      window.setTimeout(() => {
        element?.classList.remove('ring-2', 'ring-primary-400');
      }, 2000);
    }, 150);
  }, []);

  // Deep-link from notifications: expand + scroll once the process tree is loaded.
  useEffect(() => {
    if (!initialIndicatorId || !process || isLoading) {
      return;
    }
    navigateToIndicator(initialIndicatorId);
  }, [initialIndicatorId, process, isLoading, navigateToIndicator]);

  // initialObservationId reserved for future observation highlighting in the tree.
  void initialObservationId;

  const { exportDossier, isPending: isExporting, progress, error: exportError } = useDossierExport(processId);

  const handleConfirmClose = async () => {
    setCloseError(null);
    try {
      const response = await closeProcess({ processId });
      if (response.status !== 204) {
        setCloseError('No se pudo cerrar el proceso.');
        return;
      }
      setCloseDialogOpen(false);
      await refetch();
    } catch (error) {
      setCloseError(getApiErrorMessage(error, 'No se pudo cerrar el proceso.'));
    }
  };

  return (
    <div className="space-y-6" ref={structureAnchorRef}>
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
              <div className="flex flex-wrap items-center gap-2">
                <ProcessStatusBadge status={process.status ?? 'UNKNOWN'} />
                <ProcessOperationalModeBadge mode={process.operationalMode} />
                {canCloseProcess && (
                  <Button
                    variant="secondary"
                    onClick={() => {
                      setCloseError(null);
                      setCloseDialogOpen(true);
                    }}
                    disabled={isClosing}
                    className="bg-body text-primary-700 hover:bg-primary-50"
                  >
                    <Lock size={16} />
                    Cerrar proceso
                  </Button>
                )}
              </div>
            </div>
            <div className="mt-4 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
              <p className="text-body-md text-primary-100">Inicio: {formatDate(process.startDate)}</p>
              {(session?.role === 'JD' || session?.role === 'TD' || session?.role === 'CC' || session?.role === 'EE') && (
                <div className="flex flex-col items-start gap-1">
                  <Button
                    variant="secondary"
                    onClick={() => exportDossier().catch(() => undefined)}
                    disabled={isExporting}
                    className="bg-body text-primary-700 hover:bg-primary-50"
                  >
                    <Download size={16} />
                    {isExporting ? `Exportando... ${progress}%` : 'Exportar Dossier Completo (ZIP)'}
                  </Button>
                  {exportError && (
                    <p className="text-body-sm text-primary-50">{exportError}</p>
                  )}
                </div>
              )}
            </div>
          </section>

          <ProcessResponsibleContainer
            processId={processId}
            process={process}
            onUpdated={refetch}
          />

          <EvaluationMetricsPanelContainer processId={processId} />

          {/* Temporarily hidden: congelamiento, sala de pares, CAE, plan mejoras, ciclo metodológico */}

          {canSeeObservations && session && (
            <ObservationsPanel processId={processId} role={session.role} />
          )}

          <section
            className="rounded-2xl border border-gray-200 bg-body p-6 shadow-sm"
            data-testid="process-structure-section"
          >
            <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
              <div>
                <h2 className="text-heading-lg font-semibold text-primary-800">
                  Estructura del proceso
                </h2>
                <p className="mt-1 text-body-md text-gray-600">
                  Jerarquía normativa {process.evaluatorModel ?? process.templateType ?? ''}{' '}
                  (dimensión → área → criterio → indicador). Despliegue cada nivel con el icono ▸.
                </p>
              </div>
              {canEditStructure && (
                <Link to={`/procesos/${processId}/estructura`}>
                  <Button variant="secondary">
                    <Pencil size={16} />
                    Editar estructura normativa
                  </Button>
                </Link>
              )}
            </div>

            <ProcessEvidenceSearchPanel
              processId={processId}
              programId={process.careerId}
              level1Nodes={process.level1Nodes ?? []}
              onNavigateToIndicator={navigateToIndicator}
            />

            <div className="mt-6">
              <ProcessNormativeTree
                level1Nodes={process.level1Nodes ?? []}
                processId={processId}
                canUploadEvidence={canUploadEvidence}
                canEditFoda={canEditFoda}
                canReviewEvidence={canReviewEvidence}
                canSubsanateEvidence={canUploadEvidence}
                canCloseLevel1={canReviewEvidence}
                onStructureUpdated={refetch}
                onNavigateToIndicator={navigateToIndicator}
                expandToIndicatorId={expandToIndicatorId}
              />
            </div>
          </section>
        </div>
      )}

      {closeError && <Alert variant="error">{closeError}</Alert>}

      <ConfirmDialog
        isOpen={closeDialogOpen}
        title="Cerrar proceso de acreditación"
        description={
          process
            ? `¿Cerrar el proceso de ${process.careerName ?? 'esta carrera'}? Pasará a estado CLOSED y se notificará a jefatura [JD] por correo e in-app.`
            : '¿Cerrar este proceso de acreditación?'
        }
        confirmLabel="Cerrar proceso"
        confirmVariant="danger"
        isLoading={isClosing}
        onClose={() => {
          if (!isClosing) {
            setCloseDialogOpen(false);
          }
        }}
        onConfirm={() => {
          void handleConfirmClose();
        }}
      />
    </div>
  );
}
