import { useState } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { FileText } from 'lucide-react';
import { ConfirmDialog } from '../../../components/ui/ConfirmDialog';
import { getGetProcessQueryKey } from '../../../api/endpoints/procesos-de-acreditación/procesos-de-acreditación';
import { deleteNormativeIndicatorEvidence } from '../../evidence/api/normativeIndicatorEvidenceApi';
import { EvidencePreviewDialog } from '../../evidence/components/EvidencePreviewDialog';
import { useEvidencePreview } from '../../evidence/hooks/useEvidencePreview';
import { useNormativeIndicatorCollaboration } from '../../evidence/hooks/useNormativeIndicatorCollaboration';
import { NormativeIndicatorEvidenceUploadSlot } from '../components/NormativeIndicatorEvidenceUploadSlot';
import { NormativeIndicatorReviewActions } from '../components/NormativeIndicatorReviewActions';
import { NormativeIndicatorSubsanationModal } from '../../evidence/components/NormativeIndicatorSubsanationModal';

export type NormativeIndicatorCollaborationSectionProps = {
  processId: string;
  indicatorId: string;
  indicatorLabel: string;
  indicatorStatus?: string;
  breadcrumb: string;
  canUpload: boolean;
  canSubsanate: boolean;
  canReview: boolean;
};

function NormativeIndicatorEvidenceList({
  evidences,
  isLoading,
  canSubsanate,
  canRetract,
  retractingId,
  retractError,
  onSubsanate,
  onRetract,
  onView,
}: {
  evidences: ReturnType<typeof useNormativeIndicatorCollaboration>['evidences'];
  isLoading: boolean;
  canSubsanate: boolean;
  canRetract: boolean;
  retractingId: string | null;
  retractError: string | null;
  onSubsanate?: (evidenceId: string, filename: string) => void;
  onRetract?: (evidenceId: string, filename: string) => void;
  onView?: (documentAssetId: string, filename: string) => void;
}) {
  return (
    <div className="rounded-lg border border-primary-100 bg-primary-50/40 p-3">
      <div className="mb-2 flex items-center gap-2 text-label-md font-semibold uppercase text-primary-700">
        <FileText size={16} aria-hidden />
        Evidencias cargadas ({evidences.length})
      </div>
      {isLoading && evidences.length === 0 ? (
        <p className="text-body-md text-gray-500">Cargando evidencias…</p>
      ) : evidences.length === 0 ? (
        <p className="text-body-md text-gray-500">Sin evidencias cargadas.</p>
      ) : (
        <ul className="space-y-2">
          {evidences.map((item) => (
            <li
              key={item.evidenceId}
              className="rounded-md border border-primary-100 bg-body px-3 py-2 text-body-md text-gray-800"
            >
              <p className="font-medium">{item.originalFilename}</p>
              <p className="text-body-md text-gray-600">{item.description}</p>
              <p className="mt-1 text-label-md text-gray-500">
                v{item.version}
                {item.folioCode ? ` · Folio ${item.folioCode}` : ''}
                {item.externalUrl ? ` · ${item.externalUrl}` : ''}
              </p>
              <div className="mt-2 flex flex-wrap items-center gap-2">
                {item.documentAssetId && onView && (
                  <button
                    type="button"
                    title="Ver evidencia"
                    aria-label="Ver evidencia"
                    className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-body-lg transition-colors hover:bg-primary-50"
                    onClick={() => onView(item.documentAssetId ?? '', item.originalFilename)}
                  >
                    👁️
                  </button>
                )}
                {item.externalUrl && (
                  <a
                    href={item.externalUrl}
                    target="_blank"
                    rel="noreferrer"
                    title="Abrir enlace"
                    aria-label="Abrir enlace"
                    className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-body-lg transition-colors hover:bg-primary-50"
                  >
                    🔗
                  </a>
                )}
                {canSubsanate && onSubsanate && (
                  <button
                    type="button"
                    title="Subsanar evidencia"
                    aria-label="Subsanar evidencia"
                    className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-body-lg transition-colors hover:bg-secondary-50"
                    onClick={() => onSubsanate(item.evidenceId, item.originalFilename)}
                  >
                    ✏️
                  </button>
                )}
                {canRetract && onRetract && (
                  <button
                    type="button"
                    title="Quitar / reemplazar"
                    aria-label="Quitar / reemplazar"
                    disabled={retractingId === item.evidenceId}
                    className="inline-flex h-9 w-9 items-center justify-center rounded-lg text-body-lg transition-colors hover:bg-danger/10 disabled:opacity-60"
                    onClick={() => onRetract(item.evidenceId, item.originalFilename)}
                  >
                    {retractingId === item.evidenceId ? '⏳' : '🗑️'}
                  </button>
                )}
              </div>
            </li>
          ))}
        </ul>
      )}
      {retractError && (
        <p className="mt-2 text-body-md text-danger" role="alert">
          {retractError}
        </p>
      )}
    </div>
  );
}

export function NormativeIndicatorCollaborationSection({
  processId,
  indicatorId,
  indicatorLabel,
  indicatorStatus,
  breadcrumb,
  canUpload,
  canSubsanate,
  canReview,
}: NormativeIndicatorCollaborationSectionProps) {
  const queryClient = useQueryClient();
  const { evidences, eligibility, hasOpenObservation, isLoading, error, reload } =
    useNormativeIndicatorCollaboration(indicatorId, canSubsanate);
  const preview = useEvidencePreview();

  const [subsanationTarget, setSubsanationTarget] = useState<{
    evidenceId: string;
    filename: string;
  } | null>(null);
  const [retractingId, setRetractingId] = useState<string | null>(null);
  const [retractError, setRetractError] = useState<string | null>(null);
  const [retractTarget, setRetractTarget] = useState<{
    evidenceId: string;
    filename: string;
  } | null>(null);

  const effectiveHasOpenObservation =
    hasOpenObservation || indicatorStatus === 'OBSERVADO';
  const canRetract = canUpload && indicatorStatus !== 'APROBADO';
  const effectiveCanUpload =
    canUpload && !effectiveHasOpenObservation && evidences.length === 0;
  const openObservationId = eligibility?.openObservationId;

  return (
    <div className="mt-3 space-y-2">
      {error && (
        <p className="text-body-md text-danger" role="alert">
          {error}
        </p>
      )}

      {effectiveHasOpenObservation && (
        <p className="rounded-md border border-warning/30 bg-warning/10 px-3 py-2 text-body-md text-gray-800">
          Hay una observación pendiente. Debe subsanar la evidencia; no se permiten cargas nuevas
          hasta resolverla.
        </p>
      )}

      <NormativeIndicatorEvidenceList
        evidences={evidences}
        isLoading={isLoading}
        canSubsanate={Boolean(eligibility?.canSubsanate && openObservationId)}
        canRetract={canRetract}
        retractingId={retractingId}
        retractError={retractError}
        onSubsanate={(evidenceId, filename) => setSubsanationTarget({ evidenceId, filename })}
        onRetract={(evidenceId, filename) => setRetractTarget({ evidenceId, filename })}
        onView={(documentAssetId, filename) => preview.open(documentAssetId, filename)}
      />

      <NormativeIndicatorEvidenceUploadSlot
        processId={processId}
        indicatorId={indicatorId}
        indicatorLabel={indicatorLabel}
        breadcrumb={breadcrumb}
        canUpload={effectiveCanUpload}
        onUploaded={() => void reload()}
      />

      {canReview && (
        <NormativeIndicatorReviewActions
          indicatorId={indicatorId}
          indicatorLabel={indicatorLabel}
          indicatorStatus={indicatorStatus}
          hasEvidences={evidences.length > 0}
          onCompleted={() => void reload()}
        />
      )}

      <ConfirmDialog
        isOpen={retractTarget !== null}
        title="Quitar evidencia"
        description={
          retractTarget
            ? `¿Quitar «${retractTarget.filename}»? Podrá cargar otra evidencia mientras el indicador no esté APROBADO.`
            : ''
        }
        confirmLabel="Quitar evidencia"
        cancelLabel="Cancelar"
        confirmVariant="danger"
        isLoading={retractingId !== null}
        onClose={() => {
          if (retractingId === null) {
            setRetractTarget(null);
          }
        }}
        onConfirm={() => {
          if (!retractTarget) {
            return;
          }
          const evidenceId = retractTarget.evidenceId;
          setRetractError(null);
          setRetractingId(evidenceId);
          void deleteNormativeIndicatorEvidence(evidenceId)
            .then(async () => {
              setRetractTarget(null);
              await Promise.all([
                reload(),
                queryClient.invalidateQueries({ queryKey: getGetProcessQueryKey(processId) }),
              ]);
            })
            .catch((err: unknown) => {
              setRetractError(
                err instanceof Error ? err.message : 'No se pudo quitar la evidencia.',
              );
            })
            .finally(() => setRetractingId(null));
        }}
      />

      <EvidencePreviewDialog
        isOpen={preview.isOpen}
        title={preview.title}
        objectUrl={preview.objectUrl}
        contentType={preview.contentType}
        isLoading={preview.isLoading}
        error={preview.error}
        onClose={preview.close}
        onDownload={preview.download}
      />

      {subsanationTarget && openObservationId && (
        <NormativeIndicatorSubsanationModal
          isOpen
          indicatorId={indicatorId}
          indicatorLabel={indicatorLabel}
          evidenceId={subsanationTarget.evidenceId}
          evidenceFilename={subsanationTarget.filename}
          observationId={openObservationId}
          onClose={() => setSubsanationTarget(null)}
          onSubsanated={() => {
            setSubsanationTarget(null);
            void reload();
          }}
        />
      )}
    </div>
  );
}
