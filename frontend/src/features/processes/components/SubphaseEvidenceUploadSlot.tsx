import { useId, useState } from 'react';
import { CheckCircle2, FileUp, Loader2, Upload } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { Button } from '../../../components/ui/Button';
import { Select } from '../../../components/ui/Select';
import { uploadEvidence } from '../../evidence/api/uploadEvidence';
import type { UploadableIndicatorDto } from '../../evidence/api/fetchUploadableIndicators';
import { mapUploadError } from '../../evidence/hooks/mapUploadError';

const ACCEPTED_EXTENSIONS =
  '.pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg';
const ACCEPTED_LABEL = 'PDF, Word, Excel o imagen — máx. 50 MB';

export type SubphaseEvidenceUploadSlotProps = {
  processId: string;
  phaseName: string;
  subphaseId?: string;
  subphaseName: string;
  canUpload: boolean;
  indicators: UploadableIndicatorDto[];
  indicatorsLoading: boolean;
  indicatorsError: string | null;
};

export function SubphaseEvidenceUploadSlot({
  processId,
  phaseName,
  subphaseId,
  subphaseName,
  canUpload,
  indicators,
  indicatorsLoading,
  indicatorsError,
}: SubphaseEvidenceUploadSlotProps) {
  const navigate = useNavigate();
  const fieldId = useId();
  const [indicatorId, setIndicatorId] = useState('');
  const [description, setDescription] = useState('');
  const [file, setFile] = useState<File | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  const selected = indicators.find((item) => item.indicatorId === indicatorId);
  const indicatorOptions = [
    {
      value: '',
      label: indicatorsLoading
        ? 'Cargando indicadores…'
        : 'Seleccione un indicador',
    },
    ...indicators.map((item) => ({
      value: item.indicatorId,
      label: `${item.code} — ${item.title} (${item.currentState})`,
    })),
  ];

  const cargarUrl = `/evidencias/cargar?processId=${encodeURIComponent(processId)}${
    subphaseId ? `&subphaseId=${encodeURIComponent(subphaseId)}` : ''
  }&subphaseName=${encodeURIComponent(subphaseName)}`;

  const onSubmit = async () => {
    setErrorMessage(null);
    setSuccessMessage(null);

    if (!file) {
      setErrorMessage('Seleccione un archivo.');
      return;
    }

    // JD/TD: no tienen rol de carga; llevan al flujo UC-004.
    if (!canUpload) {
      navigate(cargarUrl);
      return;
    }

    if (!selected) {
      setErrorMessage('Seleccione un indicador PENDIENTE u OBSERVADO.');
      return;
    }
    if (!description.trim()) {
      setErrorMessage('Indique una descripción de la evidencia.');
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await uploadEvidence({
        indicatorId: selected.indicatorId,
        criterionId: selected.criterionId,
        description: `${description.trim()} [Subfase: ${subphaseName} · Fase: ${phaseName}]`,
        file,
      });
      if (response.status === 200 || response.status === 201) {
        setSuccessMessage(
          `Evidencia cargada (v${response.data.version}) — indicador ${response.data.currentState}`,
        );
        setFile(null);
        setDescription('');
        setIndicatorId('');
      }
    } catch (err) {
      setErrorMessage(
        mapUploadError(err instanceof Error ? err : new Error('Error al cargar')),
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="mt-3 rounded-lg border border-dashed border-gray-300 bg-gray-50 px-3 py-4">
      <p className="text-label-md font-medium uppercase tracking-wide text-gray-600">
        Archivo de evidencia
      </p>
      <p className="mt-1 text-body-md text-gray-600">
        Evidencias de la subfase «{subphaseName}». {ACCEPTED_LABEL}.
      </p>

      {canUpload && (
        <div className="mt-3 space-y-3">
          {indicatorsError && (
            <p className="text-body-md text-danger" role="alert">
              {indicatorsError}
            </p>
          )}
          <Select
            id={`${fieldId}-indicator`}
            label="Indicador"
            requiredMark
            options={indicatorOptions}
            value={indicatorId}
            disabled={isSubmitting || indicatorsLoading || indicators.length === 0}
            onChange={(event) => setIndicatorId(event.target.value)}
            helperText={
              selected
                ? `Criterio: ${selected.criterionCode} — ${selected.criterionTitle}`
                : 'Solo indicadores PENDIENTE/OBSERVADO de su carrera'
            }
          />
          <div>
            <label
              htmlFor={`${fieldId}-description`}
              className="mb-1 block text-label-md text-gray-700"
            >
              Descripción <span className="text-secondary">*</span>
            </label>
            <textarea
              id={`${fieldId}-description`}
              rows={2}
              value={description}
              disabled={isSubmitting}
              placeholder={`Ej.: Documento de respaldo para «${subphaseName}»`}
              className="w-full rounded-lg border border-gray-300 bg-body px-3 py-2 text-body-md text-gray-900 outline-none focus:border-primary-500 disabled:bg-gray-100"
              onChange={(event) => setDescription(event.target.value)}
            />
          </div>
        </div>
      )}

      {!canUpload && (
        <p className="mt-2 flex items-center gap-1 text-body-md text-gray-600">
          <FileUp size={14} className="text-primary-600" aria-hidden />
          Seleccione el archivo y pulse Subir evidencia para continuar en el
          formulario UC-004.
        </p>
      )}

      <label
        htmlFor={`${fieldId}-file`}
        className={`mt-3 flex cursor-pointer flex-col items-center justify-center rounded-xl border-2 border-dashed px-4 py-10 transition-colors ${
          isSubmitting
            ? 'cursor-not-allowed border-gray-200 bg-gray-100 opacity-60'
            : file
              ? 'border-primary-400 bg-body'
              : 'border-gray-300 bg-gray-50 hover:border-primary-400 hover:bg-primary-50'
        }`}
      >
        <Upload size={32} className="mb-3 text-primary-600" aria-hidden />
        <span className="text-body-md font-medium text-primary-800">
          {file ? file.name : 'Seleccionar archivo'}
        </span>
        <span className="mt-1 text-body-md text-gray-500">{ACCEPTED_LABEL}</span>
        <input
          id={`${fieldId}-file`}
          type="file"
          accept={ACCEPTED_EXTENSIONS}
          disabled={isSubmitting}
          className="sr-only"
          onChange={(event) => {
            setFile(event.target.files?.[0] ?? null);
            setErrorMessage(null);
            setSuccessMessage(null);
          }}
        />
      </label>

      {errorMessage && (
        <p className="mt-2 text-body-md text-danger" role="alert">
          {errorMessage}
        </p>
      )}
      {successMessage && (
        <p
          className="mt-2 flex items-center gap-1 text-body-md text-success"
          role="status"
        >
          <CheckCircle2 size={16} aria-hidden />
          {successMessage}
        </p>
      )}

      <Button
        type="button"
        onClick={() => void onSubmit()}
        disabled={isSubmitting}
        isLoading={isSubmitting}
        className="mt-3"
      >
        {isSubmitting ? (
          <Loader2 size={16} className="animate-spin" />
        ) : (
          <Upload size={16} />
        )}
        Subir evidencia
      </Button>
    </div>
  );
}
