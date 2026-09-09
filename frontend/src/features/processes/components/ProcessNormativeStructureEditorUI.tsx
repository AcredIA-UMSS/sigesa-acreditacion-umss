import { ExternalLink, GitBranch, Layers, ListChecks, Plus, Scale, Trash2 } from 'lucide-react';
import { useState, type ReactNode } from 'react';
import type {
  NormativeIndicatorDto,
  NormativeLevel1NodeDto,
  NormativeLevel2NodeDto,
  NormativeLevel3NodeDto,
} from '../../../api/model';
import { NormativeCollapsibleLayer } from '../../../components/normative/NormativeCollapsibleLayer';
import { Button } from '../../../components/ui/Button';
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog';
import { TextInput } from '../../../components/ui/TextInput';
import {
  countIndicatorsUnderLevel1,
  countIndicatorsUnderLevel2,
  sortByOrder,
} from '../lib/normativeTreeUtils';

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
  const sorted = sortByOrder(level1Nodes);
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
    <div className="space-y-4">
      {actionError && (
        <div className="rounded-xl border border-danger/30 bg-danger/10 px-4 py-3 text-body-md text-danger">
          {actionError}
        </div>
      )}

      <p className="text-body-md text-gray-600">
        Solo se listan las dimensiones (nivel 1). Despliegue cada fila con ▸ para editar áreas,
        criterios e indicadores.
      </p>

      <NormativeCollapsibleLayer
        depth={1}
        title="Agregar nueva dimensión (nivel 1)"
        meta="Formulario de alta"
        leadingIcon={<Plus size={18} className="text-primary-600" />}
      >
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
      </NormativeCollapsibleLayer>

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
  const level2Nodes = sortByOrder(level1.level2Nodes ?? []);
  const [newLevel2, setNewLevel2] = useState<NormativeNodeDraft>(() =>
    emptyNode(level2Nodes.length + 1),
  );
  const level1Label = level1.label ?? 'Dimensión';
  const indicatorCount = countIndicatorsUnderLevel1(level1);

  return (
    <NormativeCollapsibleLayer
      depth={1}
      title={level1.name ?? level1Label}
      meta={`${level1Label} ${level1.order ?? '—'} · ${level2Nodes.length} subnivel(es) · ${indicatorCount} indicador(es)`}
      leadingIcon={<Layers size={18} className="text-primary-600" />}
    >
      <NodeDraftForm
        draft={draft}
        onChange={setDraft}
        onSubmit={() => onUpdateLevel1(level1Id, draft)}
        isBusy={isBusy}
        submitLabel="Guardar dimensión"
        extraActions={
          <Button variant="danger" onClick={() => setConfirmDelete(true)} disabled={isBusy}>
            <Trash2 size={16} />
            Eliminar
          </Button>
        }
      />

      <ConfirmDialog
        isOpen={confirmDelete}
        title="Eliminar dimensión"
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

      <div className="mt-4 space-y-2 border-t border-gray-100 pt-4">
        <NormativeCollapsibleLayer
          depth={2}
          title={`Agregar ${level1.level2Nodes?.[0]?.label ?? 'área (nivel 2)'}`}
          meta="Formulario de alta"
          leadingIcon={<Plus size={16} className="text-primary-600" />}
        >
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
        </NormativeCollapsibleLayer>

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
      </div>
    </NormativeCollapsibleLayer>
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
  const level3Nodes = sortByOrder(level2.level3Nodes ?? []);
  const [newLevel3, setNewLevel3] = useState<NormativeNodeDraft>(() =>
    emptyNode(level3Nodes.length + 1),
  );
  const level2Label = level2.label ?? 'Área';
  const indicatorCount = countIndicatorsUnderLevel2(level2);

  return (
    <NormativeCollapsibleLayer
      depth={2}
      title={level2.name ?? level2Label}
      meta={`${level2Label} ${level2.order ?? '—'} · ${level3Nodes.length} subnivel(es) · ${indicatorCount} indicador(es)`}
      leadingIcon={<GitBranch size={16} className="text-primary-600" />}
    >
      <NodeDraftForm
        draft={draft}
        onChange={setDraft}
        onSubmit={() => onUpdateLevel2(level2Id, draft)}
        isBusy={isBusy}
        submitLabel="Guardar área"
        extraActions={
          <Button variant="danger" onClick={() => onDeleteLevel2(level2Id)} disabled={isBusy}>
            <Trash2 size={16} />
            Eliminar
          </Button>
        }
      />

      <div className="mt-4 space-y-2 border-t border-gray-100 pt-4">
        <NormativeCollapsibleLayer
          depth={3}
          title={`Agregar ${level2.level3Nodes?.[0]?.label ?? 'criterio (nivel 3)'}`}
          meta="Formulario de alta"
          leadingIcon={<Plus size={16} className="text-primary-600" />}
        >
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
        </NormativeCollapsibleLayer>

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
    </NormativeCollapsibleLayer>
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
  const indicators = sortByOrder(level3.indicators ?? []);
  const [newIndicator, setNewIndicator] = useState<NormativeIndicatorDraft>(() =>
    emptyIndicator(indicators.length + 1),
  );
  const level3Label = level3.label ?? 'Criterio';

  return (
    <NormativeCollapsibleLayer
      depth={3}
      title={level3.name ?? level3Label}
      meta={`${level3Label} ${level3.order ?? '—'} · ${indicators.length} indicador(es)`}
      leadingIcon={<ListChecks size={16} className="text-primary-600" />}
    >
      <NodeDraftForm
        draft={draft}
        onChange={setDraft}
        onSubmit={() => onUpdateLevel3(level3Id, draft)}
        isBusy={isBusy}
        submitLabel="Guardar criterio"
        extraActions={
          <Button variant="danger" onClick={() => onDeleteLevel3(level3Id)} disabled={isBusy}>
            <Trash2 size={16} />
            Eliminar
          </Button>
        }
      />

      <div className="mt-4 space-y-2 border-t border-gray-100 pt-4">
        <NormativeCollapsibleLayer
          depth={4}
          title="Agregar indicador"
          meta="Formulario de alta"
          leadingIcon={<Plus size={14} className="text-primary-600" />}
        >
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
        </NormativeCollapsibleLayer>

        <ul className="space-y-2">
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
    </NormativeCollapsibleLayer>
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
  const title = indicator.code
    ? `${indicator.code} — ${indicator.description ?? 'Indicador'}`
    : (indicator.description ?? 'Indicador');

  return (
    <li className="list-none">
      <NormativeCollapsibleLayer depth={4} title={title} meta={`Orden ${indicator.order ?? '—'}`}>
        <IndicatorDraftForm
          draft={draft}
          onChange={setDraft}
          onSubmit={() => onUpdateIndicator(indicatorId, draft)}
          isBusy={isBusy}
          submitLabel="Guardar indicador"
          extraActions={
            <Button variant="danger" onClick={() => onDeleteIndicator(indicatorId)} disabled={isBusy}>
              <Trash2 size={16} />
              Eliminar
            </Button>
          }
        />
      </NormativeCollapsibleLayer>
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
