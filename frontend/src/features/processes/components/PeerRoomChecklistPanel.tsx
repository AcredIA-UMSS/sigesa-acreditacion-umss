export type PeerRoomChecklistItemView = {
  id?: string;
  itemCode: string;
  label: string;
  checked: boolean;
};

export type PeerRoomChecklistPanelProps = {
  items: PeerRoomChecklistItemView[];
  canEdit: boolean;
  isLoading: boolean;
  isSaving: boolean;
  errorMessage?: string;
  onToggle: (itemCode: string, checked: boolean) => void;
};

export function PeerRoomChecklistPanel({
  items,
  canEdit,
  isLoading,
  isSaving,
  errorMessage,
  onToggle,
}: PeerRoomChecklistPanelProps) {
  return (
    <section className="rounded-2xl border border-gray-200 bg-body p-6 shadow-sm">
      <h2 className="text-heading-lg font-semibold text-primary-800">
        Checklist Sala de Pares
      </h2>
      <p className="mt-1 text-body-md text-gray-600">
        Documentación, muestra de evidencias, guía de entrevistas y logística. Requerido para
        cerrar E6.
      </p>

      {errorMessage && <p className="mt-3 text-body-md text-danger">{errorMessage}</p>}

      {isLoading ? (
        <p className="mt-3 text-body-md text-gray-600">Cargando checklist…</p>
      ) : items.length === 0 ? (
        <p className="mt-3 text-body-md text-gray-500">
          El checklist se genera al congelar el expediente.
        </p>
      ) : (
        <ul className="mt-4 space-y-2">
          {items.map((item) => (
            <li
              key={item.itemCode}
              className="flex items-start gap-3 rounded-lg border border-gray-200 bg-gray-50 px-3 py-2"
            >
              <input
                id={`checklist-${item.itemCode}`}
                type="checkbox"
                className="mt-1 h-4 w-4 accent-primary"
                checked={item.checked}
                disabled={!canEdit || isSaving}
                onChange={(event) => onToggle(item.itemCode, event.target.checked)}
              />
              <label htmlFor={`checklist-${item.itemCode}`} className="text-body-md text-gray-800">
                {item.label}
              </label>
            </li>
          ))}
        </ul>
      )}

      {canEdit && items.length > 0 && (
        <p className="mt-3 text-label-md text-gray-500">
          {isSaving ? 'Guardando…' : 'Los cambios se guardan al marcar o desmarcar.'}
        </p>
      )}
    </section>
  );
}
