import type { ReactNode } from 'react';
import {
  AlertCircle,
  BarChart3,
  Bell,
  CheckCircle2,
  Download,
  FileText,
  Info,
  Loader2,
  Settings,
} from 'lucide-react';
import type { ReportJobStatusResponse } from '../../../api/model';
import {
  SEMAPHORE_LABELS,
  type ReportPreviewModel,
  type SemaphoreTone,
} from '../lib/reportPreview';
import {
  getJobProgressPercent,
  JOB_STATUS_LABELS,
  mapJobErrorCode,
} from '../hooks/mapReportError';
import type {
  ExecutiveReportField,
  ExecutiveReportFormState,
  ExecutiveReportValidationErrors,
} from '../hooks/useExecutiveReport';

export type ExecutiveReportUIProps = {
  form: ExecutiveReportFormState;
  onFieldChange: <K extends ExecutiveReportField>(
    key: K,
    value: ExecutiveReportFormState[K],
  ) => void;
  onSubmit: () => void;
  onDownload: () => void;
  onReset: () => void;
  activeJobId: string | null;
  jobStatus: ReportJobStatusResponse | undefined;
  preview: ReportPreviewModel;
  validationErrors: ExecutiveReportValidationErrors;
  submitErrorMessage: string | null;
  statusErrorMessage: string | null;
  downloadErrorMessage: string | null;
  isSubmitting: boolean;
  isDownloading: boolean;
  isPolling: boolean;
  isBlocked: boolean;
};

export function ExecutiveReportUI({
  form,
  onFieldChange,
  onSubmit,
  onDownload,
  onReset,
  activeJobId,
  jobStatus,
  preview,
  validationErrors,
  submitErrorMessage,
  statusErrorMessage,
  downloadErrorMessage,
  isSubmitting,
  isDownloading,
  isPolling,
  isBlocked,
}: ExecutiveReportUIProps) {
  const progressPercent = getJobProgressPercent(jobStatus?.status);
  const statusKey = jobStatus?.status;
  const statusLabel = statusKey
    ? (JOB_STATUS_LABELS[statusKey] ?? statusKey)
    : null;
  const jobErrorMessage = mapJobErrorCode(jobStatus?.errorCode);
  const showJobPanel = activeJobId !== null || jobStatus !== undefined;
  const isCompleted = jobStatus?.status === 'COMPLETED';

  return (
    <div className="flex h-screen flex-1 flex-col overflow-hidden bg-gray-50">
      <header className="flex items-center justify-between border-b border-gray-200 bg-body px-8 py-4">
        <nav className="text-body-md text-gray-600" aria-label="Ruta de navegación">
          <span className="text-primary-600">Inicio</span>
          <span className="mx-2 text-gray-400">/</span>
          <span className="text-gray-700">Reportes</span>
          <span className="mx-2 text-gray-400">/</span>
          <span className="text-gray-700">Ejecutivo PDF</span>
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

      <main className="flex-1 overflow-y-auto p-6 md:p-8">
        <div className="mx-auto max-w-7xl">
          <header className="mb-8">
            <div className="mb-4 h-1 w-12 bg-secondary" />
            <h1 className="mb-2 text-heading-xl text-primary-800">
              Reporte Ejecutivo PDF
            </h1>
            <p className="max-w-3xl text-body-lg text-gray-600">
              Genere el PDF institucional con filtros, semáforo ejecutivo y avance
              por programa. La vista previa refleja el contenido del documento.
            </p>
          </header>

          <div className="grid grid-cols-1 gap-6 xl:grid-cols-12">
            <section className="space-y-6 xl:col-span-5">
              <form
                className="rounded-2xl border border-gray-100 bg-body p-6 shadow-sm md:p-8"
                onSubmit={(event) => {
                  event.preventDefault();
                  onSubmit();
                }}
                noValidate
              >
                <div className="mb-6 flex items-center gap-4">
                  <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary-50 text-primary-600">
                    <BarChart3 size={24} />
                  </div>
                  <div>
                    <h2 className="text-heading-md text-primary-800">
                      Filtros del reporte
                    </h2>
                    <p className="text-body-md text-gray-500">
                      Año obligatorio; facultad y programa opcionales
                    </p>
                  </div>
                </div>

                <div className="space-y-5">
                  <FormField
                    label="AÑO DE GESTIÓN"
                    htmlFor="management-year"
                    error={validationErrors.managementYear}
                  >
                    <input
                      id="management-year"
                      type="number"
                      min={2000}
                      max={2100}
                      value={form.managementYear}
                      disabled={isBlocked}
                      className={inputClass(!!validationErrors.managementYear)}
                      onChange={(event) =>
                        onFieldChange(
                          'managementYear',
                          Number(event.target.value),
                        )
                      }
                    />
                  </FormField>

                  <FormField
                    label="FACULTAD (OPCIONAL)"
                    htmlFor="faculty-id"
                    hint="Vacío = Todas las facultades"
                    error={validationErrors.facultyId}
                  >
                    <input
                      id="faculty-id"
                      type="text"
                      value={form.facultyId}
                      disabled={isBlocked}
                      placeholder="UUID de facultad"
                      className={inputClass(!!validationErrors.facultyId)}
                      onChange={(event) =>
                        onFieldChange('facultyId', event.target.value)
                      }
                    />
                  </FormField>

                  <FormField
                    label="PROGRAMA (OPCIONAL)"
                    htmlFor="program-id"
                    hint="Demo: 550e8400-e29b-41d4-a716-446655440000. Vacío = Todos."
                    error={validationErrors.programId}
                  >
                    <input
                      id="program-id"
                      type="text"
                      value={form.programId}
                      disabled={isBlocked}
                      placeholder="UUID de programa"
                      className={inputClass(!!validationErrors.programId)}
                      onChange={(event) =>
                        onFieldChange('programId', event.target.value)
                      }
                    />
                  </FormField>

                  {submitErrorMessage && <Alert message={submitErrorMessage} />}

                  <div className="flex flex-wrap gap-3 pt-2">
                    <button
                      type="submit"
                      disabled={isBlocked}
                      className="inline-flex items-center gap-2 rounded-lg bg-primary-600 px-5 py-3 text-label-md font-semibold text-body transition-colors hover:bg-primary-700 disabled:cursor-not-allowed disabled:opacity-50"
                    >
                      {isSubmitting ? (
                        <>
                          <Loader2 size={18} className="animate-spin" aria-hidden />
                          Encolando…
                        </>
                      ) : (
                        <>
                          <FileText size={18} aria-hidden />
                          Generar reporte PDF
                        </>
                      )}
                    </button>
                    {showJobPanel && !isBlocked && (
                      <button
                        type="button"
                        onClick={onReset}
                        className="rounded-lg border border-gray-300 bg-body px-5 py-3 text-label-md font-medium text-gray-700 transition-colors hover:bg-gray-50"
                      >
                        Nueva solicitud
                      </button>
                    )}
                  </div>
                </div>
              </form>

              {showJobPanel && (
                <section
                  className="rounded-2xl border border-gray-100 bg-body p-6 shadow-sm md:p-8"
                  aria-live="polite"
                >
                  <div className="mb-4 flex items-center gap-4">
                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary-50 text-primary-600">
                      <FileText size={24} />
                    </div>
                    <div>
                      <h2 className="text-heading-md text-primary-800">
                        Estado de generación
                      </h2>
                      <p className="font-mono text-code text-gray-500">
                        Job: {activeJobId ?? jobStatus?.jobId}
                      </p>
                    </div>
                  </div>

                  {statusErrorMessage && <Alert message={statusErrorMessage} />}

                  {jobStatus && (
                    <>
                      <div className="mb-4 flex items-center gap-2">
                        <span className="text-body-md text-gray-600">Estado:</span>
                        <StatusBadge
                          status={jobStatus.status ?? 'PENDING'}
                          label={statusLabel ?? jobStatus.status ?? 'Pendiente'}
                        />
                      </div>

                      <div className="mb-1 flex items-center justify-between text-label-md text-gray-600">
                        <span>Progreso estimado</span>
                        <span>{progressPercent}%</span>
                      </div>
                      <div
                        className="mb-4 h-2 w-full overflow-hidden rounded-full bg-gray-200"
                        role="progressbar"
                        aria-valuenow={progressPercent}
                        aria-valuemin={0}
                        aria-valuemax={100}
                      >
                        <div
                          className={`h-full transition-all duration-500 ${
                            jobStatus.status === 'FAILED'
                              ? 'bg-danger'
                              : 'bg-primary-600'
                          }`}
                          style={{ width: `${progressPercent}%` }}
                        />
                      </div>

                      {isPolling && (
                        <p className="mb-4 flex items-center gap-2 text-body-md text-gray-600">
                          <Loader2 size={16} className="animate-spin" aria-hidden />
                          Generando PDF — consultando cada 2 segundos…
                        </p>
                      )}

                      {jobErrorMessage && <Alert message={jobErrorMessage} />}

                      {isCompleted && (
                        <div className="rounded-xl border border-success/30 bg-success/5 p-4">
                          <div className="mb-3 flex items-center gap-2 text-heading-sm text-success">
                            <CheckCircle2 size={20} aria-hidden />
                            Reporte listo para descargar
                          </div>
                          <button
                            type="button"
                            disabled={isDownloading}
                            onClick={() => void onDownload()}
                            className="inline-flex items-center gap-2 rounded-lg bg-primary-600 px-5 py-3 text-label-md font-semibold text-body transition-colors hover:bg-primary-700 disabled:cursor-not-allowed disabled:opacity-50"
                          >
                            {isDownloading ? (
                              <>
                                <Loader2 size={18} className="animate-spin" aria-hidden />
                                Descargando…
                              </>
                            ) : (
                              <>
                                <Download size={18} aria-hidden />
                                Descargar PDF
                              </>
                            )}
                          </button>
                          {downloadErrorMessage && (
                            <p className="mt-3 text-body-md text-danger" role="alert">
                              {downloadErrorMessage}
                            </p>
                          )}
                        </div>
                      )}
                    </>
                  )}

                  {!jobStatus && isPolling && (
                    <p className="flex items-center gap-2 text-body-md text-gray-600">
                      <Loader2 size={16} className="animate-spin" aria-hidden />
                      Iniciando trabajo de reporte…
                    </p>
                  )}
                </section>
              )}

              <section className="rounded-2xl border border-gray-200 bg-gray-100 p-6">
                <div className="mb-4 flex items-center gap-2 text-primary-800">
                  <Info size={20} aria-hidden />
                  <h3 className="text-heading-sm">Guía rápida</h3>
                </div>
                <ol className="space-y-3">
                  <GuideStep
                    step="01"
                    text="Defina el año y, si aplica, facultad o programa."
                  />
                  <GuideStep
                    step="02"
                    text="Revise la vista previa del documento a la derecha."
                  />
                  <GuideStep
                    step="03"
                    text="Genere el PDF y descárguelo cuando esté Completado."
                  />
                </ol>
              </section>
            </section>

            <section className="xl:col-span-7">
              <ReportPreviewCard
                preview={preview}
                isCompleted={isCompleted}
                onDownload={onDownload}
                isDownloading={isDownloading}
              />
            </section>
          </div>
        </div>
      </main>
    </div>
  );
}

function ReportPreviewCard({
  preview,
  isCompleted,
  onDownload,
  isDownloading,
}: {
  preview: ReportPreviewModel;
  isCompleted: boolean;
  onDownload: () => void;
  isDownloading: boolean;
}) {
  return (
    <article className="overflow-hidden rounded-2xl border border-primary-100 bg-body shadow-sm">
      <div className="border-b border-primary-100 bg-gradient-to-r from-primary-700 to-primary-600 px-6 py-5 text-body md:px-8">
        <p className="text-label-md font-medium uppercase tracking-wide text-primary-100">
          Vista previa del documento
        </p>
        <h2 className="mt-1 text-heading-lg font-semibold">{preview.title}</h2>
        <p className="mt-1 text-body-md text-primary-100">
          {isCompleted
            ? 'Contenido generado — disponible para descarga en PDF'
            : 'Se actualiza al cambiar los filtros; el PDF oficial se genera al solicitarlo'}
        </p>
      </div>

      <div className="space-y-6 p-6 md:p-8">
        <dl className="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <MetaItem
            label="Generado"
            value={preview.generatedAtLabel ?? 'Pendiente de generación'}
          />
          <MetaItem label="Gestión" value={String(preview.managementYear)} />
          <MetaItem label="Facultad" value={preview.facultyLabel} />
          <MetaItem label="Programa" value={preview.programLabel} />
        </dl>

        <div className="overflow-x-auto rounded-xl border border-gray-200">
          <table className="min-w-full text-left">
            <thead className="bg-primary-50">
              <tr className="text-label-md uppercase tracking-wide text-primary-800">
                <th className="px-4 py-3 font-semibold">Programa</th>
                <th className="px-4 py-3 font-semibold">Semáforo</th>
                <th className="px-4 py-3 font-semibold">Indicadores</th>
                <th className="px-4 py-3 font-semibold">Aprobados</th>
                <th className="px-4 py-3 font-semibold">Avance %</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 bg-body">
              {preview.rows.map((row) => (
                <tr key={row.programName} className="text-body-md text-gray-800">
                  <td className="px-4 py-4 font-medium text-primary-900">
                    {row.programName}
                  </td>
                  <td className="px-4 py-4">
                    <SemaphoreBadge tone={row.semaphore} />
                  </td>
                  <td className="px-4 py-4 tabular-nums">{row.totalIndicators}</td>
                  <td className="px-4 py-4 tabular-nums">{row.approvedIndicators}</td>
                  <td className="min-w-40 px-4 py-4">
                    <div className="flex items-center gap-3">
                      <div
                        className="h-2 flex-1 overflow-hidden rounded-full bg-gray-200"
                        role="progressbar"
                        aria-valuenow={row.progressPercent}
                        aria-valuemin={0}
                        aria-valuemax={100}
                        aria-label={`Avance ${row.progressPercent}%`}
                      >
                        <div
                          className={`h-full rounded-full ${semaphoreBarClass(row.semaphore)}`}
                          style={{ width: `${row.progressPercent}%` }}
                        />
                      </div>
                      <span className="w-10 shrink-0 text-right tabular-nums font-medium text-gray-700">
                        {row.progressPercent}%
                      </span>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {isCompleted && (
          <div className="flex flex-wrap items-center justify-between gap-3 rounded-xl border border-success/20 bg-success/5 px-4 py-3">
            <p className="text-body-md text-success">
              El PDF oficial incluye esta misma estructura con marca temporal.
            </p>
            <button
              type="button"
              disabled={isDownloading}
              onClick={() => void onDownload()}
              className="inline-flex items-center gap-2 rounded-lg bg-primary-600 px-4 py-2.5 text-label-md font-semibold text-body transition-colors hover:bg-primary-700 disabled:cursor-not-allowed disabled:opacity-50"
            >
              {isDownloading ? (
                <>
                  <Loader2 size={16} className="animate-spin" aria-hidden />
                  Descargando…
                </>
              ) : (
                <>
                  <Download size={16} aria-hidden />
                  Descargar PDF
                </>
              )}
            </button>
          </div>
        )}
      </div>
    </article>
  );
}

function MetaItem({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-gray-200 bg-gray-50 px-4 py-3">
      <dt className="text-label-md font-medium uppercase tracking-wide text-gray-500">
        {label}
      </dt>
      <dd className="mt-1 text-body-lg font-medium text-primary-900">{value}</dd>
    </div>
  );
}

function SemaphoreBadge({ tone }: { tone: SemaphoreTone }) {
  const classes: Record<SemaphoreTone, string> = {
    GREEN: 'border-success/30 bg-success/10 text-success',
    YELLOW: 'border-warning/40 bg-warning/15 text-gray-800',
    RED: 'border-danger/30 bg-danger/10 text-danger',
  };
  const dot: Record<SemaphoreTone, string> = {
    GREEN: 'bg-success',
    YELLOW: 'bg-warning',
    RED: 'bg-danger',
  };

  return (
    <span
      className={`inline-flex items-center gap-2 rounded-full border px-3 py-1 text-label-md font-semibold ${classes[tone]}`}
    >
      <span className={`h-2.5 w-2.5 rounded-full ${dot[tone]}`} aria-hidden />
      {SEMAPHORE_LABELS[tone]}
      <span className="sr-only">({tone})</span>
    </span>
  );
}

function semaphoreBarClass(tone: SemaphoreTone): string {
  if (tone === 'GREEN') return 'bg-success';
  if (tone === 'RED') return 'bg-danger';
  return 'bg-warning';
}

type FormFieldProps = {
  label: string;
  htmlFor: string;
  error?: string;
  hint?: string;
  children: ReactNode;
};

function FormField({ label, htmlFor, error, hint, children }: FormFieldProps) {
  return (
    <div>
      <label htmlFor={htmlFor} className="mb-1 block text-label-md text-gray-600">
        {label}
      </label>
      {children}
      {hint && !error && (
        <p className="mt-1 text-body-md text-gray-500">{hint}</p>
      )}
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

function StatusBadge({ status, label }: { status: string; label: string }) {
  const tone =
    status === 'COMPLETED'
      ? 'bg-success/10 text-success border-success/30'
      : status === 'FAILED'
        ? 'bg-danger/10 text-danger border-danger/30'
        : status === 'IN_PROGRESS'
          ? 'bg-info/10 text-info border-info/30'
          : 'bg-warning/10 text-gray-700 border-warning/30';

  return (
    <span
      className={`rounded-full border px-3 py-1 text-label-md font-semibold ${tone}`}
    >
      {label}
    </span>
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
