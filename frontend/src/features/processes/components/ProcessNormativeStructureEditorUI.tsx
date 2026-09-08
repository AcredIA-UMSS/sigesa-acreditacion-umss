import { ExternalLink, GitBranch, Layers, ListChecks, Plus, Scale, Trash2 } from 'lucide-react';
import { useState, type ReactNode } from 'react';
import type {
  NormativeIndicatorDto,
  NormativeLevel1NodeDto,
  NormativeLevel2NodeDto,
  NormativeLevel3NodeDto,
} from '../../../api/model';
import { Button } from '../../../components/ui/Button';
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog';
import { TextInput } from '../../../components/ui/TextInput';

export interface NormativeNodeDraft {
  name: string;
  order: string;
  description: string;
}

export interface NormativeIndicatorDraft {
  code: string;
  description: string;
  weight: string;
  order: string;
  referenceUrl: string;
}

interface ProcessNormativeStructureEditorUIProps {
  level1Nodes: NormativeLevel1NodeDto[];
  isEditable: boolean;
  isBusy: boolean;
  actionError: string | null;
  onAddLevel1: (draft: NormativeNodeDraft) => Promise<boolean>;
  onUpdateLevel1: (level1Id: string, draft: NormativeNodeDraft) => Promise<boolean>;
  onDeleteLevel1: (level1Id: string) => Promise<boolean>;
  onAddLevel2: (level1Id: string, draft: NormativeNodeDraft) => Promise<boolean>;
  onUpdateLevel2: (level2Id: string, draft: NormativeNodeDraft) => Promise<boolean>;
  onDeleteLevel2: (level2Id: string) => Promise<boolean>;
  onAddLevel3: (level2Id: string, draft: NormativeNodeDraft) => Promise<boolean>;
  onUpdateLevel3: (level3Id: string, draft: NormativeNodeDraft) => Promise<boolean>;
  onDeleteLevel3: (level3Id: string) => Promise<boolean>;
  onAddIndicator: (level3Id: string, draft: NormativeIndicatorDraft) => Promise<boolean>;
  onUpdateIndicator: (indicatorId: string, draft: NormativeIndicatorDraft) => Promise<boolean>;
  onDeleteIndicator: (indicatorId: string) => Promise<boolean>;
}

const emptyNode = (order: number): NormativeNodeDraft => ({
  name: '',
  order: String(order),
  description: '',
});

const emptyIndicator = (order: number): NormativeIndicatorDraft => ({
  code: '',
  description: '',
  weight: '1',
  order: String(order),
  referenceUrl: 'https://duea.umss.edu.bo/normativa/pendiente',
});

function toNodeDraft(node: { name?: string; order?: number; description?: string }): NormativeNodeDraft {
  return {
    name: node.name ?? '',
    order: String(node.order ?? 1),
    description: node.description ?? '',
  };
}

function toIndicatorDraft(indicator: NormativeIndicatorDto): NormativeIndicatorDraft {
  return {
    code: indicator.code ?? '',
    description: indicator.description ?? '',
    weight: String(indicator.weight ?? 1),
    order: String(indicator.order ?? 1),
    referenceUrl: indicator.referenceUrl ?? '',
  };
}

export function ProcessNormativeStructureEditorUI({
  level1Nodes,
  isEditable,
  isBusy,
  actionError,
  onAddLevel1,
  onUpdateLevel1,
  onDeleteLevel1,
  onAddLevel2,
  onUpdateLevel2,
  onDeleteLevel2,
  onAddLevel3,
  onUpdateLevel3,
  onDeleteLevel3,
  onAddIndicator,
  onUpdateIndicator,
  onDeleteIndicator,
}: ProcessNormativeStructureEditorUIProps) {
  const sorted = [...level1Nodes].sort((a, b) => (a.order ?? 0) - (b.order ?? 0));
  const [newLevel1, setNewLevel1] = useState<NormativeNodeDraft>(() =>
    emptyNode(sorted.length + 1),
  );

  if (!isEditable) {
    return (
      <div className="rounded-xl border border-warning/40 bg-warning/10 px-4 py-3 text-body-md text-gray-800">
        Este proceso no está en estado ACTIVE. La estructura es solo lectura.
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {actionError && (
        <div className="rounded-xl border border-danger/30 bg-danger/10 px-4 py-3 text-body-md text-danger">
          {actionError}
        </div>
      )}

      <section className="rounded-2xl border border-gray-200 bg-body p-6 shadow-sm">
        <h2 className="text-heading-md font-semibold text-primary-800">Agregar nivel 1</h2>
        <NodeDraftForm
          draft={newLevel1}
          onChange={setNewLevel1}
          onSubmit={async () => {
            const ok = await onAddLevel1(newLevel1);
            if (ok) {
              setNewLevel1(emptyNode(sorted.length + 2));
            }
            return ok;
          }}
          isBusy={isBusy}
          submitLabel="Agregar nivel 1"
        />
      </section>

      {sorted.map((level1) => (
        <Level1Editor
          key={level1.id ?? level1.name}
          level1={level1}
          isBusy={isBusy}
          onUpdateLevel1={onUpdateLevel1}
          onDeleteLevel1={onDeleteLevel1}
          onAddLevel2={onAddLevel2}
          onUpdateLevel2={onUpdateLevel2}
          onDeleteLevel2={onDeleteLevel2}
          onAddLevel3={onAddLevel3}
          onUpdateLevel3={onUpdateLevel3}
          onDeleteLevel3={onDeleteLevel3}
          onAddIndicator={onAddIndicator}
          onUpdateIndicator={onUpdateIndicator}
          onDeleteIndicator={onDeleteIndicator}
        />
      ))}
    </div>
  );
}

function Level1Editor({
  level1,
  isBusy,
  onUpdateLevel1,
  onDeleteLevel1,
  onAddLevel2,
  onUpdateLevel2,
  onDeleteLevel2,
  onAddLevel3,
  onUpdateLevel3,
  onDeleteLevel3,
  onAddIndicator,
  onUpdateIndicator,
  onDeleteIndicator,
}: {
  level1: NormativeLevel1NodeDto;
  isBusy: boolean;
  onUpdateLevel1: (level1Id: string, draft: NormativeNodeDraft) => Promise<boolean>;
  onDeleteLevel1: (level1Id: string) => Promise<boolean>;
  onAddLevel2: (level1Id: string, draft: NormativeNodeDraft) => Promise<boolean>;
  onUpdateLevel2: (level2Id: string, draft: NormativeNodeDraft) => Promise<boolean>;
  onDeleteLevel2: (level2Id: string) => Promise<boolean>;
  onAddLevel3: (level2Id: string, draft: NormativeNodeDraft) => Promise<boolean>;
  onUpdateLevel3: (level3Id: string, draft: NormativeNodeDraft) => Promise<boolean>;
  onDeleteLevel3: (level3Id: string) => Promise<boolean>;
  onAddIndicator: (level3Id: string, draft: NormativeIndicatorDraft) => Promise<boolean>;
  onUpdateIndicator: (indicatorId: string, draft: NormativeIndicatorDraft) => Promise<boolean>;
  onDeleteIndicator: (indicatorId: string) => Promise<boolean>;
}) {
  const level1Id = level1.id ?? '';
  const [draft, setDraft] = useState(() => toNodeDraft(level1));
  const [confirmDelete, setConfirmDelete] = useState(false);
  const level2Nodes = [...(level1.level2Nodes ?? [])].sort(
    (a, b) => (a.order ?? 0) - (b.order ?? 0),
  );
  const [newLevel2, setNewLevel2] = useState<NormativeNodeDraft>(() =>
    emptyNode(level2Nodes.length + 1),
  );

  return (
    <section className="rounded-2xl border border-primary-200 bg-body p-6 shadow-sm">
      <div className="mb-4 flex items-center gap-2">
        <Layers size={20} className="text-primary-600" />
        <h2 className="text-heading-md font-semibold text-primary-800">
          {level1.label ?? 'Nivel 1'}: {level1.name}
        </h2>
      </div>

      <NodeDraftForm
        draft={draft}
        onChange={setDraft}
        onSubmit={() => onUpdateLevel1(level1Id, draft)}
        isBusy={isBusy}
        submitLabel="Guardar nivel 1"
        extraActions={
          <Button variant="danger" onClick={() => setConfirmDelete(true)} disabled={isBusy}>
            <Trash2 size={16} />
            Eliminar
          </Button>
        }
      />

      <ConfirmDialog
        isOpen={confirmDelete}
        title="Eliminar nivel 1"
        description="Se eliminarán todos los subnodos e indicadores. No se puede deshacer."
        confirmLabel="Eliminar"
        isLoading={isBusy}
        onClose={() => setConfirmDelete(false)}
        onConfirm={() => {
          void onDeleteLevel1(level1Id).then((ok) => {
            if (ok) setConfirmDelete(false);
          });
        }}
      />

      <div className="mt-6 border-t border-gray-100 pt-6">
        <h3 className="text-heading-sm font-semibold text-gray-800">
          Agregar {level1.level2Nodes?.[0]?.label ?? 'nivel 2'}
        </h3>
        <NodeDraftForm
          draft={newLevel2}
          onChange={setNewLevel2}
          onSubmit={async () => {
            const ok = await onAddLevel2(level1Id, newLevel2);
            if (ok) setNewLevel2(emptyNode(level2Nodes.length + 2));
            return ok;
          }}
          isBusy={isBusy}
          submitLabel="Agregar subnodo"
        />
      </div>

      {level2Nodes.map((level2) => (
        <Level2Editor
          key={level2.id ?? level2.name}
          level2={level2}
          isBusy={isBusy}
          onUpdateLevel2={onUpdateLevel2}
          onDeleteLevel2={onDeleteLevel2}
          onAddLevel3={onAddLevel3}
          onUpdateLevel3={onUpdateLevel3}
          onDeleteLevel3={onDeleteLevel3}
          onAddIndicator={onAddIndicator}
          onUpdateIndicator={onUpdateIndicator}
          onDeleteIndicator={onDeleteIndicator}
        />
      ))}
    </section>
  );
}

function Level2Editor({
  level2,
  isBusy,
  onUpdateLevel2,
  onDeleteLevel2,
  onAddLevel3,
  onUpdateLevel3,
  onDeleteLevel3,
  onAddIndicator,
  onUpdateIndicator,
  onDeleteIndicator,
}: {
  level2: NormativeLevel2NodeDto;
  isBusy: boolean;
  onUpdateLevel2: (level2Id: string, draft: NormativeNodeDraft) => Promise<boolean>;
  onDeleteLevel2: (level2Id: string) => Promise<boolean>;
  onAddLevel3: (level2Id: string, draft: NormativeNodeDraft) => Promise<boolean>;
  onUpdateLevel3: (level3Id: string, draft: NormativeNodeDraft) => Promise<boolean>;
  onDeleteLevel3: (level3Id: string) => Promise<boolean>;
  onAddIndicator: (level3Id: string, draft: NormativeIndicatorDraft) => Promise<boolean>;
  onUpdateIndicator: (indicatorId: string, draft: NormativeIndicatorDraft) => Promise<boolean>;
  onDeleteIndicator: (indicatorId: string) => Promise<boolean>;
}) {
  const level2Id = level2.id ?? '';
  const [draft, setDraft] = useState(() => toNodeDraft(level2));
  const level3Nodes = [...(level2.level3Nodes ?? [])].sort(
    (a, b) => (a.order ?? 0) - (b.order ?? 0),
  );
  const [newLevel3, setNewLevel3] = useState<NormativeNodeDraft>(() =>
    emptyNode(level3Nodes.length + 1),
  );

  return (
    <div className="mt-6 rounded-xl border border-gray-200 bg-gray-50 p-4">
      <div className="mb-3 flex items-center gap-2">
        <GitBranch size={18} className="text-primary-600" />
        <h3 className="text-heading-sm font-semibold text-gray-800">
          {level2.label ?? 'Nivel 2'}: {level2.name}
        </h3>
      </div>
      <NodeDraftForm
        draft={draft}
        onChange={setDraft}
        onSubmit={() => onUpdateLevel2(level2Id, draft)}
        isBusy={isBusy}
        submitLabel="Guardar"
        extraActions={
          <Button variant="danger" onClick={() => onDeleteLevel2(level2Id)} disabled={isBusy}>
            <Trash2 size={16} />
          </Button>
        }
      />

      <div className="mt-4">
        <NodeDraftForm
          draft={newLevel3}
          onChange={setNewLevel3}
          onSubmit={async () => {
            const ok = await onAddLevel3(level2Id, newLevel3);
            if (ok) setNewLevel3(emptyNode(level3Nodes.length + 2));
            return ok;
          }}
          isBusy={isBusy}
          submitLabel={`Agregar ${level2.level3Nodes?.[0]?.label ?? 'nivel 3'}`}
        />
      </div>

      {level3Nodes.map((level3) => (
        <Level3Editor
          key={level3.id ?? level3.name}
          level3={level3}
          isBusy={isBusy}
          onUpdateLevel3={onUpdateLevel3}
          onDeleteLevel3={onDeleteLevel3}
          onAddIndicator={onAddIndicator}
          onUpdateIndicator={onUpdateIndicator}
          onDeleteIndicator={onDeleteIndicator}
        />
      ))}
    </div>
  );
}

function Level3Editor({
  level3,
  isBusy,
  onUpdateLevel3,
  onDeleteLevel3,
  onAddIndicator,
  onUpdateIndicator,
  onDeleteIndicator,
}: {
  level3: NormativeLevel3NodeDto;
  isBusy: boolean;
  onUpdateLevel3: (level3Id: string, draft: NormativeNodeDraft) => Promise<boolean>;
  onDeleteLevel3: (level3Id: string) => Promise<boolean>;
  onAddIndicator: (level3Id: string, draft: NormativeIndicatorDraft) => Promise<boolean>;
  onUpdateIndicator: (indicatorId: string, draft: NormativeIndicatorDraft) => Promise<boolean>;
  onDeleteIndicator: (indicatorId: string) => Promise<boolean>;
}) {
  const level3Id = level3.id ?? '';
  const [draft, setDraft] = useState(() => toNodeDraft(level3));
  const indicators = [...(level3.indicators ?? [])].sort(
    (a, b) => (a.order ?? 0) - (b.order ?? 0),
  );
  const [newIndicator, setNewIndicator] = useState<NormativeIndicatorDraft>(() =>
    emptyIndicator(indicators.length + 1),
  );

  return (
    <div className="mt-4 rounded-lg border border-gray-200 bg-body p-4">
      <h4 className="text-body-md font-semibold text-gray-800">
        {level3.label ?? 'Nivel 3'}: {level3.name}
      </h4>
      <NodeDraftForm
        draft={draft}
        onChange={setDraft}
        onSubmit={() => onUpdateLevel3(level3Id, draft)}
        isBusy={isBusy}
        submitLabel="Guardar"
        extraActions={
          <Button variant="danger" onClick={() => onDeleteLevel3(level3Id)} disabled={isBusy}>
            <Trash2 size={16} />
          </Button>
        }
      />

      <div className="mt-4 border-t border-gray-100 pt-4">
        <h5 className="mb-2 flex items-center gap-1 text-body-md font-semibold text-gray-800">
          <ListChecks size={16} />
          Indicadores
        </h5>
        <IndicatorDraftForm
          draft={newIndicator}
          onChange={setNewIndicator}
          onSubmit={async () => {
            const ok = await onAddIndicator(level3Id, newIndicator);
            if (ok) setNewIndicator(emptyIndicator(indicators.length + 2));
            return ok;
          }}
          isBusy={isBusy}
          submitLabel="Agregar indicador"
        />

        <ul className="mt-4 space-y-3">
          {indicators.map((indicator) => (
            <IndicatorEditorRow
              key={indicator.id ?? indicator.code}
              indicator={indicator}
              isBusy={isBusy}
              onUpdateIndicator={onUpdateIndicator}
              onDeleteIndicator={onDeleteIndicator}
            />
          ))}
        </ul>
      </div>
    </div>
  );
}

function IndicatorEditorRow({
  indicator,
  isBusy,
  onUpdateIndicator,
  onDeleteIndicator,
}: {
  indicator: NormativeIndicatorDto;
  isBusy: boolean;
  onUpdateIndicator: (indicatorId: string, draft: NormativeIndicatorDraft) => Promise<boolean>;
  onDeleteIndicator: (indicatorId: string) => Promise<boolean>;
}) {
  const indicatorId = indicator.id ?? '';
  const [draft, setDraft] = useState(() => toIndicatorDraft(indicator));

  return (
    <li className="rounded-lg border border-gray-200 bg-gray-50 p-3">
      <IndicatorDraftForm
        draft={draft}
        onChange={setDraft}
        onSubmit={() => onUpdateIndicator(indicatorId, draft)}
        isBusy={isBusy}
        submitLabel="Guardar indicador"
        extraActions={
          <Button variant="danger" onClick={() => onDeleteIndicator(indicatorId)} disabled={isBusy}>
            <Trash2 size={16} />
          </Button>
        }
      />
    </li>
  );
}

function NodeDraftForm({
  draft,
  onChange,
  onSubmit,
  isBusy,
  submitLabel,
  extraActions,
}: {
  draft: NormativeNodeDraft;
  onChange: (draft: NormativeNodeDraft) => void;
  onSubmit: () => Promise<boolean>;
  isBusy: boolean;
  submitLabel: string;
  extraActions?: ReactNode;
}) {
  return (
    <div className="mt-3 grid gap-3 md:grid-cols-2">
      <TextInput
        label="Nombre"
        value={draft.name}
        onChange={(e) => onChange({ ...draft, name: e.target.value })}
      />
      <TextInput
        label="Orden"
        type="number"
        value={draft.order}
        onChange={(e) => onChange({ ...draft, order: e.target.value })}
      />
      <TextInput
        label="Descripción"
        className="md:col-span-2"
        value={draft.description}
        onChange={(e) => onChange({ ...draft, description: e.target.value })}
      />
      <div className="flex flex-wrap gap-2 md:col-span-2">
        <Button
          variant="primary"
          isLoading={isBusy}
          onClick={() => {
            void onSubmit();
          }}
        >
          <Plus size={16} />
          {submitLabel}
        </Button>
        {extraActions}
      </div>
    </div>
  );
}

function IndicatorDraftForm({
  draft,
  onChange,
  onSubmit,
  isBusy,
  submitLabel,
  extraActions,
}: {
  draft: NormativeIndicatorDraft;
  onChange: (draft: NormativeIndicatorDraft) => void;
  onSubmit: () => Promise<boolean>;
  isBusy: boolean;
  submitLabel: string;
  extraActions?: ReactNode;
}) {
  return (
    <div className="grid gap-3 md:grid-cols-2">
      <TextInput
        label="Código"
        value={draft.code}
        onChange={(e) => onChange({ ...draft, code: e.target.value })}
      />
      <TextInput
        label="Orden"
        type="number"
        value={draft.order}
        onChange={(e) => onChange({ ...draft, order: e.target.value })}
      />
      <TextInput
        label="Ponderación"
        type="number"
        step="0.01"
        value={draft.weight}
        onChange={(e) => onChange({ ...draft, weight: e.target.value })}
      />
      <TextInput
        label="URL referencia (HTTPS)"
        value={draft.referenceUrl}
        onChange={(e) => onChange({ ...draft, referenceUrl: e.target.value })}
      />
      <TextInput
        label="Descripción"
        className="md:col-span-2"
        value={draft.description}
        onChange={(e) => onChange({ ...draft, description: e.target.value })}
      />
      <div className="flex flex-wrap items-center gap-2 md:col-span-2">
        <Button
          variant="primary"
          isLoading={isBusy}
          onClick={() => {
            void onSubmit();
          }}
        >
          <Scale size={16} />
          {submitLabel}
        </Button>
        {draft.referenceUrl && (
          <a
            href={draft.referenceUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center gap-1 text-body-md text-primary-600"
          >
            <ExternalLink size={14} />
            Vista previa enlace
          </a>
        )}
        {extraActions}
      </div>
    </div>
  );
}
