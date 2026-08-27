import { ArrowLeft, RefreshCw } from 'lucide-react';
import { Link } from 'react-router-dom';
import { Button } from '../../../components/ui/Button';
import { ProcessStatusBadge } from './ProcessStatusBadge';
import {
  ProcessStructureEditorUI,
  type NewPhaseDraft,
  type PhaseDraft,
  type SubphaseDraft,
} from './ProcessStructureEditorUI';
import { PhasesCopilotPanel } from './PhasesCopilotPanel';
import { useProcessStructureEditor } from '../hooks/useProcessStructureEditor';

interface ProcessStructureViewProps {
  processId: string;
}

export function ProcessStructureView({ processId }: ProcessStructureViewProps) {
  const {
    process,
    isLoading,
    isError,
    isNotFound,
    errorMessage,
    refetch,
    actionError,
    isEditable,
    isBusy,
    addPhase,
    updatePhase,
    deletePhase,
    addSubphase,
    updateSubphase,
    deleteSubphase,
  } = useProcessStructureEditor(processId);

  const handleAddPhase = async (draft: NewPhaseDraft): Promise<boolean> => {
    const order = Number.parseInt(draft.order, 10);
    if (Number.isNaN(order)) {
      return false;
    }
    return addPhase({
      name: draft.name,
      order,
      description: draft.description.trim() || undefined,
    });
  };

  const handleUpdatePhase = async (phaseId: string, draft: PhaseDraft): Promise<boolean> => {
    const order = Number.parseInt(draft.order, 10);
    if (Number.isNaN(order)) {
      return false;
    }
    return updatePhase({
      phaseId,
      data: {
        name: draft.name,
        order,
        description: draft.description.trim() || undefined,
      },
    });
  };

  const handleAddSubphase = async (
    phaseId: string,
    draft: SubphaseDraft,
  ): Promise<boolean> => {
    const order = Number.parseInt(draft.order, 10);
    if (Number.isNaN(order)) {
      return false;
    }
    return addSubphase({
      phaseId,
      data: {
        name: draft.name,
        order,
        referenceUrl: draft.referenceUrl.trim(),
        description: draft.description.trim() || undefined,
        requirements: draft.requirements.trim(),
      },
    });
  };

  const handleUpdateSubphase = async (
    phaseId: string,
    subphaseId: string,
    draft: SubphaseDraft,
  ): Promise<boolean> => {
    const order = Number.parseInt(draft.order, 10);
    if (Number.isNaN(order)) {
      return false;
    }
    return updateSubphase({
      phaseId,
      subphaseId,
      data: {
        name: draft.name,
        order,
        referenceUrl: draft.referenceUrl.trim(),
        description: draft.description.trim() || undefined,
        requirements: draft.requirements.trim(),
      },
    });
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <Link
          to={`/procesos/${processId}`}
          className="inline-flex items-center gap-2 text-body-md font-medium text-primary-600 hover:text-primary-800"
        >
          <ArrowLeft size={18} />
          Volver al detalle
        </Link>
        <Button variant="ghost" onClick={refetch} isLoading={isLoading}>
          <RefreshCw size={16} />
          Actualizar
        </Button>
      </div>

      {isLoading && (
        <div className="rounded-2xl border border-gray-200 bg-body p-12 text-center">
          <p className="text-body-md text-gray-600">Cargando estructura del proceso…</p>
        </div>
      )}

      {isError && !isLoading && (
        <div
          className={`rounded-2xl border p-6 ${
            isNotFound ? 'border-gray-300 bg-gray-50' : 'border-danger/30 bg-danger/10'
          }`}
        >
          <p className={`text-body-md ${isNotFound ? 'text-gray-700' : 'text-danger'}`}>
            {errorMessage}
          </p>
        </div>
      )}

      {!isLoading && !isError && process && (
        <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_340px] xl:items-start">
          <div className="space-y-6">
          <section className="rounded-2xl border border-primary-200/40 bg-gradient-to-r from-primary-600 to-primary-500 p-6 text-body shadow-md">
            <div className="flex flex-col justify-between gap-4 md:flex-row md:items-start">
              <div>
                <p className="text-label-md uppercase tracking-wide text-primary-100">
                  Edición estructural · {process.templateType ?? 'Plantilla'}
                </p>
                <h1 className="mt-1 text-heading-xl font-bold">{process.careerName ?? 'Carrera'}</h1>
                <p className="mt-1 text-body-md text-primary-100">
                  {process.careerCode ?? '—'} · {process.templateName ?? '—'}
                </p>
              </div>
              <ProcessStatusBadge status={process.status ?? 'UNKNOWN'} />
            </div>
          </section>

          <ProcessStructureEditorUI
            phases={process.phases ?? []}
            isEditable={isEditable}
            isBusy={isBusy}
            actionError={actionError}
            onAddPhase={handleAddPhase}
            onUpdatePhase={handleUpdatePhase}
            onDeletePhase={deletePhase}
            onAddSubphase={handleAddSubphase}
            onUpdateSubphase={handleUpdateSubphase}
            onDeleteSubphase={(phaseId, subphaseId) =>
              deleteSubphase({ phaseId, subphaseId })
            }
          />
          </div>

          {isEditable && (
            <PhasesCopilotPanel
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
