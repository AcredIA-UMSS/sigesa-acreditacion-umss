import { CheckCircle2, Circle, Clock, Eye, Send } from 'lucide-react';
import type { MethodologicalStageResponseDto } from '../../../api/model';
import { Button } from '../../../components/ui/Button';

export type StageStatus =
  | 'PENDING'
  | 'IN_PROGRESS'
  | 'SUBMITTED_FOR_REVIEW'
  | 'OBSERVED'
  | 'APPROVED'
  | string;

interface MethodologicalStageTimelineProps {
  stages: MethodologicalStageResponseDto[];
  isLoading: boolean;
  isError: boolean;
  errorMessage?: string;
  canSubmitStage: boolean;
  canApproveStage: boolean;
  canApproveDeliverable: boolean;
  activeStageId?: string;
  isSubmitting: boolean;
  isApprovingStage: boolean;
  approvingDeliverableId?: string;
  onSubmitStage: (stageId: string) => void;
  onApproveStage: (stageId: string) => void;
  onApproveDeliverable: (stageId: string, deliverableId: string) => void;
  onRefresh: () => void;
}

const stageStatusConfig: Record<string, { label: string; className: string }> = {
  PENDING: { label: 'Pendiente', className: 'bg-gray-100 text-gray-700 border-gray-300' },
  IN_PROGRESS: { label: 'En curso', className: 'bg-primary-100 text-primary-700 border-primary-300' },
  SUBMITTED_FOR_REVIEW: {
    label: 'En revisión',
    className: 'bg-warning/15 text-warning border-warning/40',
  },
  OBSERVED: { label: 'Observada', className: 'bg-secondary-100 text-secondary-700 border-secondary-300' },
  APPROVED: { label: 'Aprobada', className: 'bg-success/15 text-success border-success/30' },
};

function StageStatusBadge({ status }: { status?: string }) {
  const normalized = status?.toUpperCase() ?? 'PENDING';
  const config = stageStatusConfig[normalized] ?? stageStatusConfig.PENDING;
  return (
    <span
      className={`inline-flex rounded-full border px-2.5 py-0.5 text-label-md font-medium ${config.className}`}
    >
      {config.label}
    </span>
  );
}

function stageIcon(status?: string) {
  switch (status?.toUpperCase()) {
    case 'APPROVED':
      return <CheckCircle2 size={18} className="text-success" />;
    case 'IN_PROGRESS':
      return <Clock size={18} className="text-primary-600" />;
    case 'SUBMITTED_FOR_REVIEW':
      return <Send size={18} className="text-warning" />;
    case 'OBSERVED':
      return <Eye size={18} className="text-secondary-600" />;
    default:
      return <Circle size={18} className="text-gray-400" />;
  }
}

export function MethodologicalStageTimeline({
  stages,
  isLoading,
  isError,
  errorMessage,
  canSubmitStage,
  canApproveStage,
  canApproveDeliverable,
  activeStageId,
  isSubmitting,
  isApprovingStage,
  approvingDeliverableId,
  onSubmitStage,
  onApproveStage,
  onApproveDeliverable,
  onRefresh,
}: MethodologicalStageTimelineProps) {
  if (isLoading) {
    return (
      <div className="rounded-2xl border border-gray-200 bg-body p-6 text-body-md text-gray-600">
        Cargando etapas metodológicas…
      </div>
    );
  }

  if (isError) {
    return (
      <div className="rounded-2xl border border-danger/30 bg-danger/10 p-6">
        <p className="text-body-md text-danger">{errorMessage ?? 'No se pudo cargar el timeline.'}</p>
        <Button className="mt-4" variant="secondary" onClick={onRefresh}>
          Reintentar
        </Button>
      </div>
    );
  }

  const activeStage = stages.find((stage) => stage.id === activeStageId)
    ?? stages.find((stage) => stage.status === 'IN_PROGRESS' || stage.status === 'OBSERVED')
    ?? stages.find((stage) => stage.status === 'SUBMITTED_FOR_REVIEW');

  return (
    <section className="rounded-2xl border border-gray-200 bg-body p-6 shadow-sm">
      <div className="mb-6">
        <h2 className="text-heading-lg font-semibold text-primary-800">
          Ciclo metodológico
        </h2>
        <p className="mt-1 text-body-md text-gray-600">
          Siete etapas de gestión del proceso de acreditación (ADR-0005). La evaluación normativa
          continúa siendo transversal en paralelo.
        </p>
      </div>

      <ol className="flex gap-3 overflow-x-auto pb-2">
        {stages.map((stage) => {
          const isActive =
            stage.id === activeStage?.id
            || stage.status === 'IN_PROGRESS'
            || stage.status === 'SUBMITTED_FOR_REVIEW'
            || stage.status === 'OBSERVED';
          return (
            <li
              key={stage.id}
              className={`min-w-[11rem] flex-1 rounded-xl border p-3 ${
                isActive ? 'border-primary-300 bg-primary-50' : 'border-gray-200 bg-gray-50'
              }`}
            >
              <div className="flex items-start gap-2">
                {stageIcon(stage.status)}
                <div className="min-w-0 flex-1">
                  <p className="text-label-md font-medium text-gray-500">E{stage.order}</p>
                  <p className="text-body-md font-semibold text-primary-800 line-clamp-2">
                    {stage.displayName ?? stage.code}
                  </p>
                  <div className="mt-2">
                    <StageStatusBadge status={stage.status} />
                  </div>
                </div>
              </div>
            </li>
          );
        })}
      </ol>

      {activeStage && (
        <div className="mt-6 rounded-xl border border-gray-200 bg-gray-50 p-4">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <p className="text-label-md uppercase tracking-wide text-gray-500">Etapa activa</p>
              <h3 className="text-heading-sm font-semibold text-primary-800">
                E{activeStage.order} · {activeStage.displayName}
              </h3>
            </div>
            <div className="flex flex-wrap gap-2">
              {canSubmitStage
                && activeStage.id
                && (activeStage.status === 'IN_PROGRESS' || activeStage.status === 'OBSERVED') && (
                <Button
                  variant="primary"
                  isLoading={isSubmitting}
                  onClick={() => onSubmitStage(activeStage.id!)}
                >
                  <Send size={16} />
                  Enviar a revisión
                </Button>
              )}
              {canApproveStage
                && activeStage.id
                && activeStage.status === 'SUBMITTED_FOR_REVIEW' && (
                <Button
                  variant="secondary"
                  isLoading={isApprovingStage}
                  onClick={() => onApproveStage(activeStage.id!)}
                >
                  <CheckCircle2 size={16} />
                  Aprobar etapa
                </Button>
              )}
            </div>
          </div>

          {(activeStage.deliverables?.length ?? 0) > 0 && (
            <ul className="mt-4 space-y-2">
              {activeStage.deliverables?.map((deliverable) => (
                <li
                  key={deliverable.id}
                  className="flex flex-col gap-2 rounded-lg border border-gray-200 bg-body px-3 py-2 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div>
                    <p className="text-body-md font-medium text-gray-800">
                      {deliverable.deliverableCode?.replaceAll('_', ' ')}
                    </p>
                    <p className="text-label-md text-gray-500">
                      Estado: {deliverable.approvalStatus ?? 'PENDING'}
                    </p>
                  </div>
                  {canApproveDeliverable
                    && deliverable.id
                    && activeStage.id
                    && deliverable.approvalStatus !== 'APPROVED' && (
                    <Button
                      variant="ghost"
                      isLoading={approvingDeliverableId === deliverable.id}
                      onClick={() =>
                        onApproveDeliverable(activeStage.id!, deliverable.id!)}
                    >
                      Aprobar entregable
                    </Button>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </section>
  );
}
