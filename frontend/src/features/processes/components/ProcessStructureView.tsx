import { ArrowLeft, RefreshCw } from 'lucide-react';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../../../components/ui/Button';
import {
  ProcessNormativeStructureEditorUI,
  type NormativeIndicatorDraft,
  type NormativeNodeDraft,
} from './ProcessNormativeStructureEditorUI';
import { ProcessStatusBadge } from './ProcessStatusBadge';
import {
  ProcessStructureEditorUI,
  type NewPhaseDraft,
  type PhaseDraft,
  type SubphaseDraft,
} from './ProcessStructureEditorUI';
import { PhasesCopilotPanel } from './PhasesCopilotPanel';
import { useProcessNormativeStructureEditor } from '../hooks/useProcessNormativeStructureEditor';
import { useProcessStructureEditor } from '../hooks/useProcessStructureEditor';

interface ProcessStructureViewProps {
  processId: string;
}

type StructureEditorMode = 'normative' | 'legacy';

export function ProcessStructureView({ processId }: ProcessStructureViewProps) {
  const legacy = useProcessStructureEditor(processId);
  const normative = useProcessNormativeStructureEditor(processId);
  const hasNormativeTree = (legacy.process?.level1Nodes?.length ?? 0) > 0;
  const [mode, setMode] = useState<StructureEditorMode>(
    hasNormativeTree ? 'normative' : 'legacy',
  );

  const active = mode === 'normative' ? normative : legacy;
  const {
    process,
    isLoading,
    isError,
    isNotFound,
    errorMessage,
    refetch,
  } = active;

  const handleAddPhase = async (draft: NewPhaseDraft): Promise<boolean> => {
    const order = Number.parseInt(draft.order, 10);
    if (Number.isNaN(order)) return false;
    return legacy.addPhase({
      name: draft.name,
      order,
      description: draft.description.trim() || undefined,
    });
  };

  const handleUpdatePhase = async (phaseId: string, draft: PhaseDraft): Promise<boolean> => {
    const order = Number.parseInt(draft.order, 10);
    if (Number.isNaN(order)) return false;
    return legacy.updatePhase({
      phaseId,
      data: {
        name: draft.name,
        order,
        description: draft.description.trim() || undefined,
      },
    });
  };

  const handleAddSubphase = async (phaseId: string, draft: SubphaseDraft): Promise<boolean> => {
    const order = Number.parseInt(draft.order, 10);
    if (Number.isNaN(order)) return false;
    return legacy.addSubphase({
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
    if (Number.isNaN(order)) return false;
    return legacy.updateSubphase({
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

  const submitNode = (draft: NormativeNodeDraft) => {
    const order = Number.parseInt(draft.order, 10);
    if (Number.isNaN(order)) return null;
    return {
      name: draft.name.trim(),
      order,
      description: draft.description.trim() || undefined,
    };
  };

  const submitIndicator = (draft: NormativeIndicatorDraft) => {
    const order = Number.parseInt(draft.order, 10);
    const weight = Number.parseFloat(draft.weight);
    if (Number.isNaN(order) || Number.isNaN(weight)) return null;
    return {
      code: draft.code.trim(),
      description: draft.description.trim(),
      weight,
      order,
      referenceUrl: draft.referenceUrl.trim(),
    };
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
        <div className="space-y-6">
          <section className="rounded-2xl border border-primary-200/40 bg-gradient-to-r from-primary-600 to-primary-500 p-6 text-body shadow-md">
            <div className="flex flex-col justify-between gap-4 md:flex-row md:items-start">
              <div>
                <p className="text-label-md uppercase tracking-wide text-primary-100">
                  Edición estructural · {process.evaluatorModel ?? process.templateType ?? 'Plantilla'}
                </p>
                <h1 className="mt-1 text-heading-xl font-bold">{process.careerName ?? 'Carrera'}</h1>
                <p className="mt-1 text-body-md text-primary-100">
                  {process.careerCode ?? '—'} · {process.templateName ?? '—'}
                </p>
              </div>
              <ProcessStatusBadge status={process.status ?? 'UNKNOWN'} />
            </div>
          </section>

          <div className="flex flex-wrap gap-2">
            <Button
              variant={mode === 'normative' ? 'primary' : 'ghost'}
              onClick={() => setMode('normative')}
            >
              Jerarquía normativa v2
            </Button>
            <Button
              variant={mode === 'legacy' ? 'primary' : 'ghost'}
              onClick={() => setMode('legacy')}
            >
              Fases / subfases (legacy)
            </Button>
          </div>

          {mode === 'normative' ? (
            <ProcessNormativeStructureEditorUI
              level1Nodes={process.level1Nodes ?? []}
              isEditable={normative.isEditable}
              isBusy={normative.isBusy}
              actionError={normative.actionError}
              onAddLevel1={async (draft) => {
                const payload = submitNode(draft);
                return payload ? normative.addLevel1(payload) : false;
              }}
              onUpdateLevel1={async (level1Id, draft) => {
                const payload = submitNode(draft);
                return payload ? normative.updateLevel1({ level1Id, data: payload }) : false;
              }}
              onDeleteLevel1={normative.deleteLevel1}
              onAddLevel2={async (level1Id, draft) => {
                const payload = submitNode(draft);
                return payload ? normative.addLevel2({ level1Id, data: payload }) : false;
              }}
              onUpdateLevel2={async (level2Id, draft) => {
                const payload = submitNode(draft);
                return payload ? normative.updateLevel2({ level2Id, data: payload }) : false;
              }}
              onDeleteLevel2={normative.deleteLevel2}
              onAddLevel3={async (level2Id, draft) => {
                const payload = submitNode(draft);
                return payload ? normative.addLevel3({ level2Id, data: payload }) : false;
              }}
              onUpdateLevel3={async (level3Id, draft) => {
                const payload = submitNode(draft);
                return payload ? normative.updateLevel3({ level3Id, data: payload }) : false;
              }}
              onDeleteLevel3={normative.deleteLevel3}
              onAddIndicator={async (level3Id, draft) => {
                const payload = submitIndicator(draft);
                return payload ? normative.addIndicator({ level3Id, data: payload }) : false;
              }}
              onUpdateIndicator={async (indicatorId, draft) => {
                const payload = submitIndicator(draft);
                return payload ? normative.updateIndicator({ indicatorId, data: payload }) : false;
              }}
              onDeleteIndicator={normative.deleteIndicator}
            />
          ) : (
            <ProcessStructureEditorUI
              phases={process.phases ?? []}
              isEditable={legacy.isEditable}
              isBusy={legacy.isBusy}
              actionError={legacy.actionError}
              onAddPhase={handleAddPhase}
              onUpdatePhase={handleUpdatePhase}
              onDeletePhase={legacy.deletePhase}
              onAddSubphase={handleAddSubphase}
              onUpdateSubphase={handleUpdateSubphase}
              onDeleteSubphase={(phaseId, subphaseId) =>
                legacy.deleteSubphase({ phaseId, subphaseId })
              }
            />
          )}

          {legacy.isEditable && (
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
