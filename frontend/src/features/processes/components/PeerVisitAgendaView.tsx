import { ArrowLeft, Plus, Trash2 } from 'lucide-react';
import { Link } from 'react-router-dom';
import type { PeerVisitItemRequestDto, PeerVisitViewResponseDto } from '../../../api/model';
import { Button } from '../../../components/ui/Button';
import { Select } from '../../../components/ui/Select';
import { TextInput } from '../../../components/ui/TextInput';

export type PeerVisitDraft = {
  startsOn: string;
  endsOn: string;
  venue: string;
  room: string;
  status: string;
  notes: string;
  items: PeerVisitItemRequestDto[];
};

export interface PeerVisitAgendaViewProps {
  processId: string;
  view: PeerVisitViewResponseDto | undefined;
  draft: PeerVisitDraft;
  canEdit: boolean;
  isLoading: boolean;
  isSaving: boolean;
  errorMessage: string | null;
  onChange: (patch: Partial<PeerVisitDraft>) => void;
  onItemChange: (index: number, patch: PeerVisitItemRequestDto) => void;
  onAddItem: () => void;
  onRemoveItem: (index: number) => void;
  onSave: () => void;
}

const STATUS_OPTIONS = [
  { value: 'DRAFT', label: 'Borrador' },
  { value: 'SCHEDULED', label: 'Programada' },
  { value: 'IN_PROGRESS', label: 'En curso' },
  { value: 'COMPLETED', label: 'Completada' },
  { value: 'CANCELLED', label: 'Cancelada' },
];

const ITEM_TYPE_OPTIONS = [
  { value: 'INTERVIEW', label: 'Entrevista' },
  { value: 'FACILITY', label: 'Recinto' },
  { value: 'DOCUMENT', label: 'Documento de visita' },
  { value: 'SESSION', label: 'Sesión' },
];

export function PeerVisitAgendaView({
  processId,
  view,
  draft,
  canEdit,
  isLoading,
  isSaving,
  errorMessage,
  onChange,
  onItemChange,
  onAddItem,
  onRemoveItem,
  onSave,
}: PeerVisitAgendaViewProps) {
  const immutable = view?.visit?.status === 'COMPLETED';
  const editable = canEdit && !immutable;

  return (
    <div className="space-y-6">
      <div>
        <Link
          to={`/procesos/${processId}`}
          className="inline-flex items-center gap-2 text-body-md text-primary-700 hover:text-primary-500"
        >
          <ArrowLeft size={16} />
          Volver al proceso
        </Link>
        <h1 className="mt-3 text-heading-xl font-semibold text-primary-800">
          Agenda de visita de pares
        </h1>
        <p className="mt-1 text-body-md text-gray-700">
          Calendario, sede, sala e ítems de la semana de pares. No sustituye el congelamiento (UC-030).
        </p>
        {view?.careerName && (
          <p className="mt-1 text-body-md text-gray-600">{view.careerName}</p>
        )}
      </div>

      {isLoading && <div className="h-48 animate-pulse rounded-2xl bg-gray-200" />}

      {errorMessage && (
        <p className="rounded-xl border border-danger/30 bg-secondary-50 p-4 text-body-md text-secondary-800">
          {errorMessage}
        </p>
      )}

      {!isLoading && !view?.visit && !canEdit && (
        <p className="rounded-2xl border border-gray-200 bg-body p-6 text-body-md text-gray-600">
          Todavía no hay visita programada para este proceso.
        </p>
      )}

      {immutable && (
        <p className="rounded-xl border border-warning/40 bg-warning/10 p-4 text-body-md text-gray-800">
          La visita COMPLETED no puede modificarse.
        </p>
      )}

      {!isLoading && (view?.visit || canEdit) && (
        <section className="space-y-4 rounded-2xl border border-gray-200 bg-body p-6 shadow-sm">
          <div className="grid gap-4 md:grid-cols-2">
            <TextInput
              label="Inicio"
              type="date"
              value={draft.startsOn}
              disabled={!editable}
              onChange={(event) => onChange({ startsOn: event.target.value })}
            />
            <TextInput
              label="Fin"
              type="date"
              value={draft.endsOn}
              disabled={!editable}
              onChange={(event) => onChange({ endsOn: event.target.value })}
            />
            <TextInput
              label="Sede"
              value={draft.venue}
              disabled={!editable}
              onChange={(event) => onChange({ venue: event.target.value })}
            />
            <TextInput
              label="Sala"
              value={draft.room}
              disabled={!editable}
              onChange={(event) => onChange({ room: event.target.value })}
            />
            <Select
              label="Estado"
              options={STATUS_OPTIONS}
              value={draft.status}
              disabled={!editable}
              onChange={(event) => onChange({ status: event.target.value })}
            />
            <TextInput
              label="Notas"
              value={draft.notes}
              disabled={!editable}
              onChange={(event) => onChange({ notes: event.target.value })}
            />
          </div>

          <div className="flex items-center justify-between">
            <h2 className="text-heading-sm font-semibold text-primary-800">Ítems de la visita</h2>
            {editable && (
              <Button variant="ghost" onClick={onAddItem}>
                <Plus size={16} />
                Añadir ítem
              </Button>
            )}
          </div>

          {draft.items.length === 0 && (
            <p className="text-body-md text-gray-600">Sin ítems. Añada entrevistas, recintos o documentos.</p>
          )}

          <ul className="space-y-4">
            {draft.items.map((item, index) => (
              <li key={`${item.id ?? 'new'}-${index}`} className="rounded-xl border border-gray-200 p-4">
                <div className="grid gap-3 md:grid-cols-2">
                  <Select
                    label="Tipo"
                    options={ITEM_TYPE_OPTIONS}
                    value={item.type ?? 'DOCUMENT'}
                    disabled={!editable}
                    onChange={(event) => onItemChange(index, { ...item, type: event.target.value })}
                  />
                  <TextInput
                    label="Título"
                    value={item.title ?? ''}
                    disabled={!editable}
                    onChange={(event) => onItemChange(index, { ...item, title: event.target.value })}
                  />
                  <TextInput
                    label="Fecha y hora"
                    type="datetime-local"
                    value={item.scheduledAt ?? ''}
                    disabled={!editable}
                    onChange={(event) => onItemChange(index, { ...item, scheduledAt: event.target.value })}
                  />
                  <TextInput
                    label="Lugar"
                    value={item.location ?? ''}
                    disabled={!editable}
                    onChange={(event) => onItemChange(index, { ...item, location: event.target.value })}
                  />
                </div>
                {editable && (
                  <button
                    type="button"
                    className="mt-3 inline-flex items-center gap-1 text-label-md text-secondary hover:text-secondary-700"
                    onClick={() => onRemoveItem(index)}
                  >
                    <Trash2 size={14} />
                    Quitar
                  </button>
                )}
              </li>
            ))}
          </ul>

          {editable && (
            <Button onClick={onSave} isLoading={isSaving}>
              Guardar agenda
            </Button>
          )}
        </section>
      )}
    </div>
  );
}
