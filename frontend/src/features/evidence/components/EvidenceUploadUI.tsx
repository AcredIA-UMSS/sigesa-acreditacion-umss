import type { ReactNode } from 'react';
import {
  AlertCircle,
  Bell,
  CheckCircle2,
  FileUp,
  Info,
  Loader2,
  Settings,
  Upload,
} from 'lucide-react';
import type { UploadEvidenceResponse } from '../../../api/model';
import type {
  EvidenceUploadField,
  EvidenceUploadForm,
  EvidenceUploadValidationErrors,
} from '../hooks/useEvidenceUpload';

const ACCEPTED_EXTENSIONS =
  '.pdf,.doc,.docx,.xls,.xlsx,.png,.jpg,.jpeg';
const MAX_FILE_SIZE_MB = 50;

export type EvidenceUploadUIProps = {
  form: EvidenceUploadForm;
  onFieldChange: <K extends EvidenceUploadField>(
    key: K,
    value: EvidenceUploadForm[K],
  ) => void;
  onSubmit: () => void;
  onReset: () => void;
  progress: number;
  isLargeFile: boolean;
  isSubmitting: boolean;
  isBlocked: boolean;
  result: UploadEvidenceResponse | null;
  errorMessage: string | null;
  validationErrors: EvidenceUploadValidationErrors;
};

export function EvidenceUploadUI({
  form,
  onFieldChange,
  onSubmit,
  onReset,
  progress,
  isLargeFile,
  isSubmitting,
  isBlocked,
  result,
  errorMessage,
  validationErrors,
}: EvidenceUploadUIProps) {
  const showProgress = isSubmitting || progress > 0;

  return (
    <div className="flex flex-1 flex-col h-screen overflow-hidden bg-gray-50">
      <header className="flex items-center justify-between border-b border-gray-200 bg-body px-8 py-4">
        <nav className="text-body-md text-gray-600" aria-label="Ruta de navegación">
          <span className="text-primary-600">Inicio</span>
          <span className="mx-2 text-gray-400">/</span>
          <span className="text-gray-700">Cargar evidencia</span>
        </nav>
        <div className="flex items-center gap-4 text-gray-600">
          <button
            type="button"
            className="relative hover:text-primary-600"
            aria-label="Notificaciones"
          >
            <Bell size={24} />
            <span className="absolute right-0 top-0 h-2 w-2 rounded-full bg-secondary" />
          </button>
          <button
            type="button"
            className="hover:text-primary-600"
            aria-label="Configuración"
          >
            <Settings size={24} />
          </button>
        </div>
      </header>

      <main className="flex-1 overflow-y-auto p-8">
        <div className="mx-auto max-w-6xl">
          <header className="mb-8">
            <div className="mb-4 h-1 w-12 bg-secondary" />
            <h1 className="mb-2 text-heading-xl text-primary-800">
              Cargar Evidencia
            </h1>
            <p className="text-body-lg text-gray-600">
              FSD-UC-004 — El coordinador de carrera adjunta el archivo con los
              metadatos obligatorios. El indicador pasará a estado{' '}
              <strong className="font-semibold text-primary-700">SUBIDO</strong>{' '}
              tras una carga exitosa.
            </p>
          </header>

          <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
            <section className="space-y-6 lg:col-span-2">
              <form
                className="rounded-2xl border border-gray-100 bg-body p-8 shadow-sm"
                onSubmit={(event) => {
                  event.preventDefault();
                  onSubmit();
                }}
                noValidate
              >
                <div className="mb-6 flex items-center gap-4">
                  <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary-50 text-primary-600">
                    <FileUp size={24} />
                  </div>
                  <div>
                    <h2 className="text-heading-md text-primary-800">
                      Metadatos y archivo
                    </h2>
                    <p className="text-body-md text-gray-500">
                      Indicador, criterio, descripción y documento de respaldo
                    </p>
                  </div>
                </div>

                <div className="space-y-5">
                  <FormField
                    label="INDICADOR"
                    htmlFor="indicator-id"
                    error={validationErrors.indicatorId}
                  >
                    <input
                      id="indicator-id"
                      type="text"
                      value={form.indicatorId}
                      disabled={isBlocked}
                      placeholder="UUID del indicador"
                      className={inputClass(!!validationErrors.indicatorId)}
                      onChange={(event) =>
                        onFieldChange('indicatorId', event.target.value)
                      }
                    />
                  </FormField>

                  <FormField
                    label="CRITERIO"
                    htmlFor="criterion-id"
                    error={validationErrors.criterionId}
                  >
                    <input
                      id="criterion-id"
                      type="text"
                      value={form.criterionId}
                      disabled={isBlocked}
                      placeholder="UUID del criterio asociado"
                      className={inputClass(!!validationErrors.criterionId)}
                      onChange={(event) =>
                        onFieldChange('criterionId', event.target.value)
                      }
                    />
                  </FormField>

                  <FormField
                    label="DESCRIPCIÓN"
                    htmlFor="description"
                    error={validationErrors.description}
                  >
                    <textarea
                      id="description"
                      rows={4}
                      value={form.description}
                      disabled={isBlocked}
                      placeholder="Describa el contenido y propósito de la evidencia"
                      className={inputClass(!!validationErrors.description)}
                      onChange={(event) =>
                        onFieldChange('description', event.target.value)
                      }
                    />
                  </FormField>

                  <FormField
                    label="ARCHIVO DE EVIDENCIA"
                    htmlFor="evidence-file"
                    error={validationErrors.file}
                  >
                    <label
                      htmlFor="evidence-file"
                      className={`flex cursor-pointer flex-col items-center justify-center rounded-xl border-2 border-dashed px-6 py-10 transition-colors ${
                        isBlocked
                          ? 'cursor-not-allowed border-gray-200 bg-gray-50 opacity-60'
                          : validationErrors.file
                            ? 'border-danger bg-danger/5 hover:border-danger'
                            : 'border-gray-300 bg-gray-50 hover:border-primary-400 hover:bg-primary-50'
                      }`}
                    >
                      <Upload
                        size={32}
                        className="mb-3 text-primary-600"
                        aria-hidden
                      />
                      <span className="text-body-md font-medium text-primary-800">
                        {form.file ? form.file.name : 'Seleccionar archivo'}
                      </span>
                      <span className="mt-1 text-body-md text-gray-500">
                        PDF, Word, Excel o imagen — máx. {MAX_FILE_SIZE_MB} MB
                      </span>
                      {form.file && (
                        <span className="mt-2 text-label-md text-gray-600">
                          {(form.file.size / (1024 * 1024)).toFixed(2)} MB
                        </span>
                      )}
                      <input
                        id="evidence-file"
                        type="file"
                        accept={ACCEPTED_EXTENSIONS}
                        disabled={isBlocked}
                        className="sr-only"
                        onChange={(event) =>
                          onFieldChange(
                            'file',
                            event.target.files?.[0] ?? null,
                          )
                        }
                      />
                    </label>
                    {isLargeFile && (
                      <p className="mt-2 flex items-center gap-1 text-body-md text-warning">
                        <Info size={16} aria-hidden />
                        Archivo mayor a 5 MB — se mostrará progreso de carga
                        (US-025).
                      </p>
                    )}
                  </FormField>

                  {showProgress && (
                    <div aria-live="polite">
                      <div className="mb-1 flex items-center justify-between text-label-md text-gray-600">
                        <span>Progreso de carga</span>
                        <span>{progress}%</span>
                      </div>
                      <div
                        className="h-2 w-full overflow-hidden rounded-full bg-gray-200"
                        role="progressbar"
                        aria-valuenow={progress}
                        aria-valuemin={0}
                        aria-valuemax={100}
                      >
                        <div
                          className="h-full bg-primary-600 transition-all duration-300"
                          style={{ width: `${progress}%` }}
                        />
                      </div>
                    </div>
                  )}

                  {errorMessage && (
                    <Alert message={errorMessage} />
                  )}

                  {result && (
                    <div
                      className="rounded-xl border border-success/30 bg-success/5 p-4"
                      role="status"
                    >
                      <div className="mb-2 flex items-center gap-2 text-heading-sm text-success">
                        <CheckCircle2 size={20} aria-hidden />
                        Carga exitosa
                      </div>
                      <dl className="space-y-1 text-body-md text-gray-700">
                        <div className="flex flex-wrap gap-x-2">
                          <dt className="font-medium">Evidencia:</dt>
                          <dd className="font-mono text-code">{result.evidenceId}</dd>
                        </div>
                        <div className="flex flex-wrap gap-x-2">
                          <dt className="font-medium">Versión:</dt>
                          <dd>{result.version}</dd>
                        </div>
                        <div className="flex flex-wrap gap-x-2">
                          <dt className="font-medium">Estado indicador:</dt>
                          <dd className="font-semibold text-primary-700">
                            {result.currentState}
                          </dd>
                        </div>
                        <div>
                          <dt className="font-medium">SHA-256</dt>
                          <dd className="mt-1 break-all font-mono text-code text-gray-600">
                            {result.contentHash}
                          </dd>
                        </div>
                      </dl>
                    </div>
                  )}

                  <div className="flex flex-wrap gap-3 pt-2">
                    <button
                      type="submit"
                      disabled={isSubmitting || isBlocked}
                      className="inline-flex items-center gap-2 rounded-lg bg-primary-600 px-5 py-3 text-label-md font-semibold text-body transition-colors hover:bg-primary-700 disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      {isSubmitting ? (
                        <>
                          <Loader2 size={18} className="animate-spin" aria-hidden />
                          Cargando…
                        </>
                      ) : (
                        <>
                          <Upload size={18} aria-hidden />
                          Subir evidencia
                        </>
                      )}
                    </button>
                    {(result || form.file) && !isSubmitting && (
                      <button
                        type="button"
                        onClick={onReset}
                        className="rounded-lg border border-gray-300 bg-body px-5 py-3 text-label-md font-medium text-gray-700 transition-colors hover:bg-gray-50"
                      >
                        Nueva carga
                      </button>
                    )}
                  </div>
                </div>
              </form>
            </section>

            <aside className="space-y-6">
              <section className="rounded-2xl border border-gray-200 bg-gray-100 p-6">
                <div className="mb-4 flex items-center gap-2 text-primary-800">
                  <Info size={20} aria-hidden />
                  <h3 className="text-heading-sm">Guía rápida</h3>
                </div>
                <ol className="space-y-4">
                  <GuideStep
                    step="01"
                    text="Navegue al indicador en estado PENDIENTE u OBSERVADO dentro de su carrera."
                  />
                  <GuideStep
                    step="02"
                    text="Complete indicador, criterio y una descripción clara del documento."
                  />
                  <GuideStep
                    step="03"
                    text="Tras la carga, el indicador queda en SUBIDO y se notifica al técnico DUEA."
                  />
                </ol>
              </section>

              <section className="rounded-2xl border border-primary-100 bg-primary-50 p-6">
                <h3 className="mb-3 text-heading-sm text-primary-800">
                  Formatos admitidos
                </h3>
                <ul className="space-y-2 text-body-md text-primary-700">
                  <li>Documentos: PDF, DOC, DOCX</li>
                  <li>Hojas de cálculo: XLS, XLSX</li>
                  <li>Imágenes: PNG, JPEG</li>
                </ul>
                <p className="mt-4 text-label-md text-primary-600">
                  Tamaño máximo: {MAX_FILE_SIZE_MB} MB por archivo
                </p>
              </section>
            </aside>
          </div>
        </div>
      </main>
    </div>
  );
}

type FormFieldProps = {
  label: string;
  htmlFor: string;
  error?: string;
  children: ReactNode;
};

function FormField({ label, htmlFor, error, children }: FormFieldProps) {
  return (
    <div>
      <label
        htmlFor={htmlFor}
        className="mb-1 block text-label-md text-gray-600"
      >
        {label}
      </label>
      {children}
      {error && (
        <p className="mt-1 text-body-md text-danger" role="alert">
          {error}
        </p>
      )}
    </div>
  );
}

function inputClass(hasError: boolean): string {
  const base =
    'w-full rounded-lg border p-3 text-body-md text-gray-800 focus:outline-none disabled:cursor-not-allowed disabled:bg-gray-100';
  return hasError
    ? `${base} border-danger focus:border-danger`
    : `${base} border-gray-300 focus:border-primary-500`;
}

function Alert({ message }: { message: string }) {
  return (
    <div
      className="flex items-start gap-2 rounded-lg border border-danger/30 bg-danger/5 p-4 text-body-md text-danger"
      role="alert"
    >
      <AlertCircle size={20} className="mt-0.5 shrink-0" aria-hidden />
      <span>{message}</span>
    </div>
  );
}

function GuideStep({ step, text }: { step: string; text: string }) {
  return (
    <li className="flex gap-3">
      <span className="text-heading-md font-bold text-gray-400">{step}</span>
      <p className="text-body-md text-gray-600">{text}</p>
    </li>
  );
}
