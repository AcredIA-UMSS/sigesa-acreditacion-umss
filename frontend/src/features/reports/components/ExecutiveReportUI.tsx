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
  const statusLabel = jobStatus
    ? (JOB_STATUS_LABELS[jobStatus.status] ?? jobStatus.status)
    : null;
  const jobErrorMessage = mapJobErrorCode(jobStatus?.errorCode);
  const showJobPanel = activeJobId !== null || jobStatus !== undefined;

  return (
    <div className="flex h-screen flex-1 flex-col overflow-hidden bg-gray-50">
      <header className="flex items-center justify-between border-b border-gray-200 bg-body px-8 py-4">
        <nav className="text-body-md text-gray-600" aria-label="Ruta de navegación">
          <span className="text-primary-600">Inicio</span>
          <span className="mx-2 text-gray-400">/</span>
          <span className="text-gray-700">Reporte ejecutivo PDF</span>
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
              Reporte Ejecutivo PDF
            </h1>
            <p className="text-body-lg text-gray-600">
              FSD-UC-014 — El jefe DUEA aplica filtros institucionales y genera
              un PDF con marca temporal, contexto de filtros y semáforo ejecutivo.
              La generación es asíncrona (P95 ≤ 5 min).
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
                    <BarChart3 size={24} />
                  </div>
                  <div>
                    <h2 className="text-heading-md text-primary-800">
                      Filtros del reporte
                    </h2>
                    <p className="text-body-md text-gray-500">
                      Año de gestión obligatorio; facultad y programa opcionales
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
                    hint="Deje vacío para incluir todas las facultades"
                  >
                    <input
                      id="faculty-id"
                      type="text"
                      value={form.facultyId}
                      disabled={isBlocked}
                      placeholder="UUID de facultad"
                      className={inputClass(false)}
                      onChange={(event) =>
                        onFieldChange('facultyId', event.target.value)
                      }
                    />
                  </FormField>

                  <FormField
                    label="PROGRAMA (OPCIONAL)"
                    htmlFor="program-id"
                    hint="Deje vacío para incluir todos los programas"
                  >
                    <input
                      id="program-id"
                      type="text"
                      value={form.programId}
                      disabled={isBlocked}
                      placeholder="UUID de programa / carrera"
                      className={inputClass(false)}
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
                  className="rounded-2xl border border-gray-100 bg-body p-8 shadow-sm"
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
                        <StatusBadge status={jobStatus.status} label={statusLabel ?? jobStatus.status} />
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

                      {jobErrorMessage && (
                        <Alert message={jobErrorMessage} />
                      )}

                      {jobStatus.status === 'COMPLETED' && (
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
                    text="Defina el año de gestión y, si aplica, acote por facultad o programa."
                  />
                  <GuideStep
                    step="02"
                    text='Pulse "Generar reporte PDF". El sistema encola un trabajo asíncrono.'
                  />
                  <GuideStep
                    step="03"
                    text="Cuando el estado sea Completado, descargue el PDF con timestamp institucional."
                  />
                </ol>
              </section>

              <section className="rounded-2xl border border-primary-100 bg-primary-50 p-6">
                <h3 className="mb-3 text-heading-sm text-primary-800">
                  Flujo asíncrono
                </h3>
                <ul className="space-y-2 text-body-md text-primary-700">
                  <li>Pendiente → En progreso → Completado</li>
                  <li>Polling automático cada 2 s</li>
                  <li>Solo rol JD autorizado (FSD-BR-14)</li>
                </ul>
                <p className="mt-4 text-label-md text-primary-600">
                  Tiempo objetivo P95: 5 minutos
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
