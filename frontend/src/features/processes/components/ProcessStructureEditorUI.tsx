import { ExternalLink, Layers, Plus, Trash2 } from 'lucide-react';
import { useState } from 'react';
import type { PhaseDto, SubphaseDto } from '../../../api/model';
import { Button } from '../../../components/ui/Button';
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog';
import { TextInput } from '../../../components/ui/TextInput';

export interface NewPhaseDraft {
  name: string;
  order: string;
  description: string;
}

export interface PhaseDraft {
  name: string;
  order: string;
  description: string;
}

export interface SubphaseDraft {
  name: string;
  order: string;
  referenceUrl: string;
  description: string;
}

interface ProcessStructureEditorUIProps {
  phases: PhaseDto[];
  isEditable: boolean;
  isBusy: boolean;
  actionError: string | null;
  onAddPhase: (draft: NewPhaseDraft) => Promise<boolean>;
  onUpdatePhase: (phaseId: string, draft: PhaseDraft) => Promise<boolean>;
  onDeletePhase: (phaseId: string) => Promise<boolean>;
  onAddSubphase: (phaseId: string, draft: SubphaseDraft) => Promise<boolean>;
  onUpdateSubphase: (
    phaseId: string,
    subphaseId: string,
    draft: SubphaseDraft,
  ) => Promise<boolean>;
  onDeleteSubphase: (phaseId: string, subphaseId: string) => Promise<boolean>;
}

const emptyNewPhase = (): NewPhaseDraft => ({
  name: '',
  order: '1',
  description: '',
});

const emptySubphase = (order: number): SubphaseDraft => ({
  name: '',
  order: String(order),
  referenceUrl: 'https://duea.umss.edu.bo/normativa/pendiente',
  description: '',
});

function toPhaseDraft(phase: PhaseDto): PhaseDraft {
  return {
    name: phase.name ?? '',
    order: String(phase.order ?? 1),
    description: phase.description ?? '',
  };
}

function toSubphaseDraft(subphase: SubphaseDto): SubphaseDraft {
  return {
    name: subphase.name ?? '',
    order: String(subphase.order ?? 1),
    referenceUrl: subphase.referenceUrl ?? '',
    description: subphase.description ?? '',
  };
}

export function ProcessStructureEditorUI({
  phases,
  isEditable,
  isBusy,
  actionError,
  onAddPhase,
  onUpdatePhase,
  onDeletePhase,
  onAddSubphase,
  onUpdateSubphase,
  onDeleteSubphase,
}: ProcessStructureEditorUIProps) {
  const sortedPhases = [...phases].sort((a, b) => (a.order ?? 0) - (b.order ?? 0));
  const [newPhase, setNewPhase] = useState<NewPhaseDraft>(() => ({
    ...emptyNewPhase(),
    order: String(sortedPhases.length + 1),
  }));
  const [newSubphaseByPhase, setNewSubphaseByPhase] = useState<Record<string, SubphaseDraft>>({});

  const getNewSubphaseDraft = (phase: PhaseDto): SubphaseDraft => {
    const phaseId = phase.id ?? '';
    if (newSubphaseByPhase[phaseId]) {
      return newSubphaseByPhase[phaseId];
    }
    return emptySubphase((phase.subphases?.length ?? 0) + 1);
  };

  const setNewSubphaseDraft = (phaseId: string, draft: SubphaseDraft) => {
    setNewSubphaseByPhase((current) => ({ ...current, [phaseId]: draft }));
  };

  const handleAddPhase = async () => {
    const order = Number.parseInt(newPhase.order, 10);
    if (!newPhase.name.trim() || Number.isNaN(order)) {
      return;
    }
    const ok = await onAddPhase({
      name: newPhase.name.trim(),
      order: String(order),
      description: newPhase.description.trim(),
    });
    if (ok) {
      setNewPhase({ ...emptyNewPhase(), order: String(sortedPhases.length + 2) });
    }
  };

  return (
    <div className="space-y-6">
      {!isEditable && (
        <div className="rounded-xl border border-warning/40 bg-warning/10 px-4 py-3 text-body-md text-gray-800">
          Este proceso no está en estado ACTIVE. La estructura es solo lectura.
        </div>
      )}

      {actionError && (
        <div className="rounded-xl border border-danger/30 bg-danger/10 px-4 py-3 text-body-md text-danger">
          {actionError}
        </div>
      )}

      {isEditable && (
        <section className="rounded-2xl border border-gray-200 bg-body p-6 shadow-sm">
          <h3 className="text-heading-sm font-semibold text-primary-800">Agregar fase</h3>
          <div className="mt-4 grid gap-4 md:grid-cols-3">
            <TextInput
              label="Nombre"
              value={newPhase.name}
              onChange={(event) => setNewPhase((c) => ({ ...c, name: event.target.value }))}
            />
            <TextInput
              label="Orden"
              type="number"
              min={1}
              value={newPhase.order}
              onChange={(event) => setNewPhase((c) => ({ ...c, order: event.target.value }))}
            />
            <TextInput
              label="Descripción (opcional)"
              value={newPhase.description}
              onChange={(event) =>
                setNewPhase((c) => ({ ...c, description: event.target.value }))
              }
            />
          </div>
          <Button className="mt-4" onClick={() => void handleAddPhase()} isLoading={isBusy}>
            <Plus size={16} />
            Agregar fase
          </Button>
        </section>
      )}

      <div className="space-y-4">
        {sortedPhases.map((phase) => (
          <PhaseEditorCard
            key={phase.id ?? phase.name}
            phase={phase}
            isEditable={isEditable}
            isBusy={isBusy}
            newSubphase={getNewSubphaseDraft(phase)}
            onNewSubphaseChange={(draft) => {
              if (phase.id) {
                setNewSubphaseDraft(phase.id, draft);
              }
            }}
            onUpdatePhase={onUpdatePhase}
            onDeletePhase={onDeletePhase}
            onAddSubphase={onAddSubphase}
            onUpdateSubphase={onUpdateSubphase}
            onDeleteSubphase={onDeleteSubphase}
          />
        ))}
      </div>

      {sortedPhases.length === 0 && (
        <p className="text-body-md text-gray-600">Este proceso no tiene fases registradas.</p>
      )}
    </div>
  );
}

interface PhaseEditorCardProps {
  phase: PhaseDto;
  isEditable: boolean;
  isBusy: boolean;
  newSubphase: SubphaseDraft;
  onNewSubphaseChange: (draft: SubphaseDraft) => void;
  onUpdatePhase: (phaseId: string, draft: PhaseDraft) => Promise<boolean>;
  onDeletePhase: (phaseId: string) => Promise<boolean>;
  onAddSubphase: (phaseId: string, draft: SubphaseDraft) => Promise<boolean>;
  onUpdateSubphase: (
    phaseId: string,
    subphaseId: string,
    draft: SubphaseDraft,
  ) => Promise<boolean>;
  onDeleteSubphase: (phaseId: string, subphaseId: string) => Promise<boolean>;
}

function PhaseEditorCard({
  phase,
  isEditable,
  isBusy,
  newSubphase,
  onNewSubphaseChange,
  onUpdatePhase,
  onDeletePhase,
  onAddSubphase,
  onUpdateSubphase,
  onDeleteSubphase,
}: PhaseEditorCardProps) {
  const [draft, setDraft] = useState<PhaseDraft>(() => toPhaseDraft(phase));
  const [confirmDeletePhase, setConfirmDeletePhase] = useState(false);
  const subphases = [...(phase.subphases ?? [])].sort((a, b) => (a.order ?? 0) - (b.order ?? 0));

  const savePhase = async () => {
    if (!phase.id) return;
    const order = Number.parseInt(draft.order, 10);
    if (!draft.name.trim() || Number.isNaN(order)) return;
    const ok = await onUpdatePhase(phase.id, draft);
    if (ok) {
      setDraft(toPhaseDraft({ ...phase, ...draft, order }));
    }
  };

  const removePhase = async () => {
    if (!phase.id) return;
    setConfirmDeletePhase(false);
    await onDeletePhase(phase.id);
  };

  const addSubphase = async () => {
    if (!phase.id) return;
    const order = Number.parseInt(newSubphase.order, 10);
    if (!newSubphase.name.trim() || !newSubphase.referenceUrl.trim() || Number.isNaN(order)) {
      return;
    }
    const ok = await onAddSubphase(phase.id, newSubphase);
    if (ok) {
      onNewSubphaseChange(emptySubphase(subphases.length + 2));
    }
  };

  return (
    <>
    <section className="overflow-hidden rounded-2xl border border-gray-200 bg-body shadow-sm">
      <div className="flex items-start gap-3 bg-primary-50 px-5 py-4">
        <div className="flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-primary-600 text-body">
          <Layers size={18} className="text-body" />
        </div>
        <div className="flex-1 space-y-3">
          <div className="grid gap-3 md:grid-cols-3">
            <TextInput
              label="Fase"
              value={draft.name}
              disabled={!isEditable}
              onChange={(event) => setDraft((c) => ({ ...c, name: event.target.value }))}
            />
            <TextInput
              label="Orden"
              type="number"
              min={1}
              value={draft.order}
              disabled={!isEditable}
              onChange={(event) => setDraft((c) => ({ ...c, order: event.target.value }))}
            />
            <TextInput
              label="Descripción"
              value={draft.description}
              disabled={!isEditable}
              onChange={(event) => setDraft((c) => ({ ...c, description: event.target.value }))}
            />
          </div>
          {isEditable && (
            <div className="flex flex-wrap gap-2">
              <Button
                className="px-3 py-2"
                variant="secondary"
                onClick={() => void savePhase()}
                isLoading={isBusy}
              >
                Guardar fase
              </Button>
              <Button
                className="px-3 py-2"
                variant="ghost"
                onClick={() => setConfirmDeletePhase(true)}
                isLoading={isBusy}
              >
                <Trash2 size={14} />
                Eliminar fase
              </Button>
            </div>
          )}
        </div>
      </div>

      <div className="space-y-4 border-t border-gray-100 px-5 py-4">
        {subphases.map((subphase) => (
          <SubphaseEditorRow
            key={subphase.id ?? `${phase.id}-${subphase.order}`}
            phaseId={phase.id ?? ''}
            subphase={subphase}
            isEditable={isEditable}
            isBusy={isBusy}
            onUpdateSubphase={onUpdateSubphase}
            onDeleteSubphase={onDeleteSubphase}
          />
        ))}

        {isEditable && (
          <div className="rounded-xl border border-dashed border-gray-300 p-4">
            <p className="text-label-md font-medium text-gray-700">Nueva subfase</p>
            <div className="mt-3 grid gap-3 md:grid-cols-2">
              <TextInput
                label="Nombre"
                value={newSubphase.name}
                onChange={(event) =>
                  onNewSubphaseChange({ ...newSubphase, name: event.target.value })
                }
              />
              <TextInput
                label="Orden"
                type="number"
                min={1}
                value={newSubphase.order}
                onChange={(event) =>
                  onNewSubphaseChange({ ...newSubphase, order: event.target.value })
                }
              />
              <TextInput
                label="Enlace HTTPS"
                value={newSubphase.referenceUrl}
                onChange={(event) =>
                  onNewSubphaseChange({ ...newSubphase, referenceUrl: event.target.value })
                }
              />
              <TextInput
                label="Descripción"
                value={newSubphase.description}
                onChange={(event) =>
                  onNewSubphaseChange({ ...newSubphase, description: event.target.value })
                }
              />
            </div>
            <Button className="mt-3 px-3 py-2" onClick={() => void addSubphase()} isLoading={isBusy}>
              <Plus size={14} />
              Agregar subfase
            </Button>
          </div>
        )}
      </div>
    </section>
      <ConfirmDialog
        isOpen={confirmDeletePhase}
        title="Eliminar fase"
        description="¿Eliminar esta fase y sus subfases elegibles? Esta acción no se puede deshacer."
        confirmLabel="Eliminar fase"
        isLoading={isBusy}
        onClose={() => setConfirmDeletePhase(false)}
        onConfirm={() => void removePhase()}
      />
    </>
  );
}

interface SubphaseEditorRowProps {
  phaseId: string;
  subphase: SubphaseDto;
  isEditable: boolean;
  isBusy: boolean;
  onUpdateSubphase: (
    phaseId: string,
    subphaseId: string,
    draft: SubphaseDraft,
  ) => Promise<boolean>;
  onDeleteSubphase: (phaseId: string, subphaseId: string) => Promise<boolean>;
}

function SubphaseEditorRow({
  phaseId,
  subphase,
  isEditable,
  isBusy,
  onUpdateSubphase,
  onDeleteSubphase,
}: SubphaseEditorRowProps) {
  const [draft, setDraft] = useState<SubphaseDraft>(() => toSubphaseDraft(subphase));
  const [confirmDeleteSubphase, setConfirmDeleteSubphase] = useState(false);

  const saveSubphase = async () => {
    if (!subphase.id) return;
    const order = Number.parseInt(draft.order, 10);
    if (!draft.name.trim() || !draft.referenceUrl.trim() || Number.isNaN(order)) return;
    await onUpdateSubphase(phaseId, subphase.id, draft);
  };

  const removeSubphase = async () => {
    if (!subphase.id) return;
    setConfirmDeleteSubphase(false);
    await onDeleteSubphase(phaseId, subphase.id);
  };

  return (
    <>
    <div className="rounded-xl border border-gray-200 p-4">
      <div className="grid gap-3 md:grid-cols-2">
        <TextInput
          label="Subfase"
          value={draft.name}
          disabled={!isEditable}
          onChange={(event) => setDraft((c) => ({ ...c, name: event.target.value }))}
        />
        <TextInput
          label="Orden"
          type="number"
          min={1}
          value={draft.order}
          disabled={!isEditable}
          onChange={(event) => setDraft((c) => ({ ...c, order: event.target.value }))}
        />
        <TextInput
          label="Enlace HTTPS"
          value={draft.referenceUrl}
          disabled={!isEditable}
          onChange={(event) => setDraft((c) => ({ ...c, referenceUrl: event.target.value }))}
        />
        <TextInput
          label="Descripción"
          value={draft.description}
          disabled={!isEditable}
          onChange={(event) => setDraft((c) => ({ ...c, description: event.target.value }))}
        />
      </div>

      {draft.referenceUrl && (
        <a
          href={draft.referenceUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="mt-2 inline-flex items-center gap-1 text-body-md text-primary-600 hover:text-primary-800"
        >
          <ExternalLink size={14} />
          Ver enlace de referencia
        </a>
      )}

      {isEditable && (
        <div className="mt-3 flex flex-wrap gap-2">
          <Button
            className="px-3 py-2"
            variant="secondary"
            onClick={() => void saveSubphase()}
            isLoading={isBusy}
          >
            Guardar subfase
          </Button>
          <Button
            className="px-3 py-2"
            variant="ghost"
            onClick={() => setConfirmDeleteSubphase(true)}
            isLoading={isBusy}
          >
            <Trash2 size={14} />
            Eliminar
          </Button>
        </div>
      )}
    </div>
      <ConfirmDialog
        isOpen={confirmDeleteSubphase}
        title="Eliminar subfase"
        description={`¿Eliminar la subfase "${draft.name || subphase.name || 'sin nombre'}"? Esta acción no se puede deshacer.`}
        confirmLabel="Eliminar subfase"
        isLoading={isBusy}
        onClose={() => setConfirmDeleteSubphase(false)}
        onConfirm={() => void removeSubphase()}
      />
    </>
  );
}
