import { useState } from 'react';
import { FileDown, Plus, Save } from 'lucide-react';
import { Button } from '../../../components/ui/Button';
import { TextInput } from '../../../components/ui/TextInput';

export type ImprovementPlanItemView = {
  id: string;
  processId?: string;
  sourceIndicatorId?: string;
  objective: string;
  activity?: string;
  responsibleId?: string;
  deadline?: string;
  progress: number;
  justification?: string;
  progressEvidenceAssetId?: string;
};

export type ImprovementPlanDraft = {
  objective: string;
  activity?: string;
  responsibleId?: string;
  deadline?: string;
  progress?: number;
  justification?: string;
};

export type ImprovementPlanPanelProps = {
  items: ImprovementPlanItemView[];
  canEdit: boolean;
  canEditProgressOnly?: boolean;
  isLoading: boolean;
  isError: boolean;
  errorMessage?: string;
  successMessage?: string;
  isSaving: boolean;
  isImporting: boolean;
  editingItemId?: string;
  onRefresh: () => void;
  onImport: () => void;
  onCreate: (draft: ImprovementPlanDraft) => void;
  onUpdate: (itemId: string, draft: ImprovementPlanDraft) => void;
};

function emptyDraft(): ImprovementPlanDraft {
  return { objective: '', activity: '', deadline: '', progress: 0 };
}

export function ImprovementPlanPanel({
  items,
  canEdit,
  canEditProgressOnly = false,
  isLoading,
  isError,
  errorMessage,
  successMessage,
  isSaving,
  isImporting,
  editingItemId,
  onRefresh,
  onImport,
  onCreate,
  onUpdate,
}: ImprovementPlanPanelProps) {
  const [draft, setDraft] = useState<ImprovementPlanDraft>(emptyDraft());
  const [editDraft, setEditDraft] = useState<ImprovementPlanDraft>(emptyDraft());
  const [editingId, setEditingId] = useState<string | undefined>();

  return (
    <section className="rounded-2xl border border-gray-200 bg-body p-6 shadow-sm">
      <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h2 className="text-heading-lg font-semibold text-primary-800">
            Plan de Mejoras
          </h2>
          <p className="mt-1 text-body-md text-gray-600">
            Ítems alimentados desde debilidades FODA o creados de forma manual. Requerido para
            los documentos E4.
          </p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button variant="ghost" onClick={onRefresh} isLoading={isLoading}>
            Actualizar
          </Button>
          {canEdit && !canEditProgressOnly && (
            <Button variant="secondary" onClick={onImport} isLoading={isImporting}>
              <FileDown size={16} />
              Importar desde FODA
            </Button>
          )}
        </div>
      </div>

      {isError && (
        <p className="mb-3 text-body-md text-danger">{errorMessage ?? 'No se pudo cargar el plan.'}</p>
      )}
      {successMessage && <p className="mb-3 text-label-md text-success">{successMessage}</p>}

      {isLoading ? (
        <p className="text-body-md text-gray-600">Cargando plan de mejoras…</p>
      ) : items.length === 0 ? (
        <p className="text-body-md text-gray-500">Aún no hay ítems en el plan.</p>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full text-left">
            <thead>
              <tr className="border-b border-gray-200 text-label-md text-gray-600">
                <th className="px-2 py-2 font-medium">Objetivo</th>
                <th className="px-2 py-2 font-medium">Actividad</th>
                <th className="px-2 py-2 font-medium">Plazo</th>
                <th className="px-2 py-2 font-medium">Avance</th>
                <th className="px-2 py-2 font-medium">Justificación</th>
                <th className="px-2 py-2 font-medium">Origen</th>
                {(canEdit || canEditProgressOnly) && (
                  <th className="px-2 py-2 font-medium">Acciones</th>
                )}
              </tr>
            </thead>
            <tbody>
              {items.map((item) => (
                <tr key={item.id} className="border-b border-gray-100">
                  {editingId === item.id ? (
                    <td colSpan={canEdit || canEditProgressOnly ? 7 : 6} className="px-2 py-3">
                      <form
                        className="grid gap-3 md:grid-cols-[2fr_2fr_8rem_6rem_2fr_auto]"
                        onSubmit={(event) => {
                          event.preventDefault();
                          onUpdate(item.id, editDraft);
                          setEditingId(undefined);
                        }}
                      >
                        <TextInput
                          label="Objetivo"
                          requiredMark
                          value={editDraft.objective}
                          disabled={canEditProgressOnly}
                          onChange={(event) =>
                            setEditDraft((current) => ({ ...current, objective: event.target.value }))}
                        />
                        <TextInput
                          label="Actividad"
                          value={editDraft.activity ?? ''}
                          disabled={canEditProgressOnly}
                          onChange={(event) =>
                            setEditDraft((current) => ({ ...current, activity: event.target.value }))}
                        />
                        <TextInput
                          label="Plazo"
                          type="date"
                          value={editDraft.deadline ?? ''}
                          disabled={canEditProgressOnly}
                          onChange={(event) =>
                            setEditDraft((current) => ({ ...current, deadline: event.target.value }))}
                        />
                        <TextInput
                          label="Avance %"
                          type="number"
                          min={0}
                          max={100}
                          value={String(editDraft.progress ?? 0)}
                          onChange={(event) =>
                            setEditDraft((current) => ({
                              ...current,
                              progress: Number.parseInt(event.target.value, 10) || 0,
                            }))}
                        />
                        <TextInput
                          label="Justificación"
                          value={editDraft.justification ?? ''}
                          onChange={(event) =>
                            setEditDraft((current) => ({
                              ...current,
                              justification: event.target.value,
                            }))}
                        />
                        <div className="flex items-end gap-2">
                          <Button type="submit" variant="primary" isLoading={isSaving && editingItemId === item.id}>
                            <Save size={16} />
                            Guardar
                          </Button>
                          <Button type="button" variant="ghost" onClick={() => setEditingId(undefined)}>
                            Cancelar
                          </Button>
                        </div>
                      </form>
                    </td>
                  ) : (
                    <>
                      <td className="px-2 py-2 text-body-md text-gray-800">{item.objective}</td>
                      <td className="px-2 py-2 text-body-md text-gray-600">{item.activity ?? '—'}</td>
                      <td className="px-2 py-2 text-body-md text-gray-600">{item.deadline ?? '—'}</td>
                      <td className="px-2 py-2 text-body-md text-gray-800">{item.progress}%</td>
                      <td className="px-2 py-2 text-body-md text-gray-600">
                        {item.justification ?? '—'}
                      </td>
                      <td className="px-2 py-2 text-label-md text-gray-500">
                        {item.sourceIndicatorId ? 'FODA' : 'Manual'}
                      </td>
                      {(canEdit || canEditProgressOnly) && (
                        <td className="px-2 py-2">
                          <Button
                            variant="ghost"
                            onClick={() => {
                              setEditingId(item.id);
                              setEditDraft({
                                objective: item.objective,
                                activity: item.activity,
                                deadline: item.deadline,
                                progress: item.progress,
                                justification: item.justification,
                              });
                            }}
                          >
                            Editar
                          </Button>
                        </td>
                      )}
                    </>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {canEdit && !canEditProgressOnly && (
        <form
          className="mt-6 grid gap-3 rounded-xl border border-primary-100 bg-primary-50 p-4 md:grid-cols-[2fr_2fr_8rem_6rem_auto] md:items-end"
          onSubmit={(event) => {
            event.preventDefault();
            onCreate(draft);
            setDraft(emptyDraft());
          }}
        >
          <TextInput
            label="Nuevo objetivo"
            requiredMark
            value={draft.objective}
            onChange={(event) => setDraft((current) => ({ ...current, objective: event.target.value }))}
          />
          <TextInput
            label="Actividad"
            value={draft.activity ?? ''}
            onChange={(event) => setDraft((current) => ({ ...current, activity: event.target.value }))}
          />
          <TextInput
            label="Plazo"
            type="date"
            value={draft.deadline ?? ''}
            onChange={(event) => setDraft((current) => ({ ...current, deadline: event.target.value }))}
          />
          <TextInput
            label="Avance %"
            type="number"
            min={0}
            max={100}
            value={String(draft.progress ?? 0)}
            onChange={(event) =>
              setDraft((current) => ({
                ...current,
                progress: Number.parseInt(event.target.value, 10) || 0,
              }))}
          />
          <Button type="submit" variant="primary" isLoading={isSaving && !editingItemId}>
            <Plus size={16} />
            Añadir ítem
          </Button>
        </form>
      )}
    </section>
  );
}
