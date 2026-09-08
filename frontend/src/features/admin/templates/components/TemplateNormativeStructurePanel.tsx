import { RefreshCw } from 'lucide-react';
import { Alert } from '../../../../components/ui/Alert';
import { Button } from '../../../../components/ui/Button';
import {
  ProcessNormativeStructureEditorUI,
  type NormativeIndicatorDraft,
  type NormativeNodeDraft,
} from '../../../processes/components/ProcessNormativeStructureEditorUI';
import { useTemplateNormativeStructureEditor } from '../hooks/useTemplateNormativeStructureEditor';
import { TemplateStatusBadge } from './TemplateStatusBadge';
import type { TemplateStatusCode } from '../lib/templateTypes';

interface TemplateNormativeStructurePanelProps {
  templateId: string;
  status: TemplateStatusCode;
  onCancel: () => void;
  onSave: () => void;
  onPublish: () => void;
  onArchive: () => void;
  onDuplicate: () => void;
  onDelete: () => void;
  isSaving: boolean;
}

function submitNode(draft: NormativeNodeDraft) {
  const order = Number.parseInt(draft.order, 10);
  if (Number.isNaN(order)) return null;
  return {
    name: draft.name.trim(),
    order,
    description: draft.description.trim() || undefined,
  };
}

function submitIndicator(draft: NormativeIndicatorDraft) {
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
}

export function TemplateNormativeStructurePanel({
  templateId,
  status,
  onCancel,
  onSave,
  onPublish,
  onArchive,
  onDuplicate,
  onDelete,
  isSaving,
}: TemplateNormativeStructurePanelProps) {
  const normative = useTemplateNormativeStructureEditor(templateId);
  const { template, isLoading, isError, isNotFound, errorMessage, refetch } = normative;

  if (isLoading) {
    return (
      <div className="rounded-2xl border border-gray-200 bg-body p-12 text-center">
        <p className="text-body-md text-gray-600">Cargando jerarquía normativa…</p>
      </div>
    );
  }

  if (isError && !template) {
    return (
      <div
        className={`rounded-2xl border p-6 ${
          isNotFound ? 'border-gray-300 bg-gray-50' : 'border-danger/30 bg-danger/10'
        }`}
      >
        <p className={`text-body-md ${isNotFound ? 'text-gray-700' : 'text-danger'}`}>
          {errorMessage}
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="mb-3 flex items-center gap-3">
            <h2 className="text-heading-md text-primary-800">Jerarquía normativa v2</h2>
            <TemplateStatusBadge status={status} />
          </div>
          <p className="text-body-md text-gray-600">
            Estructura N1→N2→N3→Indicador. Edite metadatos (nombre, tipo) en la pestaña legacy.
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button type="button" variant="ghost" onClick={onCancel}>
            Volver
          </Button>
          <Button type="button" onClick={onSave} isLoading={isSaving}>
            Guardar metadatos
          </Button>
          {status === 'DRAFT' && (
            <Button type="button" variant="secondary" onClick={onPublish}>
              Publicar
            </Button>
          )}
          {status === 'PUBLISHED' && (
            <Button type="button" variant="ghost" onClick={onArchive}>
              Archivar
            </Button>
          )}
          <Button type="button" variant="ghost" onClick={onDuplicate}>
            Duplicar
          </Button>
          {status === 'DRAFT' && (
            <Button type="button" variant="danger" onClick={onDelete}>
              Eliminar
            </Button>
          )}
        </div>
      </div>

      <section className="rounded-2xl border border-primary-200/40 bg-gradient-to-r from-primary-600 to-primary-500 p-6 text-body shadow-md">
        <div className="flex flex-col justify-between gap-4 md:flex-row md:items-start">
          <div>
            <p className="text-label-md uppercase tracking-wide text-primary-100">
              Jerarquía normativa v2 · {template?.evaluatorModel ?? template?.type ?? 'Plantilla'}
            </p>
            <h2 className="mt-1 text-heading-lg font-bold text-body">{template?.name ?? 'Plantilla'}</h2>
            <p className="mt-1 text-body-md text-primary-100">
              N1: {template?.level1Count ?? 0} · Indicadores: {template?.indicatorCount ?? 0}
            </p>
          </div>
          <div className="flex items-center gap-3">
            <TemplateStatusBadge status={status} />
            <Button variant="ghost" onClick={refetch} isLoading={isLoading}>
              <RefreshCw size={16} />
              Actualizar
            </Button>
          </div>
        </div>
      </section>

      {status !== 'DRAFT' && (
        <Alert variant="info">
          Solo las plantillas en borrador permiten editar la jerarquía normativa v2.
        </Alert>
      )}

      <ProcessNormativeStructureEditorUI
        level1Nodes={template?.level1Nodes ?? []}
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
    </div>
  );
}
