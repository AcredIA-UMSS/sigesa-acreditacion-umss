import type { AuditLogResponse } from '../../../api/model/auditLogResponse';
import { Button } from '../../../components/ui/Button';
import { Alert } from '../../../components/ui/Alert';
import {
  actionLabel,
  actorLabel,
  resourceLabel,
  resultLabel,
  safePayloadFields,
  splitOccurredAt,
  type ActorDirectory,
} from '../lib/auditDisplay';

interface AuditDetailPanelProps {
  event: AuditLogResponse | undefined;
  directory: ActorDirectory;
  isLoading: boolean;
  errorMessage: string | null;
  onClose: () => void;
}

export function AuditDetailPanel({
  event,
  directory,
  isLoading,
  errorMessage,
  onClose,
}: AuditDetailPanelProps) {
  const when = splitOccurredAt(event?.occurredAt);
  const payload = safePayloadFields(event?.payload);

  return (
    <section className="rounded-2xl border border-gray-100 bg-body p-6 shadow-sm" data-testid="audit-detail">
      <div className="mb-4 flex items-start justify-between gap-4">
        <h2 className="text-heading-md text-primary-800">Detalle del registro</h2>
        <Button type="button" variant="ghost" onClick={onClose}>Cerrar</Button>
      </div>
      {isLoading && <p className="text-body-md text-gray-500">Cargando detalle…</p>}
      {errorMessage && <Alert variant="error">{errorMessage}</Alert>}
      {event && (
        <dl className="grid gap-4 sm:grid-cols-2">
          <DetailItem label="Usuario" value={actorLabel(event, directory)} />
          <DetailItem label="Fecha y hora" value={`${when.date} ${when.time}`} />
          <DetailItem label="Acción" value={actionLabel(event.action)} />
          <DetailItem label="Módulo" value={resourceLabel(event.resourceType)} />
          <DetailItem label="Identificador" value={event.resourceId ?? '—'} />
          <DetailItem label="Resultado" value={resultLabel(event.result)} />
          <DetailItem label="Descripción" value={event.description?.trim() || '—'} />
          <DetailItem label="Dirección IP" value={event.ipAddress?.trim() || '—'} />
          {payload.map((field) => (
            <DetailItem key={field.label} label={field.label} value={field.value} />
          ))}
        </dl>
      )}
    </section>
  );
}

function DetailItem({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <dt className="text-label-md text-gray-500">{label}</dt>
      <dd className="mt-1 break-all text-body-md text-gray-800">{value}</dd>
    </div>
  );
}
